package lia.filters;

import java.util.List;

import lia.common.TestUtil;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.WildcardQuery;
import org.apache.lucene.store.Directory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class SpecialsFilterTest {
  private Directory directory;

  private IndexSearcher searcher;

  @BeforeEach
  void setUp() throws Exception {
    directory = TestUtil.getBookIndexDirectory();
    searcher = new IndexSearcher(DirectoryReader.open(directory));
  }

  @AfterEach
  void tearDown() throws Exception {
    directory.close();
  }

  @Test
  void testFilter() throws Exception {
    // build clause 1 - all books about logo
    TermQuery logoBooks = new TermQuery(new Term("subject", "logo"));

    // build clause 2 - all books on education which are on special sale
    // all education books
    WildcardQuery educationBooks = new WildcardQuery(new Term("category", "*education*"));

    // all books on special sale
    SpecialsAccessor accessor = new TestSpecialsAccessor(List.of("9780880105118"));
    BooleanQuery.Builder specialSale = new BooleanQuery.Builder();
    for (String isbn : accessor.getIsbns()) {
      specialSale.add(new TermQuery(new Term("isbn", isbn)), Occur.SHOULD);
    }

    // all education books on special sale
    BooleanQuery educationBooksOnSpecial = new BooleanQuery.Builder()
        .add(educationBooks, Occur.MUST)
        .add(specialSale.build(), Occur.FILTER)
        .build();

    // main query
    BooleanQuery.Builder query = new BooleanQuery.Builder();
    query.add(logoBooks, Occur.SHOULD);
    query.add(educationBooksOnSpecial, Occur.SHOULD);

    TopDocs hits = searcher.search(query.build(), 10);
    assertThat(hits.totalHits.value).isEqualTo(2);
  }
}
