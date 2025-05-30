package org.folio.linked.data.preprocessing.lccn.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

@UnitTest
class LccnNormalizerStructureaTest {

  private final LccnNormalizerStructureA normalizer = new LccnNormalizerStructureA();

  @ParameterizedTest
  @CsvSource({
    "abc12345678, 'abc12345678 '",
    "12345678 , '   12345678 '",
    "12345678, '   12345678 '",
    "a12345678, 'a  12345678 '",
    "a 12345678, 'a  12345678 '",
    "ab12345678, 'ab 12345678 '",
    " 12345678 , '   12345678 '",
    "  12345678 , '   12345678 '",
  })
  void apply_shouldAdd_appropriateSpaces(String input, String expectedOutput) {
    // expect
    assertEquals(expectedOutput, normalizer.apply(input));
  }

  @ParameterizedTest
  @ValueSource(strings = {
    "12345678", "a12345678", " 12345678"
  })
  void test_shouldReturn_true(String input) {
    // expect
    assertTrue(normalizer.test(input));
  }

  @ParameterizedTest
  @ValueSource(strings = {
    "0123456789", "a0123456789", " 0123456789"
  })
  void test_shouldReturn_false(String input) {
    // expect
    assertFalse(normalizer.test(input));
  }
}
