package lia;

import lia.common.TestUtil;
import org.apache.lucene.document.LongPoint;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Directory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class NumberRangeQueryTest {
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
  void testInclusive() throws Exception {
    var query = LongPoint.newRangeQuery("pubmonth", 200605, 200609);
    var topDocs = indexSearcher.search(query, 10);
    assertThat(topDocs.totalHits.value()).isOne();
  }

  @Test
  void testExclusive() throws Exception {
    var query = LongPoint.newRangeQuery("pubmonth", Math.addExact(200605, 1), Math.addExact(200609, -1));
    var topDocs = indexSearcher.search(query, 10);
    assertThat(topDocs.totalHits.value()).isZero();
  }
}
