package lia.i18n;

import lia.common.TestUtil;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.store.Directory;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ChineseTest {
  @Test
  void testChinese() throws Exception {
    try (Directory dir = TestUtil.getBookIndexDirectory()) {
      IndexSearcher searcher = new IndexSearcher(DirectoryReader.open(dir));
      Query query = new TermQuery(new Term("contents", "道"));
      assertThat(TestUtil.hitCount(searcher, query)).isOne();
    }
  }
}