package lia;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.store.ByteBuffersDirectory;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class NearRealTimeTest {
  @Test
  void testNearRealTime() throws Exception {
    try (var directory = new ByteBuffersDirectory();
         var indexWriter = new IndexWriter(directory, new IndexWriterConfig(new StandardAnalyzer()))) {

      for (int i = 0; i < 10; i++) {
        var document = new Document();
        document.add(new StringField("id", String.valueOf(i), Store.NO));
        document.add(new TextField("text", "aaa", Store.NO));
        indexWriter.addDocument(document);
      }

      try (var directoryReader = DirectoryReader.open(indexWriter)) {
        var indexSearcher = new IndexSearcher(directoryReader);

        var query = new TermQuery(new Term("text", "aaa"));
        var topDocs = indexSearcher.search(query, 1);
        assertThat(topDocs.totalHits.value()).isEqualTo(10);

        indexWriter.deleteDocuments(new Term("id", "7"));

        var document = new Document();
        document.add(new StringField("id", "11", Store.NO));
        document.add(new TextField("text", "bbb", Store.NO));
        indexWriter.addDocument(document);

        try (var newDirectoryReader = DirectoryReader.openIfChanged(directoryReader)) {
          assertThat(directoryReader).isNotSameAs(newDirectoryReader);
          indexSearcher = new IndexSearcher(newDirectoryReader);

          topDocs = indexSearcher.search(query, 10);
          assertThat(topDocs.totalHits.value()).isEqualTo(9);

          query = new TermQuery(new Term("text", "bbb"));
          topDocs = indexSearcher.search(query, 1);
          assertThat(topDocs.totalHits.value()).isOne();
        }
      }
    }
  }
}
