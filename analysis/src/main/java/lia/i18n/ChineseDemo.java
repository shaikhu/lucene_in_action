package lia.i18n;

import java.awt.Font;
import java.awt.FontMetrics;
import java.io.IOException;
import java.io.StringReader;
import java.util.List;
import javax.swing.JFrame;
import javax.swing.JLabel;

import lia.SimpleAnalyzer;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.cjk.CJKAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

public class ChineseDemo {
  private static final List<String> TEXT = List.of("\u9053\u5FB7\u7D93");

  private static final List<Analyzer> ANALYSERS = List.of(
      new SimpleAnalyzer(),
      new StandardAnalyzer(),
      new CJKAnalyzer());

  private static void analyze(String text, Analyzer analyzer) throws IOException {
    var stringBuilder = new StringBuilder();
    var tokenStream = analyzer.tokenStream("contents", new StringReader(text));
    var charTerm = tokenStream.addAttribute(CharTermAttribute.class);

    tokenStream.reset();
    while(tokenStream.incrementToken()) {
      stringBuilder.append("[").append(charTerm.toString()).append("] ");
    }
    tokenStream.end();
    tokenStream.close();
    var output = stringBuilder.toString();

    JFrame frame = new JFrame();
    frame.setTitle(analyzer.getClass().getSimpleName() + " : " + text);
    frame.setResizable(true);
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    var font = new Font(null, Font.PLAIN, 36);
    var width = getWidth(frame.getFontMetrics(font), output);
    frame.setSize((width < 250) ? 250 : width + 50, 75);

    var label = new JLabel(output);
    label.setSize(width, 75);
    label.setAlignmentX(JLabel.CENTER_ALIGNMENT);
    label.setAlignmentY(JLabel.CENTER_ALIGNMENT);
    label.setFont(font);
    frame.add(label);
    frame.setVisible(true);
  }

  private static int getWidth(FontMetrics fontMetrics, String text) {
    var size = 0;
    for (var i = 0; i < text.length(); i++) {
      size += fontMetrics.charWidth(text.charAt(i));
    }
    return size;
  }

  public static void main(String... args) throws IOException {
    for (var text : TEXT) {
      for (var analyzer : ANALYSERS) {
        analyze(text, analyzer);
      }
    }
  }
}
