package lia;

import lia.common.TestUtil;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermRangeQuery;
import org.apache.lucene.store.Directory;
import org.apache.lucene.util.BytesRef;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class FilterTest {
  private Query allBooks;

  private IndexSearcher searcher;

  private Directory directory;

  @BeforeEach
  void setup() throws Exception {
    allBooks = new MatchAllDocsQuery();
    directory = TestUtil.getBookIndexDirectory();
    searcher = new IndexSearcher(DirectoryReader.open(directory));
  }

  @AfterEach
  void tearDown() throws Exception {
    directory.close();
  }

  @Test
  void testQueryFiltering() throws Exception {
    BooleanQuery query = new BooleanQuery.Builder()
        .add(allBooks, Occur.MUST)
        .add(new TermRangeQuery("title2", new BytesRef("d"), new BytesRef("j"), true, true), Occur.FILTER)
        .build();

    assertThat(TestUtil.hitCount(searcher, query)).isEqualTo(3);
  }
}
