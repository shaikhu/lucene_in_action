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
import org.apache.lucene.store.ByteBuffersDirectory;
import org.apache.lucene.store.Directory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MultiPhraseQueryTest {
  private static final SynonymEngine SYNONYM_ENGINE = text -> text.equals("quick") ? List.of("fast") : Collections.emptyList();

  private Directory directory;

  private IndexSearcher indexSearcher;

  @BeforeEach
  void setUp() throws Exception {
    directory = new ByteBuffersDirectory();

    try (var indexWriter = new IndexWriter(directory, new IndexWriterConfig(new WhitespaceAnalyzer()))) {
      var document = new Document();
      document.add(new TextField("field", "the quick brown fox jumped over the lazy dog", Store.YES));
      indexWriter.addDocument(document);

      document = new Document();
      document.add(new TextField("field", "the fast fox hopped over the hound", Store.YES));
      indexWriter.addDocument(document);
    }

    indexSearcher = new IndexSearcher(DirectoryReader.open(directory));
  }

  @AfterEach
  void tearDown() throws Exception {
    directory.close();
  }

  @Test
  void testBasic() throws Exception {
    var builder = new MultiPhraseQuery.Builder()
        .add(new Term[]{new Term("field", "quick"), new Term("field", "fast")})
        .add(new Term("field", "fox"));

    var multiPhraseQuery = builder.build();
    var topDocs = indexSearcher.search(multiPhraseQuery, 10);
    assertThat(topDocs.totalHits.value()).isOne();

    builder.setSlop(1);
    multiPhraseQuery = builder.build();
    topDocs = indexSearcher.search(multiPhraseQuery, 10);
    assertThat(topDocs.totalHits.value()).isEqualTo(2);
  }


  @Test
  void testAgainstOR() throws Exception {
    var quickFoxQuery = new PhraseQuery.Builder()
        .add(new Term("field", "quick"))
        .add(new Term("field", "fox"))
        .setSlop(1)
        .build();

    var fastFoxQuery = new PhraseQuery.Builder()
        .add(new Term("field", "fast"))
        .add(new Term("field", "fox"))
        .build();

    var booleanQuery = new BooleanQuery.Builder()
        .add(new BooleanClause(quickFoxQuery, Occur.SHOULD))
        .add(new BooleanClause(fastFoxQuery, Occur.SHOULD))
        .build();

    var topDocs = indexSearcher.search(booleanQuery, 10);
    assertThat(topDocs.totalHits.value()).isEqualTo(2);
  }

  @Test
  void testQueryParser() throws Exception {
    var queryParser = new QueryParser("field", new SynonymAnalyzer(SYNONYM_ENGINE));
    var query = queryParser.parse("\"quick fox\"");
    assertThat(query).hasToString("field:\"(quick fast) fox\"");
    assertThat(query).isInstanceOf(MultiPhraseQuery.class);
  }
}