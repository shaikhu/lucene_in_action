package lia;

import lia.common.TestUtil;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BasicSearchingTest {
  private Directory directory;

  private IndexSearcher indexSearcher;

  @BeforeEach
  void setup() throws Exception {
    directory = TestUtil.getBookIndexDirectory();
    indexSearcher = new IndexSearcher(DirectoryReader.open(directory));
  }

  @AfterEach
  void tearDown() throws Exception {
    directory.close();
  }

  @Test
  void testTerm() throws Exception {
    var term = new Term("subject", "ant");
    var query = new TermQuery(term);

    var topDocs = indexSearcher.search(query, 10);
    assertThat(topDocs.totalHits.value).isOne();

    term = new Term("subject", "junit");
    topDocs = indexSearcher.search(new TermQuery(term), 10);
    assertThat(topDocs.totalHits.value).isEqualTo(2);
  }

  @Test
  void testKeyword() throws Exception {
    var term = new Term("isbn", "9781935182023");
    var query = new TermQuery(term);
    var topDocs = indexSearcher.search(query, 10);
    assertThat(topDocs.totalHits.value).isOne();
  }

  @Test
  void testQueryParser() throws Exception {
    QueryParser parser = new QueryParser("contents", new StandardAnalyzer());
    Query query = parser.parse("+JUNIT +ANT -MOCK");
    TopDocs docs = indexSearcher.search(query, 10);
    assertThat(docs.totalHits.value).isOne();

    Document doc = indexSearcher.storedFields().document(docs.scoreDocs[0].doc);
    assertThat(doc.get("title")).isEqualTo("Ant in Action");

    query = parser.parse("mock OR junit");
    docs = indexSearcher.search(query, 10);
    assertThat(docs.totalHits.value).isEqualTo(2);
  }
}
