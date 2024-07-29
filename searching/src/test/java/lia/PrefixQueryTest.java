package lia;

import lia.common.TestUtil;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.search.TermQuery;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PrefixQueryTest {
  @Test
  void testPrefix() throws Exception {
    try (var directory = TestUtil.getBookIndexDirectory()) {
      var indexSearcher = new IndexSearcher(DirectoryReader.open(directory));

      var term = new Term("category", "/technology/computers/programming");
      var prefixQuery = new PrefixQuery(term);
      var termQuery = new TermQuery(term);

      var numberOfProgrammingBooksAndSubcategories = indexSearcher.search(prefixQuery, 10).totalHits.value;
      var numberOfProgrammingBooks = indexSearcher.search(termQuery, 10).totalHits.value;
      assertThat(numberOfProgrammingBooksAndSubcategories).isGreaterThan(numberOfProgrammingBooks);
    }
  }
}
