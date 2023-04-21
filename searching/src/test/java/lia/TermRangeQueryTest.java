package lia;

import lia.common.TestUtil;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.TermRangeQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.util.BytesRef;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TermRangeQueryTest
{
  @Test
  void testTermRangeQuery() throws Exception {
    Directory directory = TestUtil.getBookIndexDirectory();
    IndexSearcher searcher = new IndexSearcher(DirectoryReader.open(directory));
    TermRangeQuery termRangeQuery = new TermRangeQuery("title2", new BytesRef("d"), new BytesRef("j"), true, true);
    TopDocs matches = searcher.search(termRangeQuery, 100);
    assertEquals(3, matches.totalHits.value);
  }
}
