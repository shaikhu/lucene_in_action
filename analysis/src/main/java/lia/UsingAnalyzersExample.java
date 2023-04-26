package lia;

import java.io.IOException;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.Query;
import org.apache.lucene.store.ByteBuffersDirectory;

public class UsingAnalyzersExample
{
  /**
   * This method doesn't do anything, except compile correctly.
   * This is used to show snippets of how Analyzers are used.
   */
  public void someMethod() throws IOException, ParseException {
    ByteBuffersDirectory directory = new ByteBuffersDirectory();

    Analyzer analyzer = new StandardAnalyzer();
    IndexWriter writer = new IndexWriter(directory, new IndexWriterConfig(analyzer));

    Document document = new Document();
    document.add(new TextField("title", "This is the title", Store.YES));
    document.add(new TextField("contents", "...document contents...", Store.NO));
    writer.addDocument(document);

    String expression = "some query";
    QueryParser parser = new QueryParser("contents", analyzer);
    Query query = parser.parse(expression);
  }
}
