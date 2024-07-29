package lia;

import lia.common.TestUtil;
import org.apache.lucene.document.LongPoint;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.store.Directory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BooleanQueryTest {
  private Directory directory;

  private IndexSearcher indexSearcher;

  @BeforeEach
  void setup() throws Exception {
    directory = TestUtil.getBookIndexDirectory();
    indexSearcher = new IndexSearcher(DirectoryReader.open(directory));
  }

  @AfterEach
  void tearDown() throws Exception {
    directory.close();
  }

  @Test
  void testAnd() throws Exception {
    var bookQuery = new TermQuery(new Term("subject", "search"));
    var yearQuery = LongPoint.newRangeQuery("pubmonth", 201001, 201012);

    var booleanQuery = new BooleanQuery.Builder()
        .add(new BooleanClause(bookQuery, Occur.MUST))
        .add(new BooleanClause(yearQuery, Occur.MUST))
        .build();

    directory = TestUtil.getBookIndexDirectory();
    indexSearcher = new IndexSearcher(DirectoryReader.open(directory));
    var topDocs = indexSearcher.search(booleanQuery, 10);
    assertThat(TestUtil.hitsIncludeTitle(indexSearcher, topDocs, "Lucene in Action, Second Edition")).isTrue();
  }

  @Test
  void testOr() throws Exception {
    var methodologyBooksQuery = new TermQuery(new Term("category", "/technology/computers/programming/methodology"));
    var easternPhilosophyBooksQuery = new TermQuery(new Term("category", "/philosophy/eastern"));

    var booleanQuery = new BooleanQuery.Builder()
        .add(methodologyBooksQuery, Occur.SHOULD)
        .add(easternPhilosophyBooksQuery, Occur.SHOULD)
        .build();

    indexSearcher = new IndexSearcher(DirectoryReader.open(directory));
    var topDocs = indexSearcher.search(booleanQuery, 100);

    assertThat(TestUtil.hitsIncludeTitle(indexSearcher, topDocs, "Extreme Programming Explained")).isTrue();
    assertThat(TestUtil.hitsIncludeTitle(indexSearcher, topDocs, "Tao Te Ching \u9053\u5FB7\u7D93")).isTrue();
  }
}
