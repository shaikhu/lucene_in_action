package lia;

import lia.common.TestUtil;
import org.apache.lucene.index.ExitableDirectoryReader;
import org.apache.lucene.index.QueryTimeoutImpl;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.TopScoreDocCollectorManager;
import org.apache.lucene.store.Directory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TimeLimitCollectorTest {
  private Directory directory;

  @BeforeEach
  void setup() throws Exception {
    directory = TestUtil.getBookIndexDirectory();
  }

  @AfterEach
  void tearDown() throws Exception {
    directory.close();
  }

  @Test
  void testTimeoutLimitCollector() throws Exception {
    try (var directoryReader = ExitableDirectoryReader.open(directory)) {
      var indexSearcher = new IndexSearcher(directoryReader);
      indexSearcher.setTimeout(new QueryTimeoutImpl(1000));

      var allBooksQuery = new MatchAllDocsQuery();
      var numAllBooks = (int) TestUtil.hitCount(indexSearcher, allBooksQuery);

      var collectorManager = new TopScoreDocCollectorManager(10, numAllBooks);
      var topDocs = indexSearcher.search(allBooksQuery, collectorManager);
      assertThat(topDocs.totalHits.value).isEqualTo(numAllBooks);
    }
  }
}
