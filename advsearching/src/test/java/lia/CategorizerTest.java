package lia;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import lia.common.TestUtil;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.TermVectors;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.util.BytesRef;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class CategorizerTest {
  Map<String,Map<String,Integer>> categories = new HashMap<>();

  @BeforeEach
  void setup() throws Exception {
    buildCategoryVectors();
  }

  private void buildCategoryVectors() throws Exception {
    IndexReader reader = DirectoryReader.open(TestUtil.getBookIndexDirectory());
    int maxDoc = reader.maxDoc();
    for (int i = 0; i < maxDoc; i++) {
      Document doc = reader.storedFields().document(i);
      String category = doc.get("category");

      Map<String, Integer> vectorMap = categories.computeIfAbsent(category, k -> new TreeMap<>());

      TermVectors termVectors = reader.termVectors();
      Terms terms = termVectors.get(i, "subject");
      addTermFreqToMap(vectorMap, terms);
    }
  }

  private void addTermFreqToMap(Map<String, Integer> vectorMap, Terms terms) throws Exception {
    List<String> subjectTerms = new ArrayList<>();

    TermsEnum enumIterator = terms.iterator();
    BytesRef bytesRef = enumIterator.next();
    while (bytesRef != null) {
      subjectTerms.add(bytesRef.utf8ToString());
      bytesRef = enumIterator.next();
    }

    int sumTotalTermFreq =  Long.valueOf(terms.getSumTotalTermFreq()).intValue();
    for (String subjectTerm : subjectTerms) {
      if (vectorMap.containsKey(subjectTerm)) {
        Integer value = vectorMap.get(subjectTerm);
        vectorMap.put(subjectTerm, value + sumTotalTermFreq);
      } else {
        vectorMap.put(subjectTerm, sumTotalTermFreq);
      }
    }
  }

  private String getCategory(String subject) {
    String[] words = subject.split(" ");

    Iterator<String> categoryIterator = categories.keySet().iterator();
    double bestAngle = Double.MAX_VALUE;
    String bestCategory = null;
    while (categoryIterator.hasNext()) {
      String category = categoryIterator.next();
      double angle = computeAngle(words, category);
      if (angle < bestAngle) {
        bestAngle = angle;
        bestCategory = category;
      }
    }
    return bestCategory;
  }

  private double computeAngle(String[] words, String category) {
    Map<String, Integer> vectorMap = categories.get(category);
    int dotProduct = 0;
    int sumOfSquares = 0;
    for (String word : words) {
      int categoryWordFreq = 0;
      if (vectorMap.containsKey(word)) {
        categoryWordFreq = vectorMap.get(word);
      }
      dotProduct += categoryWordFreq;
      sumOfSquares += categoryWordFreq * categoryWordFreq;
    }

    double denominator;
    if (sumOfSquares == words.length) {
      denominator = sumOfSquares;
    } else {
      denominator = Math.sqrt(sumOfSquares) * Math.sqrt(words.length);
    }
    double ratio = dotProduct / denominator;
    return Math.acos(ratio);
  }


  @Test
  void testCategorization() {
    assertThat(getCategory("extreme agile methodology")).isEqualTo("/technology/computers/programming/methodology");
    assertThat(getCategory("montessori education philosophy")).isEqualTo("/education/pedagogy");
  }
}
