package lia;

import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneId;

import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.document.DateTools;
import org.apache.lucene.document.DateTools.Resolution;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TermRangeQuery;
import org.apache.lucene.store.ByteBuffersDirectory;
import org.apache.lucene.store.Directory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FilteringTest {
  private static final int MAX = 500;

  private static final LocalDateTime START_DATE = LocalDateTime.of(2009, Month.FEBRUARY, 1, 0, 0);

  private static final Query TERM_QUERY_SUE = new TermQuery(new Term("owner", "sue"));

  private static final Query TERM_QUERY_BOB = new TermQuery(new Term("owner", "bob"));

  private static final Query DATE_QUERY = TermRangeQuery.newStringRange("date",
      String.valueOf(Long.MIN_VALUE), String.valueOf(Long.MAX_VALUE), true, true);

  private Directory directory;

  private IndexSearcher indexSearcher;

  @BeforeEach
  void setup() throws Exception {
    directory = new ByteBuffersDirectory();

    var date = START_DATE;
    try (var indexWriter = new IndexWriter(directory, new IndexWriterConfig(new WhitespaceAnalyzer()))) {
      for (var i = 0; i < MAX; i++) {
        var document = new Document();
        document.add(new StringField("key", String.valueOf(i + 1), Store.YES));
        document.add(new StringField("owner", (i < MAX / 2) ? "bob" : "sue", Store.YES));
        document.add(new StringField("date", DateTools.timeToString(date.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(), Resolution.DAY), Store.YES));
        indexWriter.addDocument(document);
        date = date.plusDays(1);
      }
    }

    indexSearcher = new IndexSearcher(DirectoryReader.open(directory));
  }

  @AfterEach
  void tearDown() throws Exception {
    directory.close();
  }

  @Test
  void testOr() throws Exception {
    var query = new BooleanQuery.Builder()
        .add(TERM_QUERY_SUE, Occur.SHOULD)
        .add(TERM_QUERY_BOB, Occur.SHOULD)
        .build();

    var topDocs = indexSearcher.search(query, 10);
    assertThat(topDocs.totalHits.value).isEqualTo(MAX);
  }

  @Test
  void testAnd() throws Exception {
    var query = new BooleanQuery.Builder()
        .add(DATE_QUERY, Occur.MUST)
        .add(TERM_QUERY_BOB, Occur.MUST)
        .build();

    var topDocs = indexSearcher.search(query, 10);
    assertThat(topDocs.totalHits.value).isEqualTo(MAX / 2);
    assertThat(topDocs.scoreDocs)
        .extracting(scoreDoc -> indexSearcher.storedFields().document(scoreDoc.doc).get("owner"))
        .containsOnly("bob");
  }

  @Test
  void testAndNot() throws Exception {
    var query = new BooleanQuery.Builder()
        .add(DATE_QUERY, Occur.MUST)
        .add(TERM_QUERY_BOB, Occur.MUST_NOT)
        .build();

    var topDocs = indexSearcher.search(query, 10);
    assertThat(topDocs.totalHits.value).isEqualTo(MAX / 2);
    assertThat(topDocs.scoreDocs)
        .extracting(scoreDoc -> indexSearcher.storedFields().document(scoreDoc.doc).get("owner"))
        .containsOnly("sue");
  }
}
