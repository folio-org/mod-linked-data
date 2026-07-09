package org.folio.linked.data.validation.dto;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.util.ArrayList;
import java.util.List;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.Test;

@UnitTest
class AuthorityNameDtoValidatorTest {

  private final AuthorityNameDtoValidator validator = new AuthorityNameDtoValidator();

  @Test
  void shouldReturnFalse_ifGivenNameListIsNull() {
    // when
    boolean result = validator.isValid(null, null);

    // then
    assertThat(result).isFalse();
  }

  @Test
  void shouldReturnFalse_ifGivenNameListIsEmpty() {
    // given
    var name = new ArrayList<String>();

    // when
    boolean result = validator.isValid(name, null);

    // then
    assertThat(result).isFalse();
  }

  @Test
  void shouldReturnFalse_ifGivenNameListContainsOnlyBlankStrings() {
    // given
    var name = List.of("  ", "");

    // when
    boolean result = validator.isValid(name, null);

    // then
    assertThat(result).isFalse();
  }

  @Test
  void shouldReturnTrue_ifGivenNameListContainsNonBlankString() {
    // given
    var name = List.of("Authority Name");

    // when
    boolean result = validator.isValid(name, null);

    // then
    assertThat(result).isTrue();
  }
}
