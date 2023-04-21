package lia;

import lia.common.TestUtil;
import org.apache.lucene.document.LongPoint;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class NumberRangeQueryTest
{
  @Test
  void testInclusive() throws Exception {
    Directory directory = TestUtil.getBookIndexDirectory();
    IndexSearcher searcher = new IndexSearcher(DirectoryReader.open(directory));
    Query query = LongPoint.newRangeQuery("pubmonth", 200605, 200609);
    TopDocs matches = searcher.search(query, 10);
    assertEquals(1, matches.totalHits.value);
  }

  @Test
  void testExclusive() throws Exception {
    Directory directory = TestUtil.getBookIndexDirectory();
    IndexSearcher searcher = new IndexSearcher(DirectoryReader.open(directory));
    Query query = LongPoint.newRangeQuery("pubmonth", Math.addExact(200605, 1), Math.addExact(200609, -1));
    TopDocs matches = searcher.search(query, 10);
    assertEquals(0, matches.totalHits.value);
  }
}
