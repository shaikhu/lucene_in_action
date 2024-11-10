package lia;

import java.io.IOException;
import java.nio.file.Path;
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
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.MMapDirectory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.assertj.core.api.Assertions.assertThat;

class IndexingTest {
  record Data(String id, String country, String contents, String city) {}

  private static final List<Data> INDEX_DATA = List.of(
    new Data("1", "Netherlands", "Amsterdam has lots of bridges", "Amsterdam"),
    new Data("2", "Italy", "Venice has lots of canals", "Venice"));

  @TempDir
  private Path index;

  private Directory directory;

  @BeforeEach
  void setup() throws Exception {
    directory = new MMapDirectory(index);

    try (var indexWriter = getIndexWriter()) {
      for (var data : INDEX_DATA) {
        var document = new Document();
        document.add(new StringField("id", data.id, Store.YES));
        document.add(new StringField("country", data.country, Store.YES));
        document.add(new TextField("contents", data.contents, Store.NO));
        document.add(new StringField("city", data.city, Store.YES));
        indexWriter.addDocument(document);
      }
    }
  }

  @AfterEach
  void tearDown() throws Exception {
    directory.close();
  }

  @Test
  void testIndexWriter() throws IOException {
    try (var indexWriter = getIndexWriter()) {
      assertThat(indexWriter.getDocStats().numDocs).isEqualTo(INDEX_DATA.size());
    }
  }

  @Test
  void testIndexReader() throws IOException {
    try (var directoryReader = DirectoryReader.open(directory)) {
      assertThat(directoryReader.maxDoc()).isEqualTo(INDEX_DATA.size());
      assertThat(directoryReader.numDocs()).isEqualTo(INDEX_DATA.size());
    }
  }

  @Test
  void testDelete() throws IOException {
    try (var indexWriter = getIndexWriter()) {
      assertThat(indexWriter.getDocStats().numDocs).isEqualTo(2);
      indexWriter.deleteDocuments(new Term("id", "1"));
      assertThat(indexWriter.hasDeletions()).isTrue();
      indexWriter.commit(); // forces lucene to merge index segments after deleting the document
      assertThat(indexWriter.hasDeletions()).isFalse();
      assertThat(indexWriter.getDocStats().maxDoc).isOne();
      assertThat(indexWriter.getDocStats().numDocs).isOne();
    }
  }

  @Test
  void testUpdate() throws IOException {
    assertThat(getHitCount("city", "Amsterdam")).isOne();

    try (var indexWriter = getIndexWriter()) {
      var document = new Document();
      document.add(new StringField("id", "1", Store.YES));
      document.add(new StringField("country", "Netherlands", Store.YES));
      document.add(new TextField("contents", "Den Haag has a lot of museums", Store.NO));
      document.add(new StringField("city", "Den Haag", Store.YES));
      indexWriter.updateDocument(new Term("id", "1"), document);
    }

    assertThat(getHitCount("city", "Amsterdam")).isZero();
    assertThat(getHitCount("city", "Den Haag")).isOne();
  }

  @Test
  void testMaxFieldLength() throws IOException {
    assertThat(getHitCount("contents", "bridges")).isOne();
    try (var indexWriter = new IndexWriter(directory, new IndexWriterConfig(new LimitTokenCountAnalyzer(new WhitespaceAnalyzer(), 1)))) {
      var document = new Document();
      document.add(new TextField("contents", "these bridges can't be found", Store.NO));
      indexWriter.addDocument(document);
    }
    assertThat(getHitCount("contents", "bridges")).isOne();
  }

  private IndexWriter getIndexWriter() throws IOException {
    return new IndexWriter(directory, new IndexWriterConfig(new WhitespaceAnalyzer()));
  }

  private long getHitCount(String fieldName, String searchString) throws IOException {
    try (var directoryReader = DirectoryReader.open(directory)) {
      return TestUtil.hitCount(new IndexSearcher(directoryReader), new TermQuery(new Term(fieldName, searchString)));
    }
  }
}
