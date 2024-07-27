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

public class VerboseIndexing {
  private void index() throws IOException {
    var indexWriterConfig = new IndexWriterConfig(new WhitespaceAnalyzer());
    indexWriterConfig.setInfoStream(System.out);

    try (var directory = new ByteBuffersDirectory();
         var indexWriter = new IndexWriter(directory, indexWriterConfig)) {
      IntStream.range(1, 100).forEach(index -> indexDocument(indexWriter));
    }
  }

  private void indexDocument(IndexWriter indexWriter) {
    try {
      var document = new Document();
      document.add(new StringField("keyword", "goober", Store.YES));
      indexWriter.addDocument(document);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public static void main(String... args) throws IOException {
    new VerboseIndexing().index();
  }
}
