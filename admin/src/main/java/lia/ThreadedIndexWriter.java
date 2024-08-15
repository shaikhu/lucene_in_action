package lia;

import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.Directory;

public class ThreadedIndexWriter extends IndexWriter {
  private ExecutorService threadPool;

  private Analyzer defaultAnalyzer;

  public ThreadedIndexWriter(Directory directory, Analyzer analyzer, int numThreads, int maxQueueSize) throws IOException {
    super(directory, new IndexWriterConfig(analyzer));
    defaultAnalyzer = analyzer;
    threadPool = new ThreadPoolExecutor(numThreads, numThreads, 0, TimeUnit.SECONDS,
        new ArrayBlockingQueue<>(maxQueueSize, false), new ThreadPoolExecutor.CallerRunsPolicy());
  }

  public void addDocument(Document document) {
    threadPool.execute(new Job(document, null, defaultAnalyzer));
  }

  public void addDocument(Document document, Analyzer analyzer) {
    threadPool.execute(new Job(document, null, analyzer));
  }

  public void updateDocument(Term term, Document document) {
    threadPool.execute(new Job(document, term, defaultAnalyzer));
  }

  public void updateDocument(Term term, Document document, Analyzer analyzer) {
    threadPool.execute(new Job(document, term, analyzer));
  }

  public void close() throws IOException {
    finish();
    super.close();
  }

  public void rollback() throws IOException {
    finish();
    super.rollback();
  }

  private void finish() {
    threadPool.shutdown();
    while (true) {
      try {
        if (threadPool.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS)) {
          break;
        }
      }
      catch (InterruptedException ie) {
        Thread.currentThread().interrupt();
        throw new RuntimeException(ie);
      }
    }
  }

  private class Job implements Runnable {
    Document document;

    Analyzer analyzer;

    Term delTerm;

    public Job(Document document, Term delTerm, Analyzer analyzer) {
      this.document = document;
      this.analyzer = analyzer;
      this.delTerm = delTerm;
    }

    public void run() {
      try {
        if (delTerm != null) {
          ThreadedIndexWriter.super.updateDocument(delTerm, document);
        } else {
          ThreadedIndexWriter.super.addDocument(document);
        }
      } catch (IOException ioe) {
        throw new RuntimeException(ioe);
      }
    }
  }
}
