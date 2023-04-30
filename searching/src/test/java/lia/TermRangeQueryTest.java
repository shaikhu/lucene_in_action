package lia;

import lia.common.TestUtil;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.TermRangeQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.util.BytesRef;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class TermRangeQueryTest {
  @Test
  void testTermRangeQuery() throws Exception {
    try(Directory directory = TestUtil.getBookIndexDirectory()) {
      IndexSearcher searcher = new IndexSearcher(DirectoryReader.open(directory));
      TermRangeQuery termRangeQuery = new TermRangeQuery("title2", new BytesRef("d"), new BytesRef("j"), true, true);
      TopDocs matches = searcher.search(termRangeQuery, 100);
      assertThat(matches.totalHits.value).isEqualTo(3);
    }
  }
}
