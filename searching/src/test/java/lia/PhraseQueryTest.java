package lia;

import java.io.IOException;

import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.ByteBuffersDirectory;
import org.apache.lucene.store.Directory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PhraseQueryTest
{
  private Directory directory;

  private IndexSearcher searcher;

  @BeforeEach
  void setup() throws IOException {
    directory = new ByteBuffersDirectory();

    IndexWriter writer = new IndexWriter(directory, new IndexWriterConfig(new WhitespaceAnalyzer()));
    Document doc = new Document();
    doc.add(new TextField("field", "the quick brown fox jumped over the lazy dog", Store.NO));
    writer.addDocument(doc);
    writer.close();
    searcher = new IndexSearcher(DirectoryReader.open(directory));
  }


  @AfterEach
  void tearDown() throws IOException {
    directory.close();
  }

  private boolean matched(int slop, String... phrase) throws IOException {
    PhraseQuery query = new PhraseQuery(slop, "field", phrase);
    TopDocs matches = searcher.search(query, 10);
    return matches.totalHits.value > 0;
  }

  @Test
  void testSlopComparison() throws Exception {
    assertFalse(matched(0, "quick", "fox"));
    assertTrue(matched(1, "quick", "fox"));
  }

  @Test
  void testReverse() throws Exception {
    assertFalse(matched(2, "fox", "quick"));
    assertTrue(matched(3, "fox", "quick"));
  }

  @Test
  void testMultiple() throws Exception {
    assertFalse(matched(3, "quick", "jumped", "lazy"));
    assertTrue(matched(4, "quick", "jumped", "lazy"));
    assertFalse(matched(7, "lazy", "jumped", "quick"));
    assertTrue(matched(8, "lazy", "jumped", "quick"));
  }
}
