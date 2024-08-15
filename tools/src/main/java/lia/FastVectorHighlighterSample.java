package lia;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexOptions;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.vectorhighlight.BaseFragmentsBuilder;
import org.apache.lucene.search.vectorhighlight.FastVectorHighlighter;
import org.apache.lucene.search.vectorhighlight.ScoreOrderFragmentsBuilder;
import org.apache.lucene.search.vectorhighlight.SimpleFragListBuilder;
import org.apache.lucene.store.ByteBuffersDirectory;
import org.apache.lucene.store.Directory;

public class FastVectorHighlighterSample {
  private static final String OUTPUT_HTML_FILE = "result.html";

  private static final String QUERY = "quick OR fox OR \"lazy dog\"~1";

  private static final List<String> TEXT_SAMPLES = List.of(
      "the quick brown fox jumps over the lazy dog",
      "the quick gold fox jumped over the lazy black dog",
      "the quick fox jumps over the black dog",
      "the red fox jumped over the lazy dark gray dog");


  private static void makeIndex(Directory directory) throws IOException {
    var fieldType = new FieldType();
    fieldType.setIndexOptions(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS_AND_OFFSETS);
    fieldType.setStored(true);
    fieldType.setTokenized(true);
    fieldType.setStoreTermVectors(true);
    fieldType.setStoreTermVectorOffsets(true);
    fieldType.setStoreTermVectorPositions(true);

    try (var indexWriter = new IndexWriter(directory, new IndexWriterConfig(new StandardAnalyzer()))) {
      for(var text : TEXT_SAMPLES) {
        var document = new Document();
        document.add(new Field("f", text, fieldType));
        indexWriter.addDocument(document);
      }
    }
  }

  private static void searchIndex(Directory directory) throws Exception {
    try (var directoryReader = DirectoryReader.open(directory)) {
      var indexSearcher = new IndexSearcher(directoryReader);
      var query = new QueryParser("f", new StandardAnalyzer()).parse(QUERY);
      var topDocs = indexSearcher.search(query, 10);

      var highlighter = getFastVectorHighlighter();
      var fieldQuery = highlighter.getFieldQuery(query);
      try (var fileWriter = new FileWriter(OUTPUT_HTML_FILE)) {
        fileWriter.write("<html>");
        fileWriter.write("<body>");
        fileWriter.write("<p>QUERY : " + QUERY + "</p>");
        for(var scoreDoc : topDocs.scoreDocs) {
          var htmlString = highlighter.getBestFragment(fieldQuery, indexSearcher.getIndexReader(), scoreDoc.doc, "f", 100);
          fileWriter.write(scoreDoc.doc + " : " + htmlString + "<br/>");
        }
        fileWriter.write("</body></html>");
      }
    }
  }

  private static FastVectorHighlighter getFastVectorHighlighter() {
    return new FastVectorHighlighter(true, true,
            new SimpleFragListBuilder(),
            new ScoreOrderFragmentsBuilder(BaseFragmentsBuilder.COLORED_PRE_TAGS, BaseFragmentsBuilder.COLORED_POST_TAGS));
  }

  public static void main(String... args) throws Exception {
    try (var directory = new ByteBuffersDirectory()) {
      makeIndex(directory);
      searchIndex(directory);
    }
  }
}
