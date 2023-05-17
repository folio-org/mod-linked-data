package org.folio.linked.data.util;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class TextUtilTest {

  @ParameterizedTest
  @CsvSource({
    "This is a Test Value, this_is_a_test_value",
    ", ",
    "This#String@Has&Special*Characters, this_string_has_special_characters",
    "This is a very long value that exceeds the maximum slug length, "
      + "this_is_a_very_long_value_that_exceeds_the_maximum_sl"
  })
  void testSlugify(String given, String expected) {

    // when
    var actual = TextUtil.slugify(given);

    // then
    assertThat(actual).isEqualTo(expected);
  }
}
