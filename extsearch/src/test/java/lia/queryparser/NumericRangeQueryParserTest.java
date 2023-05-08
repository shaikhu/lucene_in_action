package lia.queryparser;

import lia.common.TestUtil;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.store.Directory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class NumericRangeQueryParserTest {
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
}