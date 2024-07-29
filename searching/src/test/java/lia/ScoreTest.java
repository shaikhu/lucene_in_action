package lia;

import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.FuzzyQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.WildcardQuery;
import org.apache.lucene.search.similarities.BasicStats;
import org.apache.lucene.search.similarities.SimilarityBase;
import org.apache.lucene.store.ByteBuffersDirectory;
import org.apache.lucene.store.Directory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ScoreTest {
  private Directory directory;

  @BeforeEach
  void setup() {
    directory = new ByteBuffersDirectory();
  }

  @AfterEach
  void tearDown() throws Exception {
    directory.close();
  }

  @Test
  void testSimple() throws Exception {
    indexSingleFieldDocs(new TextField("contents", "x", Store.YES));

    var indexSearcher = new IndexSearcher(DirectoryReader.open(directory));
    indexSearcher.setSimilarity(new SimpleSimilarity());

    var query = new TermQuery(new Term("contents", "x"));

    var topDocs = indexSearcher.search(query, 10);
    assertThat(topDocs.totalHits.value).isOne();
    assertThat(topDocs.scoreDocs[0].score).isEqualTo(0);
  }

  @Test
  void testWildCard() throws Exception {
    indexSingleFieldDocs(
        new TextField("contents", "wild", Store.YES),
        new TextField("contents", "child", Store.YES),
        new TextField("contents", "mild", Store.YES),
        new TextField("contents", "mildew", Store.YES));

    var indexSearcher = new IndexSearcher(DirectoryReader.open(directory));
    var query = new WildcardQuery(new Term("contents", "?ild*"));
    var topDocs = indexSearcher.search(query, 10);

    assertThat(topDocs.totalHits.value).isEqualTo(3);
    assertThat(topDocs.scoreDocs[0].score).isEqualTo(topDocs.scoreDocs[1].score);
    assertThat(topDocs.scoreDocs[1].score).isEqualTo(topDocs.scoreDocs[2].score);
  }

  @Test
  void testFuzzy() throws Exception {
    indexSingleFieldDocs(new TextField("contents", "fuzzy", Store.YES), new TextField("contents", "wuzzy", Store.YES));

    var indexSearcher = new IndexSearcher(DirectoryReader.open(directory));
    var query = new FuzzyQuery(new Term("contents", "wuzza"));
    var topDocs = indexSearcher.search(query, 10);

    assertThat(topDocs.totalHits.value).isEqualTo(2);
    assertThat(topDocs.scoreDocs[0].score).isNotEqualTo(topDocs.scoreDocs[1].score);

    var document = indexSearcher.storedFields().document(topDocs.scoreDocs[0].doc);
    assertThat(document.get("contents")).isEqualTo("wuzzy");
  }

  private void indexSingleFieldDocs(Field... fields) throws Exception {
    try (var indexWriter = new IndexWriter(directory, new IndexWriterConfig(new WhitespaceAnalyzer())) ) {
      for (var field : fields) {
        var document = new Document();
        document.add(field);
        indexWriter.addDocument(document);
      }
    }
  }

  private static class SimpleSimilarity extends SimilarityBase {
    @Override
    protected double score(BasicStats stats, double freq, double docLen) {
      return 0;
    }

    @Override
    public String toString() {
      return "SimpleSimilarity";
    }
  }
}
