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
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BasicSearchingTest
{
  @Test
  void testTerm() throws Exception {
    Directory dir = TestUtil.getBookIndexDirectory();
    IndexSearcher searcher = new IndexSearcher(DirectoryReader.open(dir));

    Term t = new Term("subject", "ant");
    Query query = new TermQuery(t);

    TopDocs docs = searcher.search(query, 10);
    assertEquals(1, docs.totalHits.value, "Ant in Action");

    t = new Term("subject", "junit");
    docs = searcher.search(new TermQuery(t), 10);
    assertEquals(2, docs.totalHits.value, "Ant in Action, JUnit in Action, Second Edition");
    dir.close();
  }

  @Test
  void testKeyword() throws Exception {
    Directory dir = TestUtil.getBookIndexDirectory();
    IndexSearcher searcher = new IndexSearcher(DirectoryReader.open(dir));
    Term t = new Term("isbn", "9781935182023");
    Query query = new TermQuery(t);
    TopDocs docs = searcher.search(query, 10);
    assertEquals(1, docs.totalHits.value, "JUnit in Action, Second Edition");

    dir.close();
  }

  @Test
  void testQueryParser() throws Exception {
    Directory dir = TestUtil.getBookIndexDirectory();
    IndexSearcher searcher = new IndexSearcher(DirectoryReader.open(dir));

    QueryParser parser = new QueryParser("contents", new StandardAnalyzer());
    Query query = parser.parse("+JUNIT +ANT -MOCK");
    TopDocs docs = searcher.search(query, 10);
    assertEquals(1, docs.totalHits.value);

    Document d = searcher.storedFields().document(docs.scoreDocs[0].doc);
    assertEquals("Ant in Action", d.get("title"));

    query = parser.parse("mock OR junit");
    docs = searcher.search(query, 10);
    assertEquals(2, docs.totalHits.value);
    
    dir.close();
  }
}
