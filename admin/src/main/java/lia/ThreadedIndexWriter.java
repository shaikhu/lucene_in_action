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

public class ThreadedIndexWriter extends IndexWriter
{
  private ExecutorService threadPool;

  private Analyzer defaultAnalyzer;


  private class Job implements Runnable {
    Document doc;

    Analyzer analyzer;

    Term delTerm;

    public Job(Document doc, Term delTerm, Analyzer analyzer) {
      this.doc = doc;
      this.analyzer = analyzer;
      this.delTerm = delTerm;
    }

    public void run() {
      try {
        if (delTerm != null) {
          ThreadedIndexWriter.super.updateDocument(delTerm, doc);
        } else {
          ThreadedIndexWriter.super.addDocument(doc);
        }
      } catch (IOException ioe) {
        throw new RuntimeException(ioe);
      }
    }
  }

  public ThreadedIndexWriter(
      Directory dir, Analyzer a,
      int numThreads,
      int maxQueueSize) throws IOException
  {

    super(dir, new IndexWriterConfig(a));

    defaultAnalyzer = a;

    threadPool = new ThreadPoolExecutor(
        numThreads, numThreads,
        0, TimeUnit.SECONDS,
        new ArrayBlockingQueue<>(maxQueueSize, false),
        new ThreadPoolExecutor.CallerRunsPolicy());
  }

  public void addDocument(Document doc) {
    threadPool.execute(new Job(doc, null, defaultAnalyzer));
  }

  public void addDocument(Document doc, Analyzer a) {
    threadPool.execute(new Job(doc, null, a));
  }

  public void updateDocument(Term term, Document doc) {
    threadPool.execute(new Job(doc, term, defaultAnalyzer));
  }

  public void updateDocument(Term term, Document doc, Analyzer a) {
    threadPool.execute(new Job(doc, term, a));
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
}
