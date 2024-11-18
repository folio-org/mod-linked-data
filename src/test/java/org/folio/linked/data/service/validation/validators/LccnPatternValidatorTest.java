package org.folio.linked.data.service.validation.validators;

import static org.assertj.core.api.Assertions.assertThatException;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.List;
import java.util.stream.Stream;
import org.folio.linked.data.domain.dto.InstanceField;
import org.folio.linked.data.domain.dto.InstanceRequest;
import org.folio.linked.data.domain.dto.LccnField;
import org.folio.linked.data.domain.dto.LccnRequest;
import org.folio.linked.data.domain.dto.PrimaryTitleField;
import org.folio.linked.data.domain.dto.ResourceRequestDto;
import org.folio.linked.data.domain.dto.Status;
import org.folio.linked.data.service.validation.ValidationContext;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;

@UnitTest
@ExtendWith(MockitoExtension.class)
class LccnPatternValidatorTest {

  private final LccnPatternValidator validator = new LccnPatternValidator();

  @ParameterizedTest
  @MethodSource("withoutExceptionProvider")
  void validateShouldNotThrowException(String value, String link) {
    // given
    var resourceRequestDto = createResourceRequestDto(value, link);

    // expect
    assertThatNoException().isThrownBy(() -> validator.validate(new ValidationContext<>(resourceRequestDto)));
  }

  private static Stream<Arguments> withoutExceptionProvider() {
    return Stream.of(
      // structure A
      arguments("   12345678 ", "http://id.loc.gov/vocabulary/mstatus/current"),
      arguments("n  12345678 ", "http://id.loc.gov/vocabulary/mstatus/current"),
      arguments("nn 12345678 ", "http://id.loc.gov/vocabulary/mstatus/current"),
      arguments("nnn12345678 ", "http://id.loc.gov/vocabulary/mstatus/current"),
      arguments(" nnn123456789 ", "http://id.loc.gov/vocabulary/mstatus/cancinv"),

      // structure B
      arguments("  0123456789", "http://id.loc.gov/vocabulary/mstatus/current"),
      arguments("n 0123456781", "http://id.loc.gov/vocabulary/mstatus/current"),
      arguments("nn0123456789", "http://id.loc.gov/vocabulary/mstatus/current"),
      arguments("mmm0123456789", "http://id.loc.gov/vocabulary/mstatus/cancinv"));
  }

  @ParameterizedTest
  @MethodSource("withExceptionProvider")
  void validateShouldThrowException(String value) {
    // given
    var resourceRequestDto = createResourceRequestDto(value, "http://id.loc.gov/vocabulary/mstatus/current");

    // expect
    assertThatException().isThrownBy(() -> validator.validate(new ValidationContext<>(resourceRequestDto)));
  }

  public static Stream<Arguments> withExceptionProvider() {
    return Stream.of(
      // structure A
      arguments(" 12345678 "),
      arguments("  12345678 "),
      arguments(" n 12345678 "),
      arguments("  n12345678 "),
      arguments("   1234567 "),
      arguments("   12345678"),
      arguments(" nnn123456789 "),
      arguments("nnn123456789 "),
      arguments("nnnn12345678 "),
      arguments("nNn12345678 "),
      arguments("n-n12345678 "),

      // structure B
      arguments(" 0123456789"),
      arguments(" m123456789"),
      arguments("  m123456789 "),
      arguments(" mm123456789 "),
      arguments("mm123456789 "),
      arguments("mmm0123456789"),
      arguments("nN0123456789"),
      arguments("n-0123456789"));
  }

  private ResourceRequestDto createResourceRequestDto(String value, String link) {
    return new ResourceRequestDto().resource(
      new InstanceField().instance(
        new InstanceRequest(List.of(new PrimaryTitleField())).map(List.of(
          new LccnField().lccn(
            new LccnRequest().value(List.of(value)).status(List.of(new Status().link(List.of(link)))))))));
  }
}
