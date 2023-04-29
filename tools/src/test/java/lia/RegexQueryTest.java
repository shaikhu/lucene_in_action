package lia;

import lia.common.TestUtil;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.RegexpQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class RegexQueryTest
{
  @Test
  void testRegexQuery() throws Exception {
    Directory directory = TestUtil.getBookIndexDirectory();
    IndexSearcher searcher = new IndexSearcher(DirectoryReader.open(directory));
    RegexpQuery query = new RegexpQuery(new Term("title", ".*st.*"));
    TopDocs hits = searcher.search(query, 10);
    assertThat(hits.totalHits.value).isEqualTo(2);
    assertThat(TestUtil.hitsIncludeTitle(searcher, hits, "Tapestry in Action")).isTrue();
    assertThat(
        TestUtil.hitsIncludeTitle(searcher, hits, "Mindstorms: Children, Computers, And Powerful Ideas")).isTrue();
    directory.close();
  }
}
