package org.folio.linked.data.validation.dto;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.util.ArrayList;
import java.util.List;
import org.folio.linked.data.domain.dto.ParallelTitle;
import org.folio.linked.data.domain.dto.ParallelTitleField;
import org.folio.linked.data.domain.dto.PrimaryTitle;
import org.folio.linked.data.domain.dto.PrimaryTitleField;
import org.folio.linked.data.domain.dto.TitleFieldRequest;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.Test;

@UnitTest
class PrimaryTitleDtoValidatorTest {

  private final PrimaryTitleDtoValidator validator = new PrimaryTitleDtoValidator();

  @Test
  void shouldReturnTrue_ifGivenTitleFieldRequestListIsNull() {
    // when
    boolean result = validator.isValid(null, null);

    // then
    assertThat(result).isTrue();
  }

  @Test
  void shouldReturnFalse_ifGivenTitleFieldRequestListIsEmpty() {
    // given
    var titleFields = new ArrayList<TitleFieldRequest>();

    // when
    boolean result = validator.isValid(titleFields, null);

    // then
    assertThat(result).isFalse();
  }

  @Test
  void shouldReturnFalse_ifGivenTitleFieldRequestListContainsNoPrimaryTitle() {
    // given
    var titleFields = new ArrayList<TitleFieldRequest>();
    titleFields.add(new ParallelTitleField().parallelTitle(
      new ParallelTitle().mainTitle(List.of("parallel main title"))
    ));

    // when
    boolean result = validator.isValid(titleFields, null);

    // then
    assertThat(result).isFalse();
  }

  @Test
  void shouldReturnFalse_ifGivenTitleFieldRequestListContainsPrimaryTitleWithNoMainTitle() {
    // given
    var titleFields = new ArrayList<TitleFieldRequest>();
    titleFields.add(new ParallelTitleField().parallelTitle(
      new ParallelTitle().mainTitle(List.of("parallel main title"))
    ));
    titleFields.add(new PrimaryTitleField().primaryTitle(
      new PrimaryTitle().subTitle(List.of("primary sub title"))
    ));

    // when
    boolean result = validator.isValid(titleFields, null);

    // then
    assertThat(result).isFalse();
  }

  @Test
  void shouldReturnTrue_ifGivenTitleFieldListContainsPrimaryTitleWithMainTitle() {
    // given
    var titleFields = new ArrayList<TitleFieldRequest>();
    titleFields.add(new ParallelTitleField().parallelTitle(
      new ParallelTitle().mainTitle(List.of("parallel main title"))
    ));
    titleFields.add(new PrimaryTitleField().primaryTitle(
      new PrimaryTitle().mainTitle(List.of("primary main title"))
    ));

    // when
    boolean result = validator.isValid(titleFields, null);

    // then
    assertThat(result).isTrue();
  }

}
