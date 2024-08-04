package lia.codec;

import org.apache.commons.codec.language.Metaphone;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CodecTest {
  private final Metaphone metaphoner = new Metaphone();

  @Test
  void testMetaphone() {
    var encodedText1 = metaphoner.encode("cute");
    var encodedText2 = metaphoner.encode("cat");
    assertThat(encodedText1).isEqualTo(encodedText2);
  }
}
