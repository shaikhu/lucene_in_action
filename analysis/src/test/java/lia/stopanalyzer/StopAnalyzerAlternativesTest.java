package lia.stopanalyzer;

import java.io.StringReader;
import java.util.Arrays;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class StopAnalyzerAlternativesTest {
  @Test
  void testStopAnalyzer2() throws Exception {
    assertAnalyzesTo(new StopAnalyzer1(), "The quick brown...", Arrays.asList("quick", "brown"));
  }

  @Test
  public void testStopAnalyzerFlawed() throws Exception {
    assertAnalyzesTo(new StopAnalyzerFlawed(), "The quick brown...", Arrays.asList("the", "quick", "brown"));
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
