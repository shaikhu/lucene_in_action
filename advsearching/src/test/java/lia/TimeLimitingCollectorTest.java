package lia;

import lia.common.TestUtil;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TimeLimitingCollector;
import org.apache.lucene.search.TimeLimitingCollector.TimeExceededException;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.Directory;
import org.apache.lucene.util.Counter;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class TimeLimitingCollectorTest 
{
  @Test
  void testTimeLimitingCollector() throws Exception {
    Directory dir = TestUtil.getBookIndexDirectory();
    IndexSearcher searcher = new IndexSearcher(DirectoryReader.open(dir));
    Query q = new MatchAllDocsQuery();
    int numAllBooks = (int) TestUtil.hitCount(searcher, q);

    TopScoreDocCollector topDocs = TopScoreDocCollector.create(10, numAllBooks);
    Collector collector = new TimeLimitingCollector(topDocs, Counter.newCounter(), 1000);
    try {
      searcher.search(q, collector);
      assertThat(topDocs.getTotalHits()).isEqualTo(numAllBooks);
    } catch (TimeExceededException e) {
      throw new RuntimeException("Too much time taken.", e);
    }
    dir.close();
  }
}
