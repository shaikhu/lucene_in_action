package lia;

import java.io.IOException;

import lia.common.TestUtil;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.store.Directory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BasicSearchingTest {
  private Directory directory;

  private IndexSearcher indexSearcher;

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
  void testTerm() throws IOException {
    var term = new Term("subject", "ant");
    var query = new TermQuery(term);

    var topDocs = indexSearcher.search(query, 10);
    assertThat(topDocs.totalHits.value()).isOne();

    term = new Term("subject", "junit");
    topDocs = indexSearcher.search(new TermQuery(term), 10);
    assertThat(topDocs.totalHits.value()).isEqualTo(2);
  }

  @Test
  void testKeyword() throws IOException {
    var term = new Term("isbn", "9781935182023");
    var query = new TermQuery(term);
    var topDocs = indexSearcher.search(query, 10);
    assertThat(topDocs.totalHits.value()).isOne();
  }

  @Test
  void testQueryParser() throws ParseException, IOException {
    var queryParser = new QueryParser("contents", new StandardAnalyzer());
    var query = queryParser.parse("+JUNIT +ANT -MOCK");
    var topDocs = indexSearcher.search(query, 10);
    assertThat(topDocs.totalHits.value()).isOne();

    var document = indexSearcher.storedFields().document(topDocs.scoreDocs[0].doc);
    assertThat(document.get("title")).isEqualTo("Ant in Action");

    query = queryParser.parse("mock OR junit");
    topDocs = indexSearcher.search(query, 10);
    assertThat(topDocs.totalHits.value()).isEqualTo(2);
  }
}
