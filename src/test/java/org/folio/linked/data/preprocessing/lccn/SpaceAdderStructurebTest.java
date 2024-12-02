package org.folio.linked.data.preprocessing.lccn;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.regex.Pattern;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@UnitTest
class SpaceAdderStructurebTest {

  private final SpaceAdderStructureB spaceAdder = new SpaceAdderStructureB();

  @Test
  void getPattern_shouldReturn_patternForStructureA() {
    // expect
    assertEquals(Pattern.compile("(?<!\\d)\\d{10}(?!\\d)").pattern(), spaceAdder.getPattern().pattern());
  }

  @ParameterizedTest
  @CsvSource({
    "0123456789, '  0123456789'",
    " 0123456789 , '  0123456789'"
  })
  void handle_shouldAdd_appropriateSpaces(String input, String expectedOutput) {
    // expect
    assertEquals(expectedOutput, spaceAdder.handle(input));
  }

}
