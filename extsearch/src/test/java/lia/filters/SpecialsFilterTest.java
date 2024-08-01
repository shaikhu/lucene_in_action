package lia.filters;

import java.util.List;

import lia.common.TestUtil;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.WildcardQuery;
import org.apache.lucene.store.Directory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SpecialsFilterTest {
  private static final List<String> BOOKS_ON_SPECIAL_SALE = List.of("9780880105118");

  private Directory directory;

  private IndexSearcher indexSearcher;

  @BeforeEach
  void setUp() throws Exception {
    directory = TestUtil.getBookIndexDirectory();
    indexSearcher = new IndexSearcher(DirectoryReader.open(directory));
  }

  @AfterEach
  void tearDown() throws Exception {
    directory.close();
  }

  @Test
  void testFilter() throws Exception {
    // all books about logo
    var logoBooksQuery = new TermQuery(new Term("subject", "logo"));

    // all books on education
    var educationBooksQuery = new WildcardQuery(new Term("category", "*education*"));

    // all books which are on special sale
    var specialSaleQuery = new BooleanQuery.Builder();
    for (String isbn : BOOKS_ON_SPECIAL_SALE) {
      specialSaleQuery.add(new TermQuery(new Term("isbn", isbn)), Occur.SHOULD);
    }

    // all education books on special sale
    var educationBooksOnSpecialSaleQuery = new BooleanQuery.Builder()
        .add(educationBooksQuery, Occur.MUST)
        .add(specialSaleQuery.build(), Occur.FILTER)
        .build();

    // main query
    var booleanQuery = new BooleanQuery.Builder()
      .add(logoBooksQuery, Occur.SHOULD)
      .add(educationBooksOnSpecialSaleQuery, Occur.SHOULD)
      .build();

    var topDocs = indexSearcher.search(booleanQuery, 10);
    assertThat(topDocs.totalHits.value).isEqualTo(2);
  }
}
