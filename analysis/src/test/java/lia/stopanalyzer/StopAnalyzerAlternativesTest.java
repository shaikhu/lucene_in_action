package lia.stopanalyzer;

import lia.common.TestUtil;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class StopAnalyzerAlternativesTest {
  private static final String TEXT = "The quick brown";

  @Test
  void testStopAnalyzer2() throws Exception {
    assertThat(TestUtil.getTokens(new StopAnalyzer1(), TEXT)).containsOnly("quick", "brown");
  }

  @Test
  public void testStopAnalyzerFlawed() throws Exception {
    assertThat(TestUtil.getTokens(new StopAnalyzerFlawed(), TEXT)).containsOnly("the", "quick", "brown");
  }
}
