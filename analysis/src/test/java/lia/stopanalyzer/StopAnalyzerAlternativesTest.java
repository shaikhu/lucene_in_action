package lia.stopanalyzer;

import lia.common.TestUtil;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class StopAnalyzerAlternativesTest {
  @Test
  void testStopAnalyzer2() throws Exception {
    assertThat(TestUtil.getTokens(new StopAnalyzer1(), "The quick brown")).containsOnly("quick", "brown");
  }

  @Test
  public void testStopAnalyzerFlawed() throws Exception {
    assertThat(TestUtil.getTokens(new StopAnalyzerFlawed(), "The quick brown")).containsOnly("the", "quick", "brown");
  }
}
