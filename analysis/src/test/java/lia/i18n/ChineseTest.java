package lia.i18n;

import lia.common.TestUtil;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.TermQuery;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ChineseTest {
  @Test
  void testChinese() throws Exception {
    try (var directory = TestUtil.getBookIndexDirectory()) {
      var indexSearcher = new IndexSearcher(DirectoryReader.open(directory));
      var query = new TermQuery(new Term("contents", "\u9053"));
      assertThat(TestUtil.hitCount(indexSearcher, query)).isOne();
    }
  }
}