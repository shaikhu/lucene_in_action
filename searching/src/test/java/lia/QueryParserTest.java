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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

    assertEquals("+kountry~2 title:western", query.toString("field"));
  }

  @Test
  void testTermQuery() throws Exception {
    QueryParser parser = new QueryParser("subject", analyzer);
    Query query = parser.parse("computers");
    assertEquals("subject:computers", query.toString());
  }

  @Test
  void testPrefixQuery() throws Exception {
    QueryParser parser = new QueryParser("category", new StandardAnalyzer());
    assertEquals(parser.parse("/computers/technology*").toString("category"), "/computers/ technology*");
  }

  @Test
  void testFuzzyQuery() throws Exception {
    QueryParser parser = new QueryParser("subject", analyzer);
    Query query = parser.parse("kountry~");
    assertEquals("subject:kountry~2", query.toString());

    query = parser.parse("kountry~0.7");
    assertEquals("subject:kountry~2", query.toString());
  }

  @Test
  void testGrouping() throws Exception {
    Query query = new QueryParser("subject", analyzer).parse("(agile OR extreme) AND methodology");
    TopDocs matches = searcher.search(query, 10);

    assertTrue(TestUtil.hitsIncludeTitle(searcher, matches, "Extreme Programming Explained"));
    assertTrue(TestUtil.hitsIncludeTitle(searcher, matches, "The Pragmatic Programmer"));
  }

  @Test
  void testPhraseQuery() throws Exception {
    Query query = new QueryParser("field", new StandardAnalyzer()).parse("\"This is Some Phrase*\"");
    assertEquals("\"this is some phrase\"", query.toString("field"));

    query = new QueryParser("field", analyzer).parse("\"term\"");
    assertInstanceOf(TermQuery.class, query);
  }

  @Test
  void testSlop() throws Exception {
    Query query = new QueryParser("field", analyzer).parse("\"exact phrase\"");
    assertEquals("\"exact phrase\"", query.toString("field"));

    QueryParser qp = new QueryParser("field", analyzer);
    qp.setPhraseSlop(5);
    query = qp.parse("\"sloppy phrase\"");
    assertEquals("\"sloppy phrase\"~5", query.toString("field"));
  }

  @Test
  void testLowercase() throws Exception {
    Query q = new QueryParser("field", analyzer).parse("PrefixQuery*");
    assertEquals("PrefixQuery*", q.toString("field"));

    QueryParser qp = new QueryParser("field", analyzer);
    q = qp.parse("PrefixQuery*");
    assertEquals("PrefixQuery*", q.toString("field"));
  }

  @Test
  void testWildcard() {
    assertThrows(ParseException.class, () -> new QueryParser("field", analyzer).parse("*xyz"));
  }

  @Test
  void testBoost() throws Exception {
    Query q = new QueryParser("field", analyzer).parse("term^2");
    assertEquals("(term)^2.0", q.toString("field"));
  }

  @Test
  void testParseException() {
    assertThrows(ParseException.class, () -> new QueryParser("contents", analyzer).parse("^&#"));
  }
}
