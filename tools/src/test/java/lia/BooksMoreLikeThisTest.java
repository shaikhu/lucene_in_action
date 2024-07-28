package lia;

import lia.common.TestUtil;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queries.mlt.MoreLikeThis;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Objects;
import java.util.function.Predicate;

import static org.assertj.core.api.Assertions.assertThat;

class BooksMoreLikeThisTest {
  private static final String LUCENE_IN_ACTION_TITLE = "Lucene in Action, Second Edition";

  private static final String LUCENE_IN_ACTION_ISBN = "1933988177";

  private static final Predicate<String> NOT_LUCENE_IN_ACTION = title -> !Objects.equals(title, LUCENE_IN_ACTION_TITLE);

  private Directory directory;

  private DirectoryReader directoryReader;

  private IndexSearcher indexSearcher;

  private TopDocs topDocs;

  @BeforeEach
  void setup() throws Exception {
    directory = TestUtil.getBookIndexDirectory();
    directoryReader = DirectoryReader.open(directory);
    indexSearcher = new IndexSearcher(directoryReader);
    topDocs = indexSearcher.search(new TermQuery(new Term("isbn", LUCENE_IN_ACTION_ISBN)), 1);
  }

  @AfterEach
  void tearDown() throws  Exception {
    directoryReader.close();
    directory.close();
  }

  @Test
  void testMoreLikeThis() throws Exception {
    var moreLikeThisQuery = new MoreLikeThis(directoryReader);
    moreLikeThisQuery.setFieldNames(new String[]{"title", "author"});
    moreLikeThisQuery.setMinTermFreq(1);
    moreLikeThisQuery.setMinDocFreq(1);

    var query = moreLikeThisQuery.like(topDocs.scoreDocs[0].doc);
    var similarDocs = indexSearcher.search(query, 10);
    assertThat(similarDocs.scoreDocs)
        .extracting(scoreDoc -> indexSearcher.storedFields().document(scoreDoc.doc).get("title"))
        .filteredOn(NOT_LUCENE_IN_ACTION)
        .containsOnly(
            "JUnit in Action, Second Edition",
            "Ant in Action",
            "Tapestry in Action");
  }
}
