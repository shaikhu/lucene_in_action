package lia;

import java.io.IOException;
import java.util.List;

import lia.common.TestUtil;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.miscellaneous.LimitTokenCountAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.store.ByteBuffersDirectory;
import org.apache.lucene.store.Directory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class IndexingTest {
  record Data(String id, String country, String contents, String city) {}

  private static final List<Data> INDEX_DATA = List.of(
    new Data("1", "Netherlands", "Amsterdam has lots of bridges", "Amsterdam"),
    new Data("2", "Italy", "Venice has lots of canals", "Venice"));


  private Directory directory;

  @BeforeEach
  void setup() throws Exception {
    directory = new ByteBuffersDirectory();

    try (IndexWriter writer = getWriter()) {
      for (Data data : INDEX_DATA) {
        Document doc = new Document();
        doc.add(new StringField("id", data.id, Store.YES));
        doc.add(new StringField("country", data.country, Store.YES));
        doc.add(new TextField("contents", data.contents, Store.NO));
        doc.add(new StringField("city", data.city, Store.YES));
        writer.addDocument(doc);
      }
    }
  }

  @AfterEach
  void tearDown() throws Exception {
    directory.close();
  }

  @Test
  void testIndexWriter() throws IOException {
    try (IndexWriter writer = getWriter()) {
      assertThat(writer.getDocStats().numDocs).isEqualTo(INDEX_DATA.size());
    }
  }

  @Test
  void testIndexReader() throws IOException {
    try (DirectoryReader reader = DirectoryReader.open(directory)) {
      assertThat(reader.maxDoc()).isEqualTo(INDEX_DATA.size());
      assertThat(reader.numDocs()).isEqualTo(INDEX_DATA.size());
    }
  }

  @Test
  void testDelete() throws IOException {
    try (IndexWriter writer = getWriter()) {
      assertThat(writer.getDocStats().numDocs).isEqualTo(2);
      writer.deleteDocuments(new Term("id", "1"));
      writer.commit();
      assertThat(writer.hasDeletions()).isTrue();

      assertThat(writer.getDocStats().maxDoc).isEqualTo(2);
      assertThat(writer.getDocStats().numDocs).isOne();
    }
  }

  @Test
  void testUpdate() throws IOException {
    assertThat(getHitCount("city", "Amsterdam")).isOne();

    try (IndexWriter writer = getWriter()) {
      Document document = new Document();
      document.add(new StringField("id", "1", Store.YES));
      document.add(new StringField("country", "Netherlands", Store.YES));
      document.add(new TextField("contents", "Den Haag has a lot of museums", Store.NO));
      document.add(new StringField("city", "Den Haag", Store.YES));
      writer.updateDocument(new Term("id", "1"), document);
    }

    assertThat(getHitCount("city", "Amsterdam")).isZero();
    assertThat(getHitCount("city", "Den Haag")).isOne();
  }

  @Test
  void testMaxFieldLength() throws IOException {
    assertThat(getHitCount("contents", "bridges")).isOne();
    try (IndexWriter writer = new IndexWriter(directory,
        new IndexWriterConfig(new LimitTokenCountAnalyzer(new WhitespaceAnalyzer(), 1)))) {
      Document doc = new Document();
      doc.add(new TextField("contents", "these bridges can't be found", Store.NO));
      writer.addDocument(doc);
    }
    assertThat(getHitCount("contents", "bridges")).isOne();
  }

  private IndexWriter getWriter() throws IOException {
    return new IndexWriter(directory, new IndexWriterConfig(new WhitespaceAnalyzer()));
  }

  private long getHitCount(String fieldName, String searchString) throws IOException {
    try (DirectoryReader reader = DirectoryReader.open(directory)) {
      IndexSearcher searcher = new IndexSearcher(reader);
      Term term = new Term(fieldName, searchString);
      Query query = new TermQuery(term);
      return TestUtil.hitCount(searcher, query);
    }
  }
}
