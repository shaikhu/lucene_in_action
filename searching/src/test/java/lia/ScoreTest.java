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
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.WildcardQuery;
import org.apache.lucene.store.ByteBuffersDirectory;
import org.apache.lucene.store.Directory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ScoreTest
{
  private Directory directory;

  @BeforeEach
  void setup() {
    directory = new ByteBuffersDirectory();
  }

  @AfterEach
  void tearDown() throws Exception {
    directory.close();
  }

  private void indexSingleFieldDocs(Field... fields) throws Exception {
    IndexWriter writer = new IndexWriter(directory, new IndexWriterConfig(new WhitespaceAnalyzer()));
    for (Field f : fields) {
      Document doc = new Document();
      doc.add(f);
      writer.addDocument(doc);
    }
    writer.close();
  }

  @Test
  void testWildCard() throws Exception {
    indexSingleFieldDocs(
        new TextField("contents", "wild", Store.YES),
        new TextField("contents", "child", Store.YES),
        new TextField("contents", "mild", Store.YES),
        new TextField("contents", "mildew", Store.YES));

    IndexSearcher searcher = new IndexSearcher(DirectoryReader.open(directory));
    Query query = new WildcardQuery(new Term("contents", "?ild*"));
    TopDocs matches = searcher.search(query, 10);
    assertThat(matches.totalHits.value).isEqualTo(3);
    assertThat(matches.scoreDocs[0].score).isEqualTo(matches.scoreDocs[1].score);
    assertThat(matches.scoreDocs[1].score).isEqualTo(matches.scoreDocs[2].score);
  }

  @Test
  void testFuzzy() throws Exception {
    indexSingleFieldDocs(
        new TextField("contents", "fuzzy", Store.YES),
        new TextField("contents", "wuzzy", Store.YES));

    IndexSearcher searcher = new IndexSearcher(DirectoryReader.open(directory));
    Query query = new FuzzyQuery(new Term("contents", "wuzza"));
    TopDocs matches = searcher.search(query, 10);
    assertThat(matches.totalHits.value).isEqualTo(2);
    assertThat(matches.scoreDocs[0].score).isNotEqualTo(matches.scoreDocs[1].score);
    Document doc = searcher.storedFields().document(matches.scoreDocs[0].doc);
    assertThat(doc.get("contents")).isEqualTo("wuzzy");
  }
}
