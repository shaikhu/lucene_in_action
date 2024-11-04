package lia.queryparser;

import java.util.Locale;

import lia.common.TestUtil;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.document.DateTools;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Directory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class NumericQueryParserTest {
  private Directory directory;

  private Analyzer analyzer;

  private IndexSearcher indexSearcher;

  @BeforeEach
  void setup() throws Exception {
    directory = TestUtil.getBookIndexDirectory();
    analyzer = new WhitespaceAnalyzer();
    indexSearcher = new IndexSearcher(DirectoryReader.open(directory));
  }

  @AfterEach
  void tearDown() throws Exception {
    directory.close();
  }

  @Test
  void testNumericRangeQuery() throws Exception {
    var queryParser = new NumericRangeQueryParser("subject", analyzer);
    var query = queryParser.parse("price:[10 TO 20]");
    assertThat(query).hasToString("price:[10.0 TO 20.0]");
 }

  @Test void testDefaultDateRangeQuery() throws Exception {
    var queryParser = new QueryParser("subject", analyzer);
    var query = queryParser.parse("pubmonth:[1/1/04 TO 12/31/04]");
    assertThat(query).hasToString("pubmonth:[1/1/04 TO 12/31/04]");
  }

  @Test
  void testDateRangeQuery() throws Exception {
    var queryParser = new NumericDateRangeQueryParser("subject", analyzer);
    queryParser.setDateResolution("pubmonth", DateTools.Resolution.MONTH);
    queryParser.setLocale(Locale.US);
    var query = queryParser.parse("pubmonth:[01/01/2010 TO 06/01/2010]");
    var topDocs = indexSearcher.search(query, 10);
    assertThat(topDocs.totalHits.value()).isNotZero();
  }
}
