package lia;

import java.nio.file.Paths;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.TopFieldDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

public class Fragments
{
  public void openSearcher() throws Exception {
    Directory directory = FSDirectory.open(Paths.get("/path/to/index"));
    IndexReader reader = DirectoryReader.open(directory);
    IndexSearcher indexSearcher = new IndexSearcher(reader);
  }

  public void nrtReader() throws Exception {
    DirectoryReader reader = null;
    IndexSearcher searcher;
    // START

    DirectoryReader newReader = DirectoryReader.openIfChanged(reader);
    if (reader != newReader) {
      reader.close();
      reader = newReader;
      searcher = new IndexSearcher(reader);
    }
    // END
  }

  public void testSearchSigs() throws Exception {
    Query query = null;
    TopDocs hits;
    TopFieldDocs fieldHits;
    Sort sort = null;
    Collector collector = null;
    int n = 10;
    IndexSearcher searcher = null;

    hits = searcher.search(query, n);
    hits = searcher.search(query, n);
    fieldHits = searcher.search(query,  n, sort);
    searcher.search(query, collector);
    searcher.search(query,  collector);
  }

  public void queryParserOperator() throws Exception {
    Analyzer analyzer = null;
    // START
    QueryParser parser = new QueryParser("contents", analyzer);
    parser.setDefaultOperator(QueryParser.AND_OPERATOR);
    // END
  }
}
