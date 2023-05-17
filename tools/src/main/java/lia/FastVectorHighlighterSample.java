package lia;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
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
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.vectorhighlight.BaseFragmentsBuilder;
import org.apache.lucene.search.vectorhighlight.FastVectorHighlighter;
import org.apache.lucene.search.vectorhighlight.FieldQuery;
import org.apache.lucene.search.vectorhighlight.FragListBuilder;
import org.apache.lucene.search.vectorhighlight.FragmentsBuilder;
import org.apache.lucene.search.vectorhighlight.ScoreOrderFragmentsBuilder;
import org.apache.lucene.search.vectorhighlight.SimpleFragListBuilder;
import org.apache.lucene.store.ByteBuffersDirectory;
import org.apache.lucene.store.Directory;

public class FastVectorHighlighterSample {
  private static final List<String> DOCUMENTS = List.of(
      "the quick brown fox jumps over the lazy dog",
      "the quick gold fox jumped over the lazy black dog",
      "the quick fox jumps over the black dog",
      "the red fox jumped over the lazy dark gray dog");

  private static final String QUERY = "quick OR fox OR \"lazy dog\"~1";

  private static final String FIELD = "f";

  private static final String OUTPUT_FILE = "result.html";

  private static final Directory DIRECTORY = new ByteBuffersDirectory();

  private static final Analyzer ANALYZER = new StandardAnalyzer();

  public static void main(String... args) throws Exception {
    makeIndex();
    searchIndex();
  }

  private static void makeIndex() throws IOException {
    FieldType fieldType = new FieldType();
    fieldType.setIndexOptions(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS_AND_OFFSETS);
    fieldType.setStored(true);
    fieldType.setTokenized(true);
    fieldType.setStoreTermVectors(true);
    fieldType.setStoreTermVectorOffsets(true);
    fieldType.setStoreTermVectorPositions(true);

    IndexWriter writer = new IndexWriter(DIRECTORY, new IndexWriterConfig(ANALYZER));
    for(String d : DOCUMENTS) {
      Document doc = new Document();
      doc.add(new Field(FIELD, d, fieldType));
      writer.addDocument(doc);
    }
    writer.close();
  }

  private static void searchIndex() throws Exception {
    QueryParser parser = new QueryParser(FIELD, ANALYZER);
    Query query = parser.parse(QUERY);
    FastVectorHighlighter highlighter = getHighlighter();
    FieldQuery fieldQuery = highlighter.getFieldQuery(query);
    IndexSearcher searcher = new IndexSearcher(DirectoryReader.open(DIRECTORY));
    TopDocs docs = searcher.search(query, 10);

    FileWriter writer = new FileWriter(OUTPUT_FILE);
    writer.write("<html>");
    writer.write("<body>");
    writer.write("<p>QUERY : " + QUERY + "</p>");
    for(ScoreDoc scoreDoc : docs.scoreDocs) {
      String snippet = highlighter.getBestFragment(fieldQuery, searcher.getIndexReader(), scoreDoc.doc, FIELD, 100 );
      if (snippet != null) {
        writer.write(scoreDoc.doc + " : " + snippet + "<br/>");
      }
    }
    writer.write("</body></html>");
    writer.close();
  }

  private static FastVectorHighlighter getHighlighter() {
    FragListBuilder fragListBuilder = new SimpleFragListBuilder();
    FragmentsBuilder fragmentBuilder = new ScoreOrderFragmentsBuilder(
            BaseFragmentsBuilder.COLORED_PRE_TAGS,
            BaseFragmentsBuilder.COLORED_POST_TAGS);
    return new FastVectorHighlighter(true, true, fragListBuilder, fragmentBuilder);
  }
}
