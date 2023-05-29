package lia.stopanalyzer;

import lia.common.TestUtil;
import org.apache.lucene.analysis.core.StopAnalyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class StopAnalyzerTest {
  private static final StopAnalyzer STOP_ANALYZER = new StopAnalyzer(EnglishAnalyzer.ENGLISH_STOP_WORDS_SET);

  @Test void testHoles() throws Exception {
    assertThat(TestUtil.getTokens(STOP_ANALYZER, "one is not enough")).contains("one", "enough");
    assertThat(TestUtil.getTokens(STOP_ANALYZER, "one is enough")).contains("one", "enough");
    assertThat(TestUtil.getTokens(STOP_ANALYZER, "one enough")).contains("one", "enough");
    assertThat(TestUtil.getTokens(STOP_ANALYZER, "one but not enough")).contains("one", "enough");
  }
}
