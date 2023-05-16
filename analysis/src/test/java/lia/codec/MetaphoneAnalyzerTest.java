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
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.ByteBuffersDirectory;
import org.apache.lucene.store.Directory;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MetaphoneAnalyzerTest {
  @Test
  void testKoolKat() throws Exception {
    try (Directory directory = new ByteBuffersDirectory()) {
      Analyzer analyzer = new MetaphoneReplacementAnalyzer();
      IndexWriter writer = new IndexWriter(directory, new IndexWriterConfig(analyzer));

      Document doc = new Document();
      doc.add(new TextField("contents", "cool cat", Store.YES));
      writer.addDocument(doc);
      writer.close();

      IndexSearcher searcher = new IndexSearcher(DirectoryReader.open(directory));
      Query query = new QueryParser("contents", analyzer).parse("kool kat");

      TopDocs hits = searcher.search(query, 1);
      assertThat(hits.totalHits.value).isOne();

      int docID = hits.scoreDocs[0].doc;
      doc = searcher.storedFields().document(docID);
      assertThat(doc.get("contents")).isEqualTo("cool cat");
    }
  }
}