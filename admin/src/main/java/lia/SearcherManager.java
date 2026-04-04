package lia;

import java.io.IOException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Directory;

public class SearcherManager {
  private IndexSearcher currentIndexSearcher;

  private IndexWriter indexWriter;

  // Allows concurrent reads via readLock, exclusive writes via writeLock
  private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
  private final Lock readLock = lock.readLock();
  private final Lock writeLock = lock.writeLock();

  public SearcherManager(Directory directory) throws IOException {
    currentIndexSearcher = new IndexSearcher(DirectoryReader.open(directory));
    warm(currentIndexSearcher);
  }

  public SearcherManager(IndexWriter indexWriter) throws IOException {
    this.indexWriter = indexWriter;
    currentIndexSearcher = new IndexSearcher(DirectoryReader.open(indexWriter));
    warm(currentIndexSearcher);

    // Warm newly merged segments in the background before they become visible to searchers
    indexWriter.getConfig()
            .setMergedSegmentWarmer(reader ->
                    SearcherManager.this.warm(new IndexSearcher(reader)));
  }

  public void warm(IndexSearcher indexSearcher) throws IOException {
  }

  public void maybeReopen() throws IOException {
    // Exclusive lock: prevents concurrent reopens and blocks get() during the swap
    writeLock.lock();
    try {
      // Returns null if the index has not changed since the current reader was opened
      IndexReader newIndexReader =
              DirectoryReader.openIfChanged(
                      (DirectoryReader) currentIndexSearcher.getIndexReader());

      if (newIndexReader != null &&
              newIndexReader != currentIndexSearcher.getIndexReader()) {

        IndexSearcher newIndexSearcher =
                new IndexSearcher(newIndexReader);

        // Skip warming when indexWriter is set — merged segments are warmed via the warmer callback
        if (indexWriter == null) {
          warm(newIndexSearcher);
        }

        swapIndexSearcher(newIndexSearcher);
      }
    } finally {
      writeLock.unlock();
    }
  }

  public IndexSearcher get() {
    // Shared lock: multiple threads can call get() concurrently
    readLock.lock();
    try {
      // incRef prevents the reader from being closed while a caller holds it
      currentIndexSearcher.getIndexReader().incRef();
      return currentIndexSearcher;
    } finally {
      readLock.unlock();
    }
  }

  public void release(IndexSearcher indexSearcher) throws IOException {
    readLock.lock();
    try {
      // Pair to the incRef in get(); reader closes itself when ref count reaches zero
      indexSearcher.getIndexReader().decRef();
    } finally {
      readLock.unlock();
    }
  }

  // Must be called while holding writeLock
  private void swapIndexSearcher(IndexSearcher newIndexSearcher) throws IOException {
    if (currentIndexSearcher != null) {
      // Release our reference to the old searcher; it will close when all callers have released it
      currentIndexSearcher.getIndexReader().decRef();
    }
    currentIndexSearcher = newIndexSearcher;
  }

  public void close() throws IOException {
    writeLock.lock();
    try {
      swapIndexSearcher(null);
    } finally {
      writeLock.unlock();
    }
  }
}
