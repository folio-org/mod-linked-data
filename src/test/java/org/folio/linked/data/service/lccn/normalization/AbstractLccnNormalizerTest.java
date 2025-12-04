package org.folio.linked.data.service.lccn.normalization;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.regex.Pattern;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@UnitTest
class AbstractLccnNormalizerTest {

  private final AbstractLccnNormalizer normalizer = new TestLccnNormalizer();

  @ParameterizedTest
  @ValueSource(strings = {
    "0123456789",
    " 0123456789",
    "12345678",
    "12345678 ",
  })
  void isProcessable_shouldReturn_true(String input) {
    // expect
    assertTrue(normalizer.isProcessable(new StringBuilder(input)));
  }

  @ParameterizedTest
  @ValueSource(strings = {
    "ab0123456789",
    "ab0123456789c",
  })
  void isProcessable_shouldReturn_false(String input) {
    // expect
    assertFalse(normalizer.isProcessable(new StringBuilder(input)));
  }

  @ParameterizedTest
  @ValueSource(strings = {
    "ab0123456789", "abc0123456789"
  })
  void addPaces_shouldNot_addSpaces(String input) {
    //given
    var builder = new StringBuilder(input);
    var initialLength = builder.length();

    // when
    normalizer.addSpaces(builder);

    //then
    assertEquals(initialLength, builder.length());
  }

  @ParameterizedTest
  @ValueSource(strings = {
    "a0123456789", "abc12345678"
  })
  void addPaces_should_addSpaces(String input) {
    //given
    var builder = new StringBuilder(input);
    var initialLength = builder.length();

    // when
    normalizer.addSpaces(builder);

    //then
    assertNotEquals(initialLength, builder.length());
  }

  @Test
  void normalize_shouldReturn_emptyOptional() {
    // expect
    assertTrue(normalizer.normalize("abc").isEmpty());
  }

  @Test
  void normalize_shouldReturn_presentOptional() {
    // expect
    assertTrue(normalizer.normalize("123").isPresent());
  }

  private static final class TestLccnNormalizer extends AbstractLccnNormalizer {

    @Override
    public boolean test(String lccn) {
      return Pattern.compile("\\d").matcher(lccn).find();
    }

    @Override
    public String apply(String lccn) {
      return "";
    }
  }
}
