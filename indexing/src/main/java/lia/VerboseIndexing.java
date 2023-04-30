package lia;

import java.io.IOException;
import java.util.stream.IntStream;

import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.ByteBuffersDirectory;
import org.apache.lucene.store.Directory;

public class VerboseIndexing {
  private void index() throws IOException {
    try (Directory dir = new ByteBuffersDirectory()) {
      IndexWriterConfig config = new IndexWriterConfig(new WhitespaceAnalyzer());
      config.setInfoStream(System.out);
      IndexWriter writer = new IndexWriter(dir, config);
      IntStream.range(1, 100).forEach(index -> indexDocument(writer));
      writer.close();
    }
  }

  private void indexDocument(IndexWriter writer) {
    try {
      Document doc = new Document();
      doc.add(new StringField("keyword", "goober", Store.YES));
      writer.addDocument(doc);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public static void main(String... args) throws IOException {
    VerboseIndexing vi = new VerboseIndexing();
    vi.index();
  }
}
