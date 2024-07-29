package lia;

import lia.common.TestUtil;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.TermRangeQuery;
import org.apache.lucene.util.BytesRef;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TermRangeQueryTest {
  @Test
  void testTermRangeQuery() throws Exception {
    try(var directory = TestUtil.getBookIndexDirectory()) {
      var indexSearcher = new IndexSearcher(DirectoryReader.open(directory));
      var termRangeQuery = new TermRangeQuery("title2", new BytesRef("d"), new BytesRef("j"), true, true);
      var topDocs = indexSearcher.search(termRangeQuery, 100);
      assertThat(topDocs.totalHits.value).isEqualTo(3);
    }
  }
}
