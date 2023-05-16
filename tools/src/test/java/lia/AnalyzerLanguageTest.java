package lia;

import java.io.StringReader;
import java.util.Arrays;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.es.SpanishAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AnalyzerLanguageTest {
  @Test
  void testEnglish() throws Exception {
    Analyzer analyzer = new EnglishAnalyzer();
    assertAnalyzesTo(analyzer, "stemming algorithms", Arrays.asList("stem", "algorithm"));
  }

  @Test
  void testSpanish() throws Exception {
    Analyzer analyzer = new SpanishAnalyzer();
    assertAnalyzesTo(analyzer, "algoritmos", List.of("algoritm"));
  }

  private void assertAnalyzesTo(Analyzer analyzer, String input, List<String> output) throws Exception {
    TokenStream stream = analyzer.tokenStream("field", new StringReader(input));
    CharTermAttribute termAttr = stream.addAttribute(CharTermAttribute.class);
    stream.reset();
    for (String expected : output) {
      assertThat(stream.incrementToken()).isTrue();
      assertThat(termAttr.toString()).isEqualTo(expected);
    }
    assertThat(stream.incrementToken()).isFalse();
    stream.end();
    stream.close();
  }
}
