package lia;

import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.RegexpQuery;
import org.junit.jupiter.api.Test;

import static lia.common.TestUtil.getBookIndexDirectory;
import static lia.common.TestUtil.hitsIncludeTitle;
import static org.assertj.core.api.Assertions.assertThat;

class RegexQueryTest {
  @Test
  void testRegexQuery() throws Exception {
    try (var directory = getBookIndexDirectory()) {
      var indexSearcher = new IndexSearcher(DirectoryReader.open(directory));
      var query = new RegexpQuery(new Term("title", ".*st.*"));
      var topDocs = indexSearcher.search(query, 10);

      assertThat(topDocs.totalHits.value).isEqualTo(2);
      assertThat(hitsIncludeTitle(indexSearcher, topDocs, "Tapestry in Action")).isTrue();
      assertThat(hitsIncludeTitle(indexSearcher, topDocs, "Mindstorms: Children, Computers, And Powerful Ideas")).isTrue();
    }
  }
}
