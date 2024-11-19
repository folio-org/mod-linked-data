package org.folio.linked.data.validation.dto;

import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.List;
import java.util.stream.Stream;
import org.folio.linked.data.domain.dto.LccnRequest;
import org.folio.linked.data.domain.dto.Status;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@UnitTest
class LccnPatternValidatorTest {

  private final LccnPatternValidator validator = new LccnPatternValidator();

  @ParameterizedTest
  @MethodSource("validLccnProvider")
  void shouldReturnTrue_ifLccnIsValid(String value, String link) {
    // given
    var lccnRequest = createLccnRequest(value, link);

    // expect
    assertTrue(validator.isValid(lccnRequest, null));
  }

  private static Stream<Arguments> validLccnProvider() {
    return Stream.of(
      // structure A
      arguments("   12345678 ", "http://id.loc.gov/vocabulary/mstatus/current"),
      arguments("n  12345678 ", "http://id.loc.gov/vocabulary/mstatus/current"),
      arguments("nn 12345678 ", "http://id.loc.gov/vocabulary/mstatus/current"),
      arguments("nnn12345678 ", "http://id.loc.gov/vocabulary/mstatus/current"),
      arguments("nnn12345678 ", null),
      arguments(" nnn123456789 ", "http://id.loc.gov/vocabulary/mstatus/cancinv"),

      // structure B
      arguments("  0123456789", "http://id.loc.gov/vocabulary/mstatus/current"),
      arguments("n 0123456781", "http://id.loc.gov/vocabulary/mstatus/current"),
      arguments("nn0123456789", "http://id.loc.gov/vocabulary/mstatus/current"),
      arguments("nn0123456789", null),
      arguments("mmm0123456789", "http://id.loc.gov/vocabulary/mstatus/cancinv"));
  }

  @ParameterizedTest
  @MethodSource("invalidLccnProvider")
  void shouldReturnFalse_ifLccnIsInvalid(String value, String link) {
    // given
    var lccnRequest = createLccnRequest(value, link);

    // expect
    assertFalse(validator.isValid(lccnRequest, null));
  }

  public static Stream<Arguments> invalidLccnProvider() {
    return Stream.of(
      // structure A
      arguments(" 12345678 ", "http://id.loc.gov/vocabulary/mstatus/current"),
      arguments("  12345678 ", "http://id.loc.gov/vocabulary/mstatus/current"),
      arguments(" n 12345678 ", "http://id.loc.gov/vocabulary/mstatus/current"),
      arguments("  n12345678 ", "http://id.loc.gov/vocabulary/mstatus/current"),
      arguments("   1234567 ", "http://id.loc.gov/vocabulary/mstatus/current"),
      arguments("   12345678", "http://id.loc.gov/vocabulary/mstatus/current"),
      arguments(" nnn123456789 ", "http://id.loc.gov/vocabulary/mstatus/current"),
      arguments("nnn123456789 ", "http://id.loc.gov/vocabulary/mstatus/current"),
      arguments("nnnn12345678 ", "http://id.loc.gov/vocabulary/mstatus/current"),
      arguments("nNn12345678 ", "http://id.loc.gov/vocabulary/mstatus/current"),
      arguments("nNn12345678 ", null),
      arguments("n-n12345678 ", "http://id.loc.gov/vocabulary/mstatus/current"),

      // structure B
      arguments(" 0123456789", "http://id.loc.gov/vocabulary/mstatus/current"),
      arguments(" m123456789", "http://id.loc.gov/vocabulary/mstatus/current"),
      arguments("  m123456789 ", "http://id.loc.gov/vocabulary/mstatus/current"),
      arguments(" mm123456789 ", "http://id.loc.gov/vocabulary/mstatus/current"),
      arguments("mm123456789 ", "http://id.loc.gov/vocabulary/mstatus/current"),
      arguments("mmm0123456789", "http://id.loc.gov/vocabulary/mstatus/current"),
      arguments("nN0123456789", "http://id.loc.gov/vocabulary/mstatus/current"),
      arguments("nN0123456789", null),
      arguments("n-0123456789", "http://id.loc.gov/vocabulary/mstatus/current"));
  }

  private LccnRequest createLccnRequest(String value, String link) {
    return new LccnRequest()
      .value(List.of(value))
      .status(link == null ? emptyList() : List.of(new Status().link(List.of(link))));
  }
}
