package lia;

import lia.common.TestUtil;
import org.apache.lucene.document.LongPoint;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermRangeQuery;
import org.apache.lucene.store.Directory;
import org.apache.lucene.util.BytesRef;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FilterTest {
  private Directory directory;

  private IndexSearcher indexSearcher;

  private Query allBooksQuery;

  @BeforeEach
  void setup() throws Exception {
    allBooksQuery = new MatchAllDocsQuery();
    directory = TestUtil.getBookIndexDirectory();
    indexSearcher = new IndexSearcher(DirectoryReader.open(directory));
  }

  @AfterEach
  void tearDown() throws Exception {
    directory.close();
  }

  @Test
  void testQueryFiltering() throws Exception {
    var query = new BooleanQuery.Builder()
        .add(allBooksQuery, Occur.MUST)
        .add(new TermRangeQuery("title2", new BytesRef("d"), new BytesRef("j"), true, true), Occur.FILTER)
        .build();

    assertThat(TestUtil.hitCount(indexSearcher, query)).isEqualTo(3);
  }

  @Test
  void testNumericDateFilter() throws Exception {
    var query = new BooleanQuery.Builder()
        .add(allBooksQuery, Occur.MUST)
        .add(LongPoint.newRangeQuery("pubmonth", 201001, 201006), Occur.FILTER)
        .build();

    assertThat(TestUtil.hitCount(indexSearcher, query)).isEqualTo(2);
  }

  @Test
  void testStringRangeFilter() throws Exception {
    var query = new BooleanQuery.Builder()
        .add(allBooksQuery, Occur.MUST)
        .add(TermRangeQuery.newStringRange("title2", "d", "j", true, true), Occur.FILTER)
        .build();

    assertThat(TestUtil.hitCount(indexSearcher, query)).isEqualTo(3);
  }

  @Test
  void testPrefixFilter() throws Exception {
    var prefixQuery = new PrefixQuery(new Term("category", "/technology/computers"));
    var booleanQuery = new BooleanQuery.Builder()
        .add(allBooksQuery, Occur.MUST)
        .add(prefixQuery, Occur.FILTER)
        .build();

    assertThat(TestUtil.hitCount(indexSearcher, booleanQuery)).isEqualTo(8);
  }
}
