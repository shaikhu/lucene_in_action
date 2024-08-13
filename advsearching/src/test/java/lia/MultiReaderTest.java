package lia;

import java.util.List;

import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.MultiReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.TermRangeQuery;
import org.apache.lucene.store.ByteBuffersDirectory;
import org.apache.lucene.store.Directory;
import org.apache.lucene.util.BytesRef;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MultiReaderTest {
  private static final List<String> ANIMALS = List.of("aardvark", "beaver", "coati", "dog", "elephant", "frog",
          "gila monster", "horse", "iguana", "javelina", "kangaroo", "lemur", "moose", "nematode", "orca", "python",
          "quokka", "rat", "scorpion", "tarantula", "uromastyx", "vicuna", "walrus", "xiphias", "yak", "zebra");

  private Directory directory1;

  private Directory directory2;

  @BeforeEach
  void setUp() throws Exception {
    directory1 = new ByteBuffersDirectory();
    directory2 = new ByteBuffersDirectory();

    var analyzer = new WhitespaceAnalyzer();
    try (var indexWriter1 = new IndexWriter(directory1, new IndexWriterConfig(analyzer));
         var indexWriter2 = new IndexWriter(directory2, new IndexWriterConfig(analyzer))) {
      for (var animal : ANIMALS) {
        var document = new Document();
        document.add(new StringField("animal", animal, Store.YES));
        if (animal.charAt(0) < 'n') {
          indexWriter1.addDocument(document);
        } else {
          indexWriter2.addDocument(document);
        }
      }
    }
  }

  @AfterEach
  void tearDown() throws Exception {
    directory1.close();
    directory2.close();
  }

  @Test
  void testMulti() throws Exception {
    try (var directoryReader1 = DirectoryReader.open(directory1);
         var directoryReader2 = DirectoryReader.open(directory2)) {
      var indexSearcher = new IndexSearcher(new MultiReader(directoryReader1, directoryReader2));
      var query = new TermRangeQuery("animal", new BytesRef("h"), new BytesRef("t"), true, true);
      var topDocs = indexSearcher.search(query, 10);
      assertThat(topDocs.totalHits.value).isEqualTo(12);
    }
  }
}
