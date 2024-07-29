package lia;

import lia.common.TestUtil;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.FuzzyQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.store.Directory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class QueryParserTest {
  private Analyzer analyzer;

  private Directory directory;

  private IndexSearcher indexSearcher;

  @BeforeEach
  void setup() throws Exception {
    analyzer = new WhitespaceAnalyzer();
    directory = TestUtil.getBookIndexDirectory();
    indexSearcher = new IndexSearcher(DirectoryReader.open(directory));
  }

  @AfterEach
  void tearDown() throws Exception {
    directory.close();
  }

  @Test
  void testToString() {
    var booleanQuery = new BooleanQuery.Builder()
        .add(new FuzzyQuery(new Term("field", "kountry")), Occur.MUST)
        .add(new TermQuery(new Term("title", "western")), Occur.SHOULD)
        .build();

    assertThat(booleanQuery.toString("field")).isEqualTo("+kountry~2 title:western");
  }

  @Test
  void testTermQuery() throws Exception {
    var queryParser = new QueryParser("subject", analyzer);
    var query = queryParser.parse("computers");
    assertThat(query).hasToString("subject:computers");
  }

  @Test
  void testPrefixQuery() throws Exception {
    var queryParser = new QueryParser("category", new StandardAnalyzer());
    assertThat(queryParser.parse("/computers/technology*").toString("category")).isEqualTo("/computers/ technology*");
  }

  @Test
  void testFuzzyQuery() throws Exception {
    var queryParser = new QueryParser("subject", analyzer);
    var query = queryParser.parse("kountry~");
    assertThat(query).hasToString("subject:kountry~2");

    query = queryParser.parse("kountry~0.7");
    assertThat(query).hasToString("subject:kountry~2");
  }

  @Test
  void testGrouping() throws Exception {
    var query = new QueryParser("subject", analyzer).parse("(agile OR extreme) AND methodology");
    var topDocs = indexSearcher.search(query, 10);

    assertThat(TestUtil.hitsIncludeTitle(indexSearcher, topDocs, "Extreme Programming Explained")).isTrue();
    assertThat(TestUtil.hitsIncludeTitle(indexSearcher, topDocs, "The Pragmatic Programmer")).isTrue();
  }

  @Test
  void testPhraseQuery() throws Exception {
    var query = new QueryParser("field", new StandardAnalyzer()).parse("\"This is Some Phrase*\"");
    assertThat(query.toString("field")).isEqualTo("\"this is some phrase\"");

    query = new QueryParser("field", analyzer).parse("\"term\"");
    assertThat(query).isInstanceOf(TermQuery.class);
  }

  @Test
  void testSlop() throws Exception {
    var query = new QueryParser("field", analyzer).parse("\"exact phrase\"");
    assertThat(query.toString("field")).isEqualTo("\"exact phrase\"");

    var queryParser = new QueryParser("field", analyzer);
    queryParser.setPhraseSlop(5);
    query = queryParser.parse("\"sloppy phrase\"");
    assertThat(query.toString("field")).isEqualTo("\"sloppy phrase\"~5");
  }

  @Test
  void testLowercase() throws Exception {
    Query query = new QueryParser("field", analyzer).parse("PrefixQuery*");
    assertThat(query.toString("field")).isEqualTo("PrefixQuery*");

    var queryParser = new QueryParser("field", analyzer);
    query = queryParser.parse("PrefixQuery*");
    assertThat(query.toString("field")).isEqualTo("PrefixQuery*");
  }

  @Test
  void testWildcard() {
    assertThatExceptionOfType(ParseException.class).isThrownBy(() -> new QueryParser("field", analyzer).parse("*xyz"));
  }

  @Test
  void testBoost() throws Exception {
    var query = new QueryParser("field", analyzer).parse("term^2");
    assertThat(query.toString("field")).isEqualTo("(term)^2.0");
  }

  @Test
  void testParseException() {
    assertThatExceptionOfType(ParseException.class).isThrownBy(
        () -> new QueryParser("contents", analyzer).parse("^&#"));
  }
}
