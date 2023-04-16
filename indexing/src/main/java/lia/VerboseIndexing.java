package lia;

import java.io.IOException;

import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.ByteBuffersDirectory;
import org.apache.lucene.store.Directory;

public class VerboseIndexing
{
  private void index() throws IOException {
    Directory dir = new ByteBuffersDirectory();

    IndexWriterConfig config = new IndexWriterConfig(new WhitespaceAnalyzer());
    config.setInfoStream(System.out);
    IndexWriter writer = new IndexWriter(dir, config);

    for (int i=0; i<100; i++) {
      Document doc = new Document();
      doc.add(new StringField("keyword", "goober", Store.YES));
      writer.addDocument(doc);
    }
    writer.close();
  }

  public static void main(String... args) throws IOException {
    VerboseIndexing vi = new VerboseIndexing();
    vi.index();
  }
}
