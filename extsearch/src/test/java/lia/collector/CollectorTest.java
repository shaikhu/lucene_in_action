package lia.collector;

import lia.common.TestUtil;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.store.Directory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CollectorTest {
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
  void testCollecting() throws Exception {
    var query = new TermQuery(new Term("contents", "junit"));

    try (var directoryReader = DirectoryReader.open(directory)) {
      var indexSearcher = new IndexSearcher(directoryReader);
      var links = indexSearcher.search(query, new BookLinkCollectorManager(directoryReader.storedFields()));
      assertThat(links).containsEntry("http://www.manning.com/loughran", "ant in action");
    }
  }
}
