package lia;

import java.io.IOException;

import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.ByteBuffersDirectory;
import org.apache.lucene.store.Directory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PhraseQueryTest {
  private Directory directory;

  private IndexSearcher searcher;

  @BeforeEach
  void setup() throws IOException {
    directory = new ByteBuffersDirectory();

    try (IndexWriter writer = new IndexWriter(directory, new IndexWriterConfig(new WhitespaceAnalyzer()))) {
      Document doc = new Document();
      doc.add(new TextField("field", "the quick brown fox jumped over the lazy dog", Store.NO));
      writer.addDocument(doc);
    }
    searcher = new IndexSearcher(DirectoryReader.open(directory));
  }

  @AfterEach
  void tearDown() throws IOException {
    directory.close();
  }

  @Test
  void testSlopComparison() throws Exception {
    assertThat(matched(0, "quick", "fox")).isFalse();
    assertThat(matched(1, "quick", "fox")).isTrue();
  }

  @Test
  void testReverse() throws Exception {
    assertThat(matched(2, "fox", "quick")).isFalse();
    assertThat(matched(3, "fox", "quick")).isTrue();
  }

  @Test
  void testMultiple() throws Exception {
    assertThat(matched(3, "quick", "jumped", "lazy")).isFalse();
    assertThat(matched(4, "quick", "jumped", "lazy")).isTrue();
    assertThat(matched(7, "lazy", "jumped", "quick")).isFalse();
    assertThat(matched(8, "lazy", "jumped", "quick")).isTrue();
  }

  private boolean matched(int slop, String... phrase) throws IOException {
    PhraseQuery query = new PhraseQuery(slop, "field", phrase);
    TopDocs matches = searcher.search(query, 10);
    return matches.totalHits.value > 0;
  }
}
