package lia;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.Directory;

public class ThreadedIndexWriter extends IndexWriter {
  // Virtual threads are cheap JVM-managed threads
  private final ExecutorService threadPool = Executors.newVirtualThreadPerTaskExecutor();

  public ThreadedIndexWriter(Directory directory, Analyzer analyzer) throws IOException {
    super(directory, new IndexWriterConfig(analyzer));
  }

  public void addDocument(Document document) {
    threadPool.execute(() -> {
      try {
        ThreadedIndexWriter.super.addDocument(document);
      } catch (IOException e) {
        throw new RuntimeException("Failed to add document in background thread", e);
      }
    });
  }

  public void updateDocument(Term term, Document document) {
    threadPool.execute(() -> {
      try {
        ThreadedIndexWriter.super.updateDocument(term, document);
      } catch (IOException e) {
        throw new RuntimeException("Failed to update document in background thread", e);
      }
    });
  }

  @Override
  public void close() throws IOException {
    threadPool.close(); // drain all pending tasks before committing
    super.close();
  }

  @Override
  public void rollback() throws IOException {
    threadPool.close(); // drain all pending tasks before discarding changes
    super.rollback();
  }
}
