package lia;

import java.util.List;

import org.apache.lucene.analysis.Analyzer;
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
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.ByteBuffersDirectory;
import org.apache.lucene.store.Directory;
import org.apache.lucene.util.BytesRef;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class MultiReaderTest
{
  private DirectoryReader reader1;

  private DirectoryReader reader2;

  @BeforeEach
  void setUp() throws Exception {
    List<String> animals = List.of("aardvark", "beaver", "coati",
        "dog", "elephant", "frog", "gila monster",
        "horse", "iguana", "javelina", "kangaroo",
        "lemur", "moose", "nematode", "orca",
        "python", "quokka", "rat", "scorpion",
        "tarantula", "uromastyx", "vicuna",
        "walrus", "xiphias", "yak", "zebra");

    Analyzer analyzer = new WhitespaceAnalyzer();

    Directory aTOmDirectory = new ByteBuffersDirectory();
    Directory nTOzDirectory = new ByteBuffersDirectory();

    IndexWriter aTOmWriter = new IndexWriter(aTOmDirectory, new IndexWriterConfig(analyzer));
    IndexWriter nTOzWriter = new IndexWriter(nTOzDirectory, new IndexWriterConfig(analyzer));


    for (int i=animals.size() - 1; i >= 0; i--) {
      Document doc = new Document();
      String animal = animals.get(i);
      doc.add(new StringField("animal", animal, Store.YES));
      if (animal.charAt(0) < 'n') {
        aTOmWriter.addDocument(doc);
      } else {                                       
        nTOzWriter.addDocument(doc);
      }
    }

    aTOmWriter.close();
    nTOzWriter.close();

    reader1 = DirectoryReader.open(aTOmDirectory);
    reader2 = DirectoryReader.open(nTOzDirectory);
  }

  @AfterEach
  void tearDown() throws Exception {
    reader1.close();
    reader2.close();
  }

  @Test
  void testMulti() throws Exception {
    IndexSearcher searcher = new IndexSearcher(new MultiReader(reader1, reader2));
    TermRangeQuery query = new TermRangeQuery("animal", new BytesRef("h"), new BytesRef("t"), true, true);
    TopDocs hits = searcher.search(query, 10);
    assertThat(hits.totalHits.value).isEqualTo(12);
  }
}
