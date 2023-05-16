package lia.collector;

import java.util.Map;

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
    TermQuery query = new TermQuery(new Term("contents", "junit"));

    try (DirectoryReader reader = DirectoryReader.open(directory)) {
      IndexSearcher searcher = new IndexSearcher(reader);
      BookLinkCollector collector = new BookLinkCollector(reader.storedFields());
      searcher.search(query, collector);
      Map<String,String> links = collector.getLinks();
      assertThat(links).containsEntry("http://www.manning.com/loughran", "ant in action");
    }
  }
}
