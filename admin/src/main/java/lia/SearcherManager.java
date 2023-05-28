package lia;

import java.io.IOException;

import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Directory;

public class SearcherManager {
  private IndexSearcher currentSearcher;

  private IndexWriter writer;

  public SearcherManager(Directory dir) throws IOException {
    currentSearcher = new IndexSearcher(DirectoryReader.open(dir));
    warm(currentSearcher);
  }

  public SearcherManager(IndexWriter writer) throws IOException {
    this.writer = writer;
    currentSearcher = new IndexSearcher(DirectoryReader.open(writer));
    warm(currentSearcher);

    writer.getConfig().setMergedSegmentWarmer(reader -> SearcherManager.this.warm(new IndexSearcher(reader)));
  }

  public void warm(IndexSearcher searcher) throws IOException {}

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
      final IndexSearcher searcher = get();
      try {
        IndexReader newReader = DirectoryReader.openIfChanged((DirectoryReader) currentSearcher.getIndexReader());
        if (newReader != currentSearcher.getIndexReader()) {
          IndexSearcher newSearcher = new IndexSearcher(newReader);
          if (writer == null) {
            warm(newSearcher);
          }
          swapSearcher(newSearcher);
        }
      } finally {
        release(searcher);
      }
    } finally {
      doneReopen();
    }
  }

  public synchronized IndexSearcher get() {
    currentSearcher.getIndexReader().incRef();
    return currentSearcher;
  }

  public synchronized void release(IndexSearcher searcher) throws IOException {
    searcher.getIndexReader().decRef();
  }

  private synchronized void swapSearcher(IndexSearcher newSearcher) throws IOException {
    release(currentSearcher);
    currentSearcher = newSearcher;
  }

  public void close() throws IOException {
    swapSearcher(null);
  }
}
