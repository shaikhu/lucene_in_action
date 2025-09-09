package lia;

import lia.common.TestUtil;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.*;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.store.Directory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

class BooksLikeThisTest {
  private static final String ANT_IN_ACTION_ISBN = "193239480X";

  private Directory directory;

  private DirectoryReader directoryReader;

  private IndexSearcher indexSearcher;

  private TopDocs topDocs;

  @BeforeEach
  void setup() throws Exception {
    directory = TestUtil.getBookIndexDirectory();
    directoryReader = DirectoryReader.open(directory);
    indexSearcher = new IndexSearcher(directoryReader);
    topDocs = indexSearcher.search(new TermQuery(new Term("isbn", ANT_IN_ACTION_ISBN)), 1);
  }

  @AfterEach
  void tearDown() throws Exception {
    directory.close();
  }

  @Test
  void testMoreLikeThis() throws Exception {
    var antInActionDocument = directoryReader.storedFields().document(topDocs.scoreDocs[0].doc);

    var authorQueryBuilder = new BooleanQuery.Builder();
    for (var author : antInActionDocument.getValues("author")) {
      authorQueryBuilder.add(new BoostQuery(new TermQuery(new Term("author", author)), 2.0f), Occur.SHOULD);
    }

    var subjectQueryBuilder = new BooleanQuery.Builder();
    var terms = directoryReader.termVectors().get(topDocs.scoreDocs[0].doc, "subject");
    var termsEnum = terms.iterator();
    var bytesRef = terms.iterator().next();
    while (bytesRef != null) {
      subjectQueryBuilder.add(new TermQuery(new Term("subject", bytesRef.utf8ToString())), Occur.SHOULD);
      bytesRef = termsEnum.next();
    }

    var likeThisQuery = new BooleanQuery.Builder()
        .add(authorQueryBuilder.build(), Occur.SHOULD)
        .add(subjectQueryBuilder.build(), Occur.SHOULD)
        .add(new TermQuery(new Term("isbn", ANT_IN_ACTION_ISBN)), Occur.MUST_NOT)
        .build();

    topDocs = indexSearcher.search(likeThisQuery, 10);
    assertThat(topDocs.scoreDocs)
        .extracting(this::mapToTitle)
        .containsOnly("Lucene in Action, Second Edition", "JUnit in Action, Second Edition", "Extreme Programming Explained");
  }

  private String mapToTitle(ScoreDoc scoreDoc) {
    try {
      return indexSearcher.storedFields().document(scoreDoc.doc).get("title");
    } catch (IOException e) {
      throw new RuntimeException("Failed to retrieve title for document " + scoreDoc.doc, e);
    }
  }
}
