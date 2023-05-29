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
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.SortField.Type;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SortingTest {
  Directory directory;

  IndexSearcher searcher;

  BooleanQuery query;

  @BeforeEach
  void setup() throws Exception {
    directory = TestUtil.getBookIndexDirectory();

    Query allBooks = new MatchAllDocsQuery();

    QueryParser parser = new QueryParser("contents", new StandardAnalyzer());
    query = new BooleanQuery.Builder()
        .add(allBooks, Occur.SHOULD)
        .add(parser.parse("java OR action"), Occur.SHOULD)
        .build();

    searcher = new IndexSearcher(DirectoryReader.open(directory));
  }

  @AfterEach
  void tearDown() throws Exception {
    directory.close();
  }

  @Test
  void testSortByRelevance() throws Exception {
    TopDocs results = searcher.search(query, 10, Sort.RELEVANCE, true);
    List<Float> scores = Arrays.stream(results.scoreDocs).map(scoreDoc -> scoreDoc.score).toList();
    assertThat(scores).isSortedAccordingTo(Comparator.reverseOrder());
  }

  @Test
  void testSortByIndexOrder() throws Exception {
    // sorted by index / document number
    TopDocs results = searcher.search(query, 10, Sort.INDEXORDER, true);
    List<Integer> documentNumbers = Arrays.stream(results.scoreDocs).map(scoreDoc -> scoreDoc.doc).toList();
    assertThat(documentNumbers).isSorted();
  }

  @Test
  void testSortByField() throws Exception {
    TopDocs results = searcher.search(query, 10, new Sort(new SortField("category", Type.STRING)), true);
    List<String> titles = Arrays.stream(results.scoreDocs).map(scoreDoc -> {
      try {
        return searcher.storedFields().document(scoreDoc.doc).get("category");
      }
      catch (IOException e) {
        throw new RuntimeException(e);
      }
    }).toList();

    assertThat(titles).isSorted();
  }

  @Test
  void testSortByFieldReverse() throws Exception {
    TopDocs results = searcher.search(query, 10, new Sort(new SortField("category", Type.STRING, true)), true);
    List<String> titles = Arrays.stream(results.scoreDocs).map(scoreDoc -> {
      try {
        return searcher.storedFields().document(scoreDoc.doc).get("category");
      }
      catch (IOException e) {
        throw new RuntimeException(e);
      }
    }).toList();

    assertThat(titles).isSortedAccordingTo(Comparator.reverseOrder());
  }
}
