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
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

public class QueryParserTest
{
  private Analyzer analyzer;

  private Directory directory;

  private IndexSearcher searcher;

  @BeforeEach
  void setup() throws Exception {
    analyzer = new WhitespaceAnalyzer();
    directory = TestUtil.getBookIndexDirectory();
    searcher = new IndexSearcher(DirectoryReader.open(directory));
  }

  @AfterEach
  void tearDown() throws Exception {
    directory.close();
  }

  @Test
  void testToString() {
    BooleanQuery query = new BooleanQuery.Builder()
        .add(new FuzzyQuery(new Term("field", "kountry")), Occur.MUST)
        .add(new TermQuery(new Term("title", "western")), Occur.SHOULD)
        .build();

    assertThat(query.toString("field")).isEqualTo("+kountry~2 title:western");
  }

  @Test
  void testTermQuery() throws Exception {
    QueryParser parser = new QueryParser("subject", analyzer);
    Query query = parser.parse("computers");
    assertThat(query).hasToString("subject:computers");
  }

  @Test
  void testPrefixQuery() throws Exception {
    QueryParser parser = new QueryParser("category", new StandardAnalyzer());
    assertThat(parser.parse("/computers/technology*").toString("category")).isEqualTo("/computers/ technology*");
  }

  @Test
  void testFuzzyQuery() throws Exception {
    QueryParser parser = new QueryParser("subject", analyzer);
    Query query = parser.parse("kountry~");
    assertThat(query).hasToString("subject:kountry~2");

    query = parser.parse("kountry~0.7");
    assertThat(query).hasToString("subject:kountry~2");
  }

  @Test
  void testGrouping() throws Exception {
    Query query = new QueryParser("subject", analyzer).parse("(agile OR extreme) AND methodology");
    TopDocs matches = searcher.search(query, 10);

    assertThat(TestUtil.hitsIncludeTitle(searcher, matches, "Extreme Programming Explained")).isTrue();
    assertThat(TestUtil.hitsIncludeTitle(searcher, matches, "The Pragmatic Programmer")).isTrue();
  }

  @Test
  void testPhraseQuery() throws Exception {
    Query query = new QueryParser("field", new StandardAnalyzer()).parse("\"This is Some Phrase*\"");
    assertThat(query.toString("field")).isEqualTo("\"this is some phrase\"");

    query = new QueryParser("field", analyzer).parse("\"term\"");
    assertThat(query).isInstanceOf(TermQuery.class);
  }

  @Test
  void testSlop() throws Exception {
    Query query = new QueryParser("field", analyzer).parse("\"exact phrase\"");
    assertThat(query.toString("field")).isEqualTo("\"exact phrase\"");

    QueryParser qp = new QueryParser("field", analyzer);
    qp.setPhraseSlop(5);
    query = qp.parse("\"sloppy phrase\"");
    assertThat(query.toString("field")).isEqualTo("\"sloppy phrase\"~5");
  }

  @Test
  void testLowercase() throws Exception {
    Query q = new QueryParser("field", analyzer).parse("PrefixQuery*");
    assertThat(q.toString("field")).isEqualTo("PrefixQuery*");

    QueryParser qp = new QueryParser("field", analyzer);
    q = qp.parse("PrefixQuery*");
    assertThat(q.toString("field")).isEqualTo("PrefixQuery*");
  }

  @Test
  void testWildcard() {
    assertThatExceptionOfType(ParseException.class).isThrownBy(() -> new QueryParser("field", analyzer).parse("*xyz"));
  }

  @Test
  void testBoost() throws Exception {
    Query q = new QueryParser("field", analyzer).parse("term^2");
    assertThat(q.toString("field")).isEqualTo("(term)^2.0");
  }

  @Test
  void testParseException() {
    assertThatExceptionOfType(ParseException.class).isThrownBy(
        () -> new QueryParser("contents", analyzer).parse("^&#"));
  }
}
