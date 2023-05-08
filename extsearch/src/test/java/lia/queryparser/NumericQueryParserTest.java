package lia.queryparser;

import java.util.Locale;

import lia.common.TestUtil;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.document.DateTools;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class NumericQueryParserTest {
  private Directory directory;

  private Analyzer analyzer;

  private IndexSearcher searcher;

  @BeforeEach
  void setup() throws Exception {
    directory = TestUtil.getBookIndexDirectory();
    analyzer = new WhitespaceAnalyzer();
    searcher = new IndexSearcher(DirectoryReader.open(directory));
  }

  @AfterEach
  void tearDown() throws Exception {
    directory.close();
  }

  @Test
  void testNumericRangeQuery() throws Exception {
    String expression = "price:[10 TO 20]";
    QueryParser parser = new NumericRangeQueryParser("subject", analyzer);
    Query query = parser.parse(expression);
    assertThat(query).hasToString("price:[10.0 TO 20.0]");
 }

  @Test void testDefaultDateRangeQuery() throws Exception {
    QueryParser parser = new QueryParser("subject", analyzer);
    Query query = parser.parse("pubmonth:[1/1/04 TO 12/31/04]");
    assertThat(query).hasToString("pubmonth:[1/1/04 TO 12/31/04]");
  }

  @Test
  void testDateRangeQuery() throws Exception {
    String expression = "pubmonth:[01/01/2010 TO 06/01/2010]";

    QueryParser parser = new NumericDateRangeQueryParser("subject", analyzer);

    parser.setDateResolution("pubmonth", DateTools.Resolution.MONTH);
    parser.setLocale(Locale.US);
    Query query = parser.parse(expression);
    TopDocs matches = searcher.search(query, 10);
    assertThat(matches.totalHits.value).isNotZero();
  }
}
