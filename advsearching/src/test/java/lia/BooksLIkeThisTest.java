package lia;

import lia.common.TestUtil;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.BoostQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.util.BytesRef;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class BooksLIkeThisTest {
  private Directory directory;

  private DirectoryReader reader;

  private IndexSearcher searcher;

  private TopDocs hits;

  @BeforeEach
  void setup() throws Exception {
    directory = TestUtil.getBookIndexDirectory();
    reader = DirectoryReader.open(directory);
    searcher = new IndexSearcher(reader);

    Term term = new Term("isbn", "193239480X");
    Query query = new TermQuery(term);
    hits = searcher.search(query, 1);
  }

  @AfterEach
  void tearDown() throws Exception {
    directory.close();
  }

  @Test
  void testMoreLikeThis() throws Exception {
    Document document = reader.storedFields().document(hits.scoreDocs[0].doc);

    BooleanQuery.Builder authorQueryBuilder = new BooleanQuery.Builder();
    for (String author : document.getValues("author")) {
      authorQueryBuilder.add(new BoostQuery(new TermQuery(new Term("author", author)), 2.0f), Occur.SHOULD);
    }

    BooleanQuery.Builder subjectQueryBuilder = new BooleanQuery.Builder();
    Terms terms = reader.termVectors().get(hits.scoreDocs[0].doc, "subject");
    TermsEnum enumIterator = terms.iterator();
    BytesRef bytesRef = enumIterator.next();
    while (bytesRef != null) {
      subjectQueryBuilder.add(new TermQuery(new Term("subject", bytesRef.utf8ToString())), Occur.SHOULD);
      bytesRef = enumIterator.next();
    }

    BooleanQuery likeThisQuery = new BooleanQuery.Builder()
        .add(authorQueryBuilder.build(), Occur.SHOULD)
        .add(subjectQueryBuilder.build(), Occur.SHOULD)
        .add(new TermQuery(new Term("isbn", document.get("isbn"))), Occur.MUST_NOT)
        .build();

    TopDocs matches = searcher.search(likeThisQuery, 10);
    assertThat(matches.scoreDocs)
        .extracting(scoreDoc -> searcher.storedFields().document(scoreDoc.doc).get("title"))
        .containsOnly(
            "Lucene in Action, Second Edition",
            "JUnit in Action, Second Edition",
            "Extreme Programming Explained");
  }
}
