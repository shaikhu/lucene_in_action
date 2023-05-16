package lia;

import lia.common.TestUtil;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queries.mlt.MoreLikeThis;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BooksMoreLikeThisTest {
  private static final String LUCENE_IN_ACTION_TITLE = "Lucene in Action, Second Edition";

  private static final String LUCENE_IN_ACTION_ISBN = "1933988177";

  private Directory directory;

  private DirectoryReader reader;

  private IndexSearcher searcher;

  private TopDocs luceneInAction;

  @BeforeEach
  void setup() throws Exception {
    directory = TestUtil.getBookIndexDirectory();
    reader = DirectoryReader.open(directory);
    searcher = new IndexSearcher(reader);
    luceneInAction = searcher.search(new TermQuery(new Term("isbn", LUCENE_IN_ACTION_ISBN)), 1);
  }

  @AfterEach
  void tearDown() throws  Exception {
    reader.close();
    directory.close();
  }

  @Test
  void testMoreLikeThis() throws Exception {
    MoreLikeThis mlt = new MoreLikeThis(reader);
    mlt.setFieldNames(new String[]{"title", "author"});
    mlt.setMinTermFreq(1);
    mlt.setMinDocFreq(1);

    Query query = mlt.like(luceneInAction.scoreDocs[0].doc);
    TopDocs similarDocs = searcher.search(query, 10);
    assertThat(similarDocs.scoreDocs)
        .extracting(scoreDoc -> searcher.storedFields().document(scoreDoc.doc).get("title"))
        .filteredOn(title -> !title.equals(LUCENE_IN_ACTION_TITLE))
        .containsOnly(
            "JUnit in Action, Second Edition",
            "Ant in Action",
            "Tapestry in Action");
  }
}
