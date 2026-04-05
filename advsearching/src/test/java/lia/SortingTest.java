package lia;

import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import lia.common.TestUtil;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.*;
import org.apache.lucene.search.SortField.Type;
import org.apache.lucene.store.Directory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static java.util.Comparator.comparing;
import static lia.common.TestUtil.documents;
import static org.assertj.core.api.Assertions.assertThat;

class SortingTest {
  private static BooleanQuery booleanQuery;

  private Directory directory;

  private IndexSearcher indexSearcher;

  @BeforeAll
  static void initQuery() throws ParseException {
    var javaBook = new QueryParser("contents", new StandardAnalyzer()).parse("java OR action");

    booleanQuery = new BooleanQuery.Builder()
        .add(new MatchAllDocsQuery(), Occur.SHOULD)
        .add(javaBook, Occur.SHOULD)
        .build();
  }

  @BeforeEach
  void setup() throws IOException {
    directory = TestUtil.getBookIndexDirectory();
    indexSearcher = new IndexSearcher(DirectoryReader.open(directory));
  }

  @AfterEach
  void tearDown() throws IOException {
    directory.close();
  }

  @Test
  void testSortByRelevance() throws IOException {
    TopDocs results = indexSearcher.search(booleanQuery, 10, Sort.RELEVANCE, true);

    List<Float> scores = Arrays.stream(results.scoreDocs)
            .map(scoreDoc -> scoreDoc.score)
            .toList();

    assertThat(scores).isSortedAccordingTo(Comparator.reverseOrder());
  }

  @Test
  void testSortByIndexOrder() throws IOException {
    TopDocs results = indexSearcher.search(booleanQuery, 10, Sort.INDEXORDER, true);

    List<Integer> documentNumbers = Arrays.stream(results.scoreDocs)
            .map(scoreDoc -> scoreDoc.doc)
            .toList();

    assertThat(documentNumbers).isSorted();
  }

  @Test
  void testSortByField() throws IOException {
    Sort sort = new Sort(new SortField("category", Type.STRING));
    TopDocs results = indexSearcher.search(booleanQuery, 10, sort, true);

    List<String> titles = documents(indexSearcher, results).stream()
            .map(doc -> doc.get("category"))
            .toList();

    assertThat(titles).isSorted();
  }

  @Test
  void testSortByMultipleFields() throws IOException {
    Sort sort = new Sort(new SortField("category", Type.STRING), new SortedNumericSortField("pubmonth", Type.LONG));
    TopDocs results = indexSearcher.search(booleanQuery, 10, sort, true);

    List<Document> docs = documents(indexSearcher, results);
    assertThat(docs).isSortedAccordingTo(comparing((Document doc) -> doc.get("category"))
            .thenComparing((Document doc) -> doc.get("pubmonth"))
    );
  }

  @Test
  void testSortByFieldReverse() throws IOException {
    Sort sort = new Sort(new SortField("category", Type.STRING, true));
    TopDocs results = indexSearcher.search(booleanQuery, 10, sort, true);

    List<String> titles = documents(indexSearcher, results).stream()
            .map(doc -> doc.get("category"))
            .toList();

    assertThat(titles).isSortedAccordingTo(Comparator.reverseOrder());
  }
}
