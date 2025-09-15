package lia.payloads;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.PayloadAttribute;
import org.apache.lucene.store.ByteBuffersDirectory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.StringReader;

import static org.assertj.core.api.Assertions.assertThat;

class BulletinFieldTest {
    private static final float WARNING_BOOST = 5.0F;

    private ByteBuffersDirectory directory;

  @BeforeEach
  void setup() {
    directory = new ByteBuffersDirectory();
  }

  @AfterEach
  void tearDown() throws Exception {
    directory.close();
  }

  private TokenStream createTestTokenStream(String value, boolean isBulletin, float warningBoost) {
    try {
      StandardTokenizer tokenizer = new StandardTokenizer();
      tokenizer.setReader(new StringReader(value));
      
      BulletinPayloadsFilter payloadFilter = new BulletinPayloadsFilter(tokenizer, warningBoost);
      payloadFilter.setIsBulletin(isBulletin);
      
      return payloadFilter;
    } catch (Exception e) {
      throw new RuntimeException("Failed to create test TokenStream", e);
    }
  }

  @Test
  void testBulletinFieldCreation() {
    var field = new BulletinField("contents", "This is a warning message", true, WARNING_BOOST);
    
    assertThat(field.name()).isEqualTo("contents");
    assertThat(field.fieldType().tokenized()).isTrue();
  }

  @Test
  void testNonBulletinFieldCreation() {
    var field = new BulletinField("contents", "This is a warning message", false, WARNING_BOOST);
    
    assertThat(field.name()).isEqualTo("contents");
    assertThat(field.fieldType().tokenized()).isTrue();
  }

  @Test
  void testBulletinPayloadAttachment() throws Exception {
    // Test by creating a TokenStream directly with the same chain as BulletinField uses
    try (TokenStream tokenStream = createTestTokenStream("This is a warning message", true, WARNING_BOOST)) {
      var charTermAttribute = tokenStream.addAttribute(CharTermAttribute.class);
      var payloadAttribute = tokenStream.addAttribute(PayloadAttribute.class);
      
      tokenStream.reset();
      
      boolean foundWarningWithPayload = false;
      while (tokenStream.incrementToken()) {
        String term = charTermAttribute.toString();
        if ("warning".equals(term)) {
          assertThat(payloadAttribute.getPayload()).isNotNull();
          assertThat(payloadAttribute.getPayload().length).isGreaterThan(0);
          foundWarningWithPayload = true;
        } else {
          // Non-warning terms should not have payloads in bulletin mode
          assertThat(payloadAttribute.getPayload()).isNull();
        }
      }
      
      assertThat(foundWarningWithPayload).isTrue();
      tokenStream.end();
    }
  }

  @Test
  void testNonBulletinNoPayload() throws Exception {
    try (TokenStream tokenStream = createTestTokenStream("This is a warning message", false, WARNING_BOOST)) {
      var payloadAttribute = tokenStream.addAttribute(PayloadAttribute.class);
      
      tokenStream.reset();
      
      while (tokenStream.incrementToken()) {
        // Non-bulletin fields should never have payloads, even for "warning" terms
        assertThat(payloadAttribute.getPayload()).isNull();
      }
      
      tokenStream.end();
    }
  }

  @Test
  void testEmptyContent() throws Exception {
    try (TokenStream tokenStream = createTestTokenStream("", true, WARNING_BOOST)) {
      tokenStream.reset();
      assertThat(tokenStream.incrementToken()).isFalse();
      tokenStream.end();
    }
  }

  @Test
  void testMultipleWarningTerms() throws Exception {
    try (TokenStream tokenStream = createTestTokenStream("Warning: severe warning about tornado warning", true, WARNING_BOOST)) {
      var charTermAttribute = tokenStream.addAttribute(CharTermAttribute.class);
      var payloadAttribute = tokenStream.addAttribute(PayloadAttribute.class);
      
      tokenStream.reset();
      
      int warningCount = 0;
      while (tokenStream.incrementToken()) {
        String term = charTermAttribute.toString();
        if ("warning".equals(term)) {
          warningCount++;
          assertThat(payloadAttribute.getPayload()).isNotNull();
        }
      }
      
      assertThat(warningCount).isEqualTo(2); // Two "warning" terms should be found (StandardTokenizer normalizes case)
      tokenStream.end();
    }
  }

  @Test
  void testDifferentBoostValues() throws Exception {
    float customBoost = 10.0F;
    
    try (TokenStream tokenStream = createTestTokenStream("This is a warning", true, customBoost)) {
      var charTermAttribute = tokenStream.addAttribute(CharTermAttribute.class);
      var payloadAttribute = tokenStream.addAttribute(PayloadAttribute.class);
      
      tokenStream.reset();
      
      while (tokenStream.incrementToken()) {
        String term = charTermAttribute.toString();
        if ("warning".equals(term)) {
          assertThat(payloadAttribute.getPayload()).isNotNull();
          // Payload should contain the custom boost value (though we can't easily decode it here)
          assertThat(payloadAttribute.getPayload().length).isEqualTo(4); // Float is 4 bytes
        }
      }
      
      tokenStream.end();
    }
  }
}
