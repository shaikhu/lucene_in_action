package lia.codec;

import org.apache.commons.codec.language.Metaphone;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class CodecTest {
  private final Metaphone metaphoner = new Metaphone();

  @Test
  void testMetaphone() {
    String encoding1 = metaphoner.encode("cute");
    String encoding2 = metaphoner.encode("cat");
    assertThat(encoding1).isEqualTo(encoding2);
  }
}
