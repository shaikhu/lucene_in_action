package lia;

import lia.common.TestUtil;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.es.SpanishAnalyzer;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AnalyzerLanguageTest {
  @Test
  void testEnglish() throws Exception {
    assertThat(TestUtil.getTokens(new EnglishAnalyzer(), "stemming algorithms")).containsOnly("stem", "algorithm");
  }

  @Test
  void testSpanish() throws Exception {
    assertThat(TestUtil.getTokens(new SpanishAnalyzer(), "algoritmos")).containsOnly("algoritm");
  }
}
