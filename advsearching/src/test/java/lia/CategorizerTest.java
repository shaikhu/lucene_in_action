package lia;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import lia.common.TestUtil;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.util.BytesRef;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CategorizerTest {
  Map<String,Map<String,Integer>> categories = new HashMap<>();

  @BeforeEach
  void setup() throws Exception {
    try (IndexReader indexReader = DirectoryReader.open(TestUtil.getBookIndexDirectory())) {
      for (int i = 0; i < indexReader.maxDoc(); i++) {
        Document document = indexReader.storedFields().document(i);
        String category = document.get("category");

        Map<String, Integer> vectorMap = categories.computeIfAbsent(category, frequency -> new TreeMap<>());
        Terms terms = indexReader.termVectors().get(i, "subject");
        addTermFreqToMap(vectorMap, terms);
      }
    }
  }

  private void addTermFreqToMap(Map<String, Integer> vectorMap, Terms terms) throws Exception {
    List<String> subjectTerms = new ArrayList<>();

    TermsEnum termsIterator = terms.iterator();
    BytesRef bytesRef;
    while ((bytesRef = termsIterator.next()) != null) {
      subjectTerms.add(bytesRef.utf8ToString());
    }

    int sumTotalTermFreq =  Long.valueOf(terms.getSumTotalTermFreq()).intValue();
    for (String subjectTerm : subjectTerms) {
      vectorMap.merge(subjectTerm, sumTotalTermFreq, Integer::sum);
    }
  }

  private String getCategory(String subject) {
    double bestAngle = Double.MAX_VALUE;
    String bestCategory = "";

    for (String category : categories.keySet()) {
      double angle = computeAngle(subject.split(" "), category);
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
      int categoryWordFreq = vectorMap.getOrDefault(word, 0);
      dotProduct += categoryWordFreq;
      sumOfSquares += categoryWordFreq * categoryWordFreq;
    }
    double denominator = sumOfSquares == words.length ? sumOfSquares : Math.sqrt(sumOfSquares) * Math.sqrt(words.length);
    double ratio = dotProduct / denominator;
    return Math.acos(ratio);
  }

  @Test
  void testCategorization() {
    assertThat(getCategory("extreme agile methodology")).isEqualTo("/technology/computers/programming/methodology");
    assertThat(getCategory("montessori education philosophy")).isEqualTo("/education/pedagogy");
  }
}
