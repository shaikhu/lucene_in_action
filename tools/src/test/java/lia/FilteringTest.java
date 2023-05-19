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
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.ByteBuffersDirectory;
import org.apache.lucene.store.Directory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FilteringTest {
  private static final int MAX = 500;

  private static final LocalDateTime START_DATE = LocalDateTime.of(2009, Month.FEBRUARY, 1, 0, 0);

  private static final Query SUE_TERM_QUERY = new TermQuery(new Term("owner", "sue"));

  private static final Query BOB_TERM_QUERY = new TermQuery(new Term("owner", "bob"));

  private static final Query DATE_QUERY = TermRangeQuery.newStringRange("date",
      String.valueOf(Long.MIN_VALUE),
      String.valueOf(Long.MAX_VALUE),
      true,
      true);

  private Directory directory;

  private IndexSearcher searcher;

  @BeforeEach
  void setup() throws Exception {
    directory = new ByteBuffersDirectory();

    LocalDateTime date = START_DATE;
    IndexWriter writer = new IndexWriter(directory, new IndexWriterConfig(new WhitespaceAnalyzer()));
    for (int i=0; i< MAX; i++) {
      Document doc = new Document();
      doc.add(new StringField("key", String.valueOf(i + 1), Store.YES));
      doc.add(new StringField("owner", (i < MAX / 2) ? "bob" : "sue", Store.YES));
      doc.add(new StringField("date", DateTools.timeToString(date.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(), Resolution.DAY), Store.YES));
      writer.addDocument(doc);
      date = date.plusDays(1);
    }
    writer.close();

    searcher = new IndexSearcher(DirectoryReader.open(directory));
  }

  @AfterEach
  void tearDown() throws Exception {
    directory.close();
  }

  @Test
  void testOr() throws Exception {
    Query query = new BooleanQuery.Builder()
        .add(SUE_TERM_QUERY, Occur.SHOULD)
        .add(BOB_TERM_QUERY, Occur.SHOULD)
        .build();

    TopDocs hits = searcher.search(query, 10);
    assertThat(hits.totalHits.value).isEqualTo(MAX);
  }

  @Test
  void testAnd() throws Exception {
    Query query = new BooleanQuery.Builder()
        .add(DATE_QUERY, Occur.MUST)
        .add(BOB_TERM_QUERY, Occur.MUST)
        .build();

    TopDocs hits = searcher.search(query, 10);
    assertThat(hits.totalHits.value).isEqualTo(MAX / 2);
    assertThat(hits.scoreDocs)
        .extracting(scoreDoc -> searcher.storedFields().document(scoreDoc.doc).get("owner"))
        .containsOnly("bob");
  }

  @Test
  void testAndNot() throws Exception {
    Query query = new BooleanQuery.Builder()
        .add(DATE_QUERY, Occur.MUST)
        .add(BOB_TERM_QUERY, Occur.MUST_NOT)
        .build();

    TopDocs hits = searcher.search(query, 10);
    assertThat(hits.totalHits.value).isEqualTo(MAX / 2);
    assertThat(hits.scoreDocs)
        .extracting(scoreDoc -> searcher.storedFields().document(scoreDoc.doc).get("owner"))
        .containsOnly("sue");
  }
}
