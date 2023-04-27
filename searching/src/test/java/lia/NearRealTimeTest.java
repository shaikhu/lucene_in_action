package lia;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.ByteBuffersDirectory;
import org.apache.lucene.store.Directory;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class NearRealTimeTest
{
  @Test
  void testNearRealTime() throws Exception {
    Directory directory = new ByteBuffersDirectory();
    IndexWriter writer = new IndexWriter(directory, new IndexWriterConfig(new StandardAnalyzer()));

    for (int i = 0; i < 10; i++) {
      Document doc = new Document();
      doc.add(new StringField("id", String.valueOf(i), Store.NO));
      doc.add(new TextField("text", "aaa", Store.NO));
      writer.addDocument(doc);
    }

    DirectoryReader reader = DirectoryReader.open(writer);
    IndexSearcher searcher = new IndexSearcher(reader);

    Query query = new TermQuery(new Term("text", "aaa"));
    TopDocs docs = searcher.search(query, 1);
    assertThat(docs.totalHits.value).isEqualTo(10);

    writer.deleteDocuments(new Term("id", "7"));

    Document doc = new Document();
    doc.add(new StringField("id", "11", Store.NO));
    doc.add(new TextField("text", "bbb", Store.NO));
    writer.addDocument(doc);

    DirectoryReader newReader = DirectoryReader.openIfChanged(reader);
    assertThat(reader).isNotSameAs(newReader);
    reader.close();
    searcher = new IndexSearcher(newReader);

    TopDocs hits = searcher.search(query, 10);
    assertThat(hits.totalHits.value).isEqualTo(9);
    query = new TermQuery(new Term("text", "bbb"));
    hits = searcher.search(query, 1);
    assertThat(hits.totalHits.value).isOne();
    newReader.close();
    directory.close();
  }
}
