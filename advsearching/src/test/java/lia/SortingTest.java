package lia;

import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import lia.common.TestUtil;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.SortField.Type;
import org.apache.lucene.search.SortedNumericSortField;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SortingTest {
  private Directory directory;

  private IndexSearcher indexSearcher;

  private BooleanQuery booleanQuery;

  @BeforeEach
  void setup() throws Exception {
    directory = TestUtil.getBookIndexDirectory();

    Query javaBook = new QueryParser("contents", new StandardAnalyzer()).parse("java OR action");
    Query allBooks = new MatchAllDocsQuery();

    booleanQuery = new BooleanQuery.Builder()
        .add(allBooks, Occur.SHOULD)
        .add(javaBook, Occur.SHOULD)
        .build();

    indexSearcher = new IndexSearcher(DirectoryReader.open(directory));
  }

  @AfterEach
  void tearDown() throws Exception {
    directory.close();
  }

  @Test
  void testSortByRelevance() throws Exception {
    TopDocs results = indexSearcher.search(booleanQuery, 10, Sort.RELEVANCE, true);
    List<Float> scores = Arrays.stream(results.scoreDocs).map(this::mapToScore).toList();
    assertThat(scores).isSortedAccordingTo(Comparator.reverseOrder());
  }

  @Test
  void testSortByIndexOrder() throws Exception {
    TopDocs results = indexSearcher.search(booleanQuery, 10, Sort.INDEXORDER, true);
    List<Integer> documentNumbers = Arrays.stream(results.scoreDocs).map(this::mapToDocumentID).toList();
    assertThat(documentNumbers).isSorted();
  }

  @Test
  void testSortByField() throws Exception {
    TopDocs results = indexSearcher.search(booleanQuery, 10, new Sort(new SortField("category", Type.STRING)), true);
    List<String> titles = Arrays.stream(results.scoreDocs).map(this::mapToCategory).toList();
    assertThat(titles).isSorted();
  }

  @Test
  void testSortByMultipleFields() throws Exception {
    TopDocs results = indexSearcher.search(booleanQuery, 10,
            new Sort(new SortField("category", Type.STRING),
                     new SortedNumericSortField("pubmonth", Type.LONG)), true);
    List<ScoreDoc> docs = Arrays.stream(results.scoreDocs).toList();
    assertThat(docs).isSortedAccordingTo(Comparator.comparing(this::mapToCategory).thenComparing(this::mapToPubMonth));
  }

  @Test
  void testSortByFieldReverse() throws Exception {
    TopDocs results = indexSearcher.search(booleanQuery, 10, new Sort(new SortField("category", Type.STRING, true)), true);
    List<String> titles = Arrays.stream(results.scoreDocs).map(this::mapToCategory).toList();
    assertThat(titles).isSortedAccordingTo(Comparator.reverseOrder());
  }

  private String mapToCategory(ScoreDoc scoreDoc) {
    try {
      return indexSearcher.storedFields().document(scoreDoc.doc).get("category");
    } catch (IOException e) {
      throw new RuntimeException("Failed to retrieve category for document " + scoreDoc.doc, e);
    }
  }

  private String mapToPubMonth(ScoreDoc scoreDoc) {
    try {
      return indexSearcher.storedFields().document(scoreDoc.doc).get("pubmonth");
    } catch (IOException e) {
      throw new RuntimeException("Failed to retrieve pubmonth for document " + scoreDoc.doc, e);
    }
  }

  private int mapToDocumentID(ScoreDoc scoreDoc) {
    return scoreDoc.doc;
  }

  private float mapToScore(ScoreDoc scoreDoc) {
    return scoreDoc.score;
  }
}
