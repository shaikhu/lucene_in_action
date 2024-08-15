package lia;

import java.io.IOException;

import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Directory;

public class SearcherManager {
  private IndexSearcher currentIndexSearcher;

  private IndexWriter indexWriter;

  public SearcherManager(Directory directory) throws IOException {
    currentIndexSearcher = new IndexSearcher(DirectoryReader.open(directory));
    warm(currentIndexSearcher);
  }

  public SearcherManager(IndexWriter indexWriter) throws IOException {
    this.indexWriter = indexWriter;
    currentIndexSearcher = new IndexSearcher(DirectoryReader.open(indexWriter));
    warm(currentIndexSearcher);

    indexWriter.getConfig().setMergedSegmentWarmer(reader -> SearcherManager.this.warm(new IndexSearcher(reader)));
  }

  public void warm(IndexSearcher indexSearcher) throws IOException {}

  private boolean reopening;

  private synchronized void startReopen() throws InterruptedException {
    while (reopening) {
      wait();
    }
    reopening = true;
  }

  private synchronized void doneReopen() {
    reopening = false;
    notifyAll();
  }

  public void maybeReopen() throws InterruptedException, IOException {
    startReopen();

    try {
      final IndexSearcher indexSearcher = get();
      try {
        IndexReader newIndexReader = DirectoryReader.openIfChanged((DirectoryReader) currentIndexSearcher.getIndexReader());
        if (newIndexReader != currentIndexSearcher.getIndexReader()) {
          IndexSearcher newIndexSearcher = new IndexSearcher(newIndexReader);
          if (indexWriter == null) {
            warm(newIndexSearcher);
          }
          swapIndexSearcher(newIndexSearcher);
        }
      } finally {
        release(indexSearcher);
      }
    } finally {
      doneReopen();
    }
  }

  public synchronized IndexSearcher get() {
    currentIndexSearcher.getIndexReader().incRef();
    return currentIndexSearcher;
  }

  public synchronized void release(IndexSearcher indexSearcher) throws IOException {
    indexSearcher.getIndexReader().decRef();
  }

  private synchronized void swapIndexSearcher(IndexSearcher newIndexSearcher) throws IOException {
    release(currentIndexSearcher);
    currentIndexSearcher = newIndexSearcher;
  }

  public void close() throws IOException {
    swapIndexSearcher(null);
  }
}
