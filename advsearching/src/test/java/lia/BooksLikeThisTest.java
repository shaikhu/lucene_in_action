package lia;

import lia.common.TestUtil;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static lia.common.TestUtil.documents;
import static org.assertj.core.api.Assertions.assertThat;

class BooksLikeThisTest {
  private static final String ANT_IN_ACTION_ISBN = "193239480X";

  private Directory directory;

  private DirectoryReader directoryReader;

  private IndexSearcher indexSearcher;

  private TopDocs results;

  @BeforeEach
  void setup() throws IOException {
    directory = TestUtil.getBookIndexDirectory();
    directoryReader = DirectoryReader.open(directory);
    indexSearcher = new IndexSearcher(directoryReader);
    results = indexSearcher.search(new TermQuery(new Term("isbn", ANT_IN_ACTION_ISBN)), 1);
  }

  @AfterEach
  void tearDown() throws IOException {
    directory.close();
  }

  @Test
  void testMoreLikeThis() throws IOException {
    var antInActionDocument = directoryReader.storedFields().document(results.scoreDocs[0].doc);

    var authorQueryBuilder = new BooleanQuery.Builder();
    for (var author : antInActionDocument.getValues("author")) {
      authorQueryBuilder.add(new BoostQuery(new TermQuery(new Term("author", author)), 2.0f), Occur.SHOULD);
    }

    var subjectQueryBuilder = new BooleanQuery.Builder();
    var terms = directoryReader.termVectors().get(results.scoreDocs[0].doc, "subject");
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

    results = indexSearcher.search(likeThisQuery, 10);
    List<String> titles = documents(indexSearcher, results).stream()
            .map(doc -> doc.get("title"))
            .toList();

    assertThat(titles).containsExactlyInAnyOrder(
            "Lucene in Action, Second Edition",
            "JUnit in Action, Second Edition",
            "Extreme Programming Explained");
  }
}
