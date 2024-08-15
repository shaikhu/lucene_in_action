package lia;

import java.io.FileWriter;
import java.io.StringReader;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;
import org.apache.lucene.search.highlight.SimpleSpanFragmenter;

public class HighlightIt {
  private static final String OUTPUT_HTML_FILE = "result.html";

  private static final String TEXT = """
          In this section we'll show you how to make the simplest
          programmatic query, searching for a single term, and then
          we'll see how to use QueryParser to accept textual queries.
          In the sections that follow, we’ll take this simple example
          further by detailing all the query types built into Lucene.
          We begin with the simplest search of all: searching for all
          documents that contain a single term.""";

  public static void main(String... args) throws Exception {
    if (args.length != 1) {
      throw new IllegalArgumentException("Usage: java " + HighlightIt.class.getName() + " <word>");
    }

    var queryParser = new QueryParser("f", new StandardAnalyzer());
    Query query = queryParser.parse(args[0]);

    var htmlFormatter = new SimpleHTMLFormatter("<span class=\"highlight\">", "</span>");
    var tokenStream = new StandardAnalyzer().tokenStream("f", new StringReader(TEXT));
    var queryScorer = new QueryScorer(query, "f");

    var highlighter = new Highlighter(htmlFormatter, queryScorer);
    highlighter.setTextFragmenter(new SimpleSpanFragmenter(queryScorer));

    try (var fileWriter = new FileWriter(OUTPUT_HTML_FILE)) {
      fileWriter.write("<html>");
      fileWriter.write("""
            <style>
              .highlight {
                background: yellow;
              }
            </style>""");
      fileWriter.write("<body>");
      fileWriter.write(highlighter.getBestFragments(tokenStream, TEXT, 3, "..."));
      fileWriter.write("</body></html>");
    }
  }
}
