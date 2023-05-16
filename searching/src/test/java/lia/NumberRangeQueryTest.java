package lia;

import lia.common.TestUtil;
import org.apache.lucene.document.LongPoint;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class NumberRangeQueryTest {
  private Directory directory;

  private IndexSearcher searcher;

  @BeforeEach
  void setup() throws Exception {
    directory = TestUtil.getBookIndexDirectory();
    searcher = new IndexSearcher(DirectoryReader.open(directory));
  }

  @AfterEach
  void tearDown() throws Exception {
    directory.close();
  }

  @Test
  void testInclusive() throws Exception {
    Query query = LongPoint.newRangeQuery("pubmonth", 200605, 200609);
    TopDocs matches = searcher.search(query, 10);
    assertThat(matches.totalHits.value).isOne();
  }

  @Test
  void testExclusive() throws Exception {
    Query query = LongPoint.newRangeQuery("pubmonth", Math.addExact(200605, 1), Math.addExact(200609, -1));
    TopDocs matches = searcher.search(query, 10);
    assertThat(matches.totalHits.value).isZero();
  }
}
