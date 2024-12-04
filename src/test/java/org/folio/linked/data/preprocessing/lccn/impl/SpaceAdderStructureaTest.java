package org.folio.linked.data.preprocessing.lccn.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.regex.Pattern;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@UnitTest
class SpaceAdderStructureaTest {

  private final SpaceAdderStructureA spaceAdder = new SpaceAdderStructureA();

  @Test
  void getPattern_shouldReturn_patternForStructureA() {
    // expect
    assertEquals(Pattern.compile("(?<!\\d)\\d{8}(?!\\d)").pattern(), spaceAdder.getPattern().pattern());
  }

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
  void handle_shouldAdd_appropriateSpaces(String input, String expectedOutput) {
    // expect
    assertEquals(expectedOutput, spaceAdder.handle(input));
  }
}
