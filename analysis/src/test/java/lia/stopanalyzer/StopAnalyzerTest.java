package lia.stopanalyzer;

import java.io.StringReader;
import java.util.Arrays;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.StopAnalyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class StopAnalyzerTest {
  private static final StopAnalyzer STOP_ANALYZER = new StopAnalyzer(EnglishAnalyzer.ENGLISH_STOP_WORDS_SET);

  @Test void testHoles() throws Exception {
    List<String> expected = Arrays.asList("one", "enough");

    assertAnalyzesTo(STOP_ANALYZER, "one is not enough", expected);
    assertAnalyzesTo(STOP_ANALYZER, "one is enough", expected);
    assertAnalyzesTo(STOP_ANALYZER, "one enough", expected);
    assertAnalyzesTo(STOP_ANALYZER, "one but not enough", expected);
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
