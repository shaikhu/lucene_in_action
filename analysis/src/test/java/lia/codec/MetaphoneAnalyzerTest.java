package lia.codec;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.MMapDirectory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class MetaphoneAnalyzerTest {
  private static final Analyzer METAPHONE_ANALYZER = new MetaphoneReplacementAnalyzer();

  @TempDir
  private Path index;

  @Test
  void testKoolKat() throws Exception {
    try (var directory = new MMapDirectory(index)) {
      try (var indexWriter = new IndexWriter(directory, new IndexWriterConfig(METAPHONE_ANALYZER))) {
        var document = new Document();
        document.add(new TextField("contents", "cool cat", Store.YES));
        indexWriter.addDocument(document);
      }

      var indexSearcher = new IndexSearcher(DirectoryReader.open(directory));
      var query = new QueryParser("contents", METAPHONE_ANALYZER).parse("kool kat");

      var topDocs = indexSearcher.search(query, 1);
      assertThat(topDocs.totalHits.value()).isOne();

      Document document = indexSearcher.storedFields().document(topDocs.scoreDocs[0].doc);
      assertThat(document.get("contents")).isEqualTo("cool cat");
    }
  }
}
