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

public class StopAnalyzerTest
{
  private static final StopAnalyzer STOP_ANALYZER = new StopAnalyzer(EnglishAnalyzer.ENGLISH_STOP_WORDS_SET);

  private static final StopAnalyzer1 STOP_ANALYZER_ONE = new StopAnalyzer1();

  @Test
  void testHoles() throws Exception {
    List<String> expected = Arrays.asList("one", "enough");

    assertAnalyzesTo(STOP_ANALYZER, "one is not enough", expected);
    assertAnalyzesTo(STOP_ANALYZER, "one is enough", expected);
    assertAnalyzesTo(STOP_ANALYZER, "one enough", expected);
    assertAnalyzesTo(STOP_ANALYZER, "one but not enough", expected);
  }

  @Test
  void testStopAnalyzers() throws Exception {
    assertAnalyzesTo(STOP_ANALYZER, "The quick brown...", Arrays.asList("quick", "brown"));
    assertAnalyzesTo(STOP_ANALYZER_ONE, "The quick brown...", Arrays.asList("quick", "brown"));
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
