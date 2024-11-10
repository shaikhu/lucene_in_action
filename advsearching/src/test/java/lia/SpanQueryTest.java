package lia;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.queries.spans.SpanFirstQuery;
import org.apache.lucene.queries.spans.SpanNearQuery;
import org.apache.lucene.queries.spans.SpanNotQuery;
import org.apache.lucene.queries.spans.SpanOrQuery;
import org.apache.lucene.queries.spans.SpanQuery;
import org.apache.lucene.queries.spans.SpanTermQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.store.ByteBuffersDirectory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SpanQueryTest {
  private ByteBuffersDirectory directory;

  private IndexSearcher indexSearcher;

  private SpanTermQuery quick;

  private SpanTermQuery brown;

  private SpanTermQuery red;

  private SpanTermQuery fox;

  private SpanTermQuery lazy;

  private SpanTermQuery sleepy;

  private SpanTermQuery dog;

  private SpanTermQuery cat;

  @BeforeEach
  void setUp() throws Exception {
    directory = new ByteBuffersDirectory();

    try (var indexWriter = new IndexWriter(directory, new IndexWriterConfig(new WhitespaceAnalyzer()))) {
      var document = new Document();
      document.add(new TextField("f", "the quick brown fox jumps over the lazy dog", Store.YES));
      indexWriter.addDocument(document);

      document = new Document();
      document.add(new TextField("f", "the quick red fox jumps over the sleepy cat", Store.YES));
      indexWriter.addDocument(document);
    }

    indexSearcher = new IndexSearcher(DirectoryReader.open(directory));
    quick = new SpanTermQuery(new Term("f", "quick"));
    brown = new SpanTermQuery(new Term("f", "brown"));
    red = new SpanTermQuery(new Term("f", "red"));
    fox = new SpanTermQuery(new Term("f", "fox"));
    lazy = new SpanTermQuery(new Term("f", "lazy"));
    sleepy = new SpanTermQuery(new Term("f", "sleepy"));
    dog = new SpanTermQuery(new Term("f", "dog"));
    cat = new SpanTermQuery(new Term("f", "cat"));
  }

  @AfterEach
  void tearDown() throws Exception {
    directory.close();
  }

  @Test
  void testSpanTermQuery() throws Exception {
    assertOnlyBrownFox(brown);
  }

  @Test
  void testSpanFirstQuery() throws Exception {
    SpanFirstQuery spanFirstQuery = new SpanFirstQuery(brown, 2);
    assertNoMatches(spanFirstQuery);

    spanFirstQuery = new SpanFirstQuery(brown, 3);
    assertOnlyBrownFox(spanFirstQuery);
  }

  @Test
  void testSpanNearQuery() throws Exception {
    var quickBrownDog = new SpanQuery[]{quick, brown, dog};
    var spanNearQuery = new SpanNearQuery(quickBrownDog, 0, true);
    assertNoMatches(spanNearQuery);

    spanNearQuery = new SpanNearQuery(quickBrownDog, 4, true);
    assertNoMatches(spanNearQuery);

    spanNearQuery = new SpanNearQuery(quickBrownDog, 5, true);
    assertOnlyBrownFox(spanNearQuery);

    // interesting - even a sloppy phrase query would require more slop to match
    spanNearQuery = new SpanNearQuery(new SpanQuery[]{lazy, fox}, 3, false);
    assertOnlyBrownFox(spanNearQuery);

    var phraseQueryBuilder = new PhraseQuery.Builder()
        .add(new Term("f", "lazy"))
        .add(new Term("f", "fox"))
        .setSlop(4);
    assertNoMatches(phraseQueryBuilder.build());

    phraseQueryBuilder.setSlop(5);
    assertOnlyBrownFox(phraseQueryBuilder.build());
  }

  @Test
  void testSpanNotQuery() throws Exception {
    var quickFox = new SpanNearQuery(new SpanQuery[]{quick, fox}, 1, true);
    assertBothFoxes(quickFox);

    var quickFoxDog = new SpanNotQuery(quickFox, dog);
    assertBothFoxes(quickFoxDog);

    var noQuickRedFox = new SpanNotQuery(quickFox, red);
    assertOnlyBrownFox(noQuickRedFox);
  }

  @Test
  void testSpanOrQuery() throws Exception {
    var quickFox = new SpanNearQuery(new SpanQuery[]{quick, fox}, 1, true);
    var lazyDog = new SpanNearQuery(new SpanQuery[]{lazy, dog}, 0, true);
    var sleepyCat = new SpanNearQuery(new SpanQuery[]{sleepy, cat}, 0, true);

    var quickFoxNearLazyDog = new SpanNearQuery(new SpanQuery[]{quickFox, lazyDog}, 3, true);
    assertOnlyBrownFox(quickFoxNearLazyDog);

    var quickFoxNearSleepyCat = new SpanNearQuery(new SpanQuery[]{quickFox, sleepyCat}, 3, true);
    var bothFoxes = new SpanOrQuery(quickFoxNearLazyDog, quickFoxNearSleepyCat);
    assertBothFoxes(bothFoxes);
  }

  private void assertOnlyBrownFox(Query query) throws Exception {
    var topDocs = indexSearcher.search(query, 10);
    assertThat(topDocs.totalHits.value()).isOne();
    assertThat(topDocs.scoreDocs[0].doc).isZero();
  }

  private void assertBothFoxes(Query query) throws Exception {
    var topDocs = indexSearcher.search(query, 10);
    assertThat(topDocs.totalHits.value()).isEqualTo(2);
  }

  private void assertNoMatches(Query query) throws Exception {
    var topDocs = indexSearcher.search(query, 10);
    assertThat(topDocs.totalHits.value()).isZero();
  }
}
