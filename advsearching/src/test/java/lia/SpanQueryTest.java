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
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.ByteBuffersDirectory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class SpanQueryTest
{
  private ByteBuffersDirectory directory;

  private IndexSearcher searcher;

  private SpanTermQuery quick;

  private SpanTermQuery brown;

  private SpanTermQuery red;

  private SpanTermQuery fox;

  private SpanTermQuery lazy;

  private SpanTermQuery sleepy;

  private SpanTermQuery dog;

  private SpanTermQuery cat;

  private Analyzer analyzer;

  @BeforeEach
  void setUp() throws Exception {
    directory = new ByteBuffersDirectory();

    analyzer = new WhitespaceAnalyzer();

    IndexWriter writer = new IndexWriter(directory, new IndexWriterConfig(analyzer));

    Document doc = new Document();
    doc.add(new TextField("f", "the quick brown fox jumps over the lazy dog", Store.YES));
    writer.addDocument(doc);

    doc = new Document();
    doc.add(new TextField("f", "the quick red fox jumps over the sleepy cat", Store.YES));
    writer.addDocument(doc);

    writer.close();

    searcher = new IndexSearcher(DirectoryReader.open(directory));
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
    SpanQuery[] quickBrownDog = new SpanQuery[]{quick, brown, dog};
    SpanNearQuery snq = new SpanNearQuery(quickBrownDog, 0, true);
    assertNoMatches(snq);

    snq = new SpanNearQuery(quickBrownDog, 4, true);
    assertNoMatches(snq);

    snq = new SpanNearQuery(quickBrownDog, 5, true);
    assertOnlyBrownFox(snq);

    // interesting - even a sloppy phrase query would require
    // more slop to match
    snq = new SpanNearQuery(new SpanQuery[]{lazy, fox}, 3, false);
    assertOnlyBrownFox(snq);

    PhraseQuery.Builder builder = new PhraseQuery.Builder();
    builder.add(new Term("f", "lazy"));
    builder.add(new Term("f", "fox"));
    builder.setSlop(4);
    assertNoMatches(builder.build());

    builder.setSlop(5);
    assertOnlyBrownFox(builder.build());
  }

  @Test
  void testSpanNotQuery() throws Exception {
    SpanNearQuery quickFox = new SpanNearQuery(new SpanQuery[]{quick, fox}, 1, true);
    assertBothFoxes(quickFox);

    SpanNotQuery quickFoxDog = new SpanNotQuery(quickFox, dog);
    assertBothFoxes(quickFoxDog);

    SpanNotQuery noQuickRedFox = new SpanNotQuery(quickFox, red);
    assertOnlyBrownFox(noQuickRedFox);
  }

  @Test
  void testSpanOrQuery() throws Exception {
    SpanNearQuery quickFox = new SpanNearQuery(new SpanQuery[]{quick, fox}, 1, true);
    SpanNearQuery lazyDog = new SpanNearQuery(new SpanQuery[]{lazy, dog}, 0, true);
    SpanNearQuery sleepyCat = new SpanNearQuery(new SpanQuery[]{sleepy, cat}, 0, true);

    SpanNearQuery quickFoxNearLazyDog = new SpanNearQuery(new SpanQuery[]{quickFox, lazyDog}, 3, true);
    assertOnlyBrownFox(quickFoxNearLazyDog);

    SpanNearQuery quickFoxNearSleepyCat = new SpanNearQuery(new SpanQuery[]{quickFox, sleepyCat}, 3, true);
    SpanOrQuery or = new SpanOrQuery(quickFoxNearLazyDog, quickFoxNearSleepyCat);
    assertBothFoxes(or);
  }

  private void assertOnlyBrownFox(Query query) throws Exception {
    TopDocs hits = searcher.search(query, 10);
    assertThat(hits.totalHits.value).isOne();
    assertThat(hits.scoreDocs[0].doc).isZero();
  }

  private void assertBothFoxes(Query query) throws Exception {
    TopDocs hits = searcher.search(query, 10);
    assertThat(hits.totalHits.value).isEqualTo(2);
  }

  private void assertNoMatches(Query query) throws Exception {
    TopDocs hits = searcher.search(query, 10);
    assertThat(hits.totalHits.value).isZero();
  }
}
