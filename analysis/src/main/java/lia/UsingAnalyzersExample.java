package lia;

import java.io.IOException;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.store.ByteBuffersDirectory;

public class UsingAnalyzersExample
{
  /**
   * This method doesn't do anything, except compile correctly.
   * This is used to show snippets of how Analyzers are used.
   */
  public void someMethod() throws IOException, ParseException {
    var directory = new ByteBuffersDirectory();
    var analyzer = new StandardAnalyzer();

    try (var indexWriter = new IndexWriter(directory, new IndexWriterConfig(analyzer))){
      var document = new Document();
      document.add(new TextField("title", "This is the title", Store.YES));
      document.add(new TextField("contents", "...document contents...", Store.NO));
      indexWriter.addDocument(document);
    }

    var expression = "some query";
    var queryParser = new QueryParser("contents", analyzer);
    var query = queryParser.parse(expression);
  }
}
