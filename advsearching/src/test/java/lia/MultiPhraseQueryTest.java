package lia;

import java.util.Collections;
import java.util.List;

import lia.synonym.SynonymAnalyzer;
import lia.synonym.SynonymEngine;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MultiPhraseQuery;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.ByteBuffersDirectory;
import org.apache.lucene.store.Directory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MultiPhraseQueryTest {
  private Directory directory;

  private IndexSearcher searcher;

  @BeforeEach
  void setUp() throws Exception {
    directory = new ByteBuffersDirectory();
    IndexWriter writer = new IndexWriter(directory, new IndexWriterConfig(new WhitespaceAnalyzer()));
    Document doc1 = new Document();
    doc1.add(new TextField("field", "the quick brown fox jumped over the lazy dog", Store.YES));
    writer.addDocument(doc1);

    Document doc2 = new Document();
    doc2.add(new TextField("field", "the fast fox hopped over the hound", Store.YES));
    writer.addDocument(doc2);
    writer.close();

    searcher = new IndexSearcher(DirectoryReader.open(directory));
  }

  @AfterEach
  void tearDown() throws Exception {
    directory.close();
  }

  @Test
  void testBasic() throws Exception {
    MultiPhraseQuery.Builder builder = new MultiPhraseQuery.Builder();
    builder
        .add(new Term[]{new Term("field", "quick"), new Term("field", "fast")})
        .add(new Term("field", "fox"));

    MultiPhraseQuery query = builder.build();
    TopDocs hits = searcher.search(query, 10);
    assertThat(hits.totalHits.value).isOne();

    builder.setSlop(1);
    query = builder.build();

    hits = searcher.search(query, 10);
    assertThat(hits.totalHits.value).isEqualTo(2);
  }


  @Test
  void testAgainstOR() throws Exception {
    PhraseQuery quickFox = new PhraseQuery.Builder()
        .add(new Term("field", "quick"))
        .add(new Term("field", "fox"))
        .setSlop(1)
        .build();

    PhraseQuery fastFox = new PhraseQuery.Builder()
        .add(new Term("field", "fast"))
        .add(new Term("field", "fox"))
        .build();

    BooleanQuery query = new BooleanQuery.Builder()
        .add(new BooleanClause(quickFox, Occur.SHOULD))
        .add(new BooleanClause(fastFox, Occur.SHOULD))
        .build();

    TopDocs hits = searcher.search(query, 10);
    assertThat(hits.totalHits.value).isEqualTo(2);
  }

  @Test
  void testQueryParser() throws Exception {
    SynonymEngine engine = s -> {
      if (s.equals("quick")) {
        return List.of("fast");
      } else {
        return Collections.emptyList();
      }
    };

    Query query = new QueryParser("field", new SynonymAnalyzer(engine)).parse("\"quick fox\"");

    assertThat(query).hasToString("field:\"(quick fast) fox\"");
    assertThat(query).isInstanceOf(MultiPhraseQuery.class);
  }
}