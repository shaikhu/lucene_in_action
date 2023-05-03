package lia.synonym;

import java.io.IOException;

import lia.AnalyzerUtils;

public class SynonymAnalyzerViewer {
  public static void main(String[] args) throws IOException {
    SynonymEngine engine = new TestSynonymEngine();

    AnalyzerUtils.displayTokensWithPositions(new SynonymAnalyzer(engine), "The quick brown fox jumps over the lazy dog");
  }
}
