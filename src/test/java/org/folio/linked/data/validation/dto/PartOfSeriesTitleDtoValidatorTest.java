package org.folio.linked.data.validation.dto;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.util.ArrayList;
import org.folio.linked.data.domain.dto.PartOfSeries;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@UnitTest
@ExtendWith(MockitoExtension.class)
class PartOfSeriesTitleDtoValidatorTest {

  private final PartOfSeriesTitleDtoValidator validator = new PartOfSeriesTitleDtoValidator();

  @Test
  void shouldReturnTrue_ifSeriesIsNull() {
    // when
    boolean result = validator.isValid(null, null);

    // then
    assertThat(result).isTrue();
  }

  @Test
  void shouldReturnTrue_ifAllSeriesAreEmpty() {
    // given
    var series = new ArrayList<PartOfSeries>();
    series.add(new PartOfSeries());
    series.add(new PartOfSeries());

    // when
    boolean result = validator.isValid(series, null);

    // then
    assertThat(result).isTrue();    
  }

  @Test
  void shouldReturnTrue_ifOneSeriesWithOneTitle() {
    // given
    var series = new ArrayList<PartOfSeries>();
    series.add(new PartOfSeries().addNameItem("non-empty"));

    // when
    boolean result = validator.isValid(series, null);

    // then
    assertThat(result).isTrue();    
  }

  @Test
  void shouldReturnTrue_ifTwoSeriesEachWithTitle() {
    // given
    var series = new ArrayList<PartOfSeries>();
    series.add(new PartOfSeries().addNameItem("anything"));
    series.add(new PartOfSeries().addNameItem("title like"));

    // when
    boolean result = validator.isValid(series, null);

    // then
    assertThat(result).isTrue();    
  }

  @Test
  void shouldReturnTrue_ifOneSeriesWithTitleOneSeriesEmpty() {
    // given
    var series = new ArrayList<PartOfSeries>();
    series.add(new PartOfSeries());
    series.add(new PartOfSeries().addNameItem("non-empty"));

    // when
    boolean result = validator.isValid(series, null);

    // then
    assertThat(result).isTrue();    
  }

  @Test
  void shouldReturnFalse_ifOneSeriesWithoutTitle() {
    // given
    var series = new ArrayList<PartOfSeries>();
    series.add(new PartOfSeries().addIssnItem("issn")
      .addVolumeItem("5"));

    // when
    boolean result = validator.isValid(series, null);

    // then
    assertThat(result).isFalse();    
  }

  @Test
  void shouldReturnFalse_ifAllSeriesWithoutTitle() {
    // given
    var series = new ArrayList<PartOfSeries>();
    series.add(new PartOfSeries().addIssnItem("issn1")
      .addVolumeItem("10"));
    series.add(new PartOfSeries().addIssnItem("issn2")
      .addVolumeItem("1"));

    // when
    boolean result = validator.isValid(series, null);

    // then
    assertThat(result).isFalse();    
  }

  @Test
  void shouldReturnFalse_ifTwoSeriesOneWithoutTitle() {
    // given
    var series = new ArrayList<PartOfSeries>();
    series.add(new PartOfSeries().addNameItem("non-empty")
      .addIssnItem("issn0")
      .addVolumeItem("2"));
    series.add(new PartOfSeries().addIssnItem("issn1")
      .addVolumeItem("3"));    

    // when
    boolean result = validator.isValid(series, null);

    // then
    assertThat(result).isFalse();    
  }
}
