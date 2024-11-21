package org.folio.linked.data.validation.dto;

import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.Mockito.doReturn;

import java.util.List;
import java.util.stream.Stream;
import org.folio.linked.data.domain.dto.LccnRequest;
import org.folio.linked.data.domain.dto.Status;
import org.folio.linked.data.validation.spec.SpecProvider;
import org.folio.rspec.domain.dto.SpecificationRuleDto;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@UnitTest
@ExtendWith(MockitoExtension.class)
class LccnPatternValidatorTest {

  @Mock
  private SpecProvider specProvider;

  @InjectMocks
  private LccnPatternValidator validator;

  @ParameterizedTest
  @MethodSource("positiveCaseProvider")
  void shouldReturnTrue_ifLccnIsValid(String value, String link, List<SpecificationRuleDto> specRules) {
    // given
    var lccnRequest = createLccnRequest(value, link);
    doReturn(specRules).when(specProvider).getSpecRules();

    // expect
    assertTrue(validator.isValid(lccnRequest, null));
  }

  private static Stream<Arguments> positiveCaseProvider() {
    return Stream.of(
      // structure A
      arguments("   12345678 ", "http://id.loc.gov/vocabulary/mstatus/current", List.of(createSpecRule(true))),
      arguments("n  12345678 ", "http://id.loc.gov/vocabulary/mstatus/current", List.of(createSpecRule(true))),
      arguments("nn 12345678 ", "http://id.loc.gov/vocabulary/mstatus/current", List.of(createSpecRule(true))),
      arguments("nnn12345678 ", "http://id.loc.gov/vocabulary/mstatus/current", List.of(createSpecRule(true))),
      arguments("nnn12345678 ", null, List.of(createSpecRule(true))),
      arguments(" nnn123456789 ", "http://id.loc.gov/vocabulary/mstatus/cancinv", List.of(createSpecRule(true))),
      arguments(" nnn123456789 ", "http://id.loc.gov/vocabulary/mstatus/current", List.of(createSpecRule(false))),
      arguments(" nnn123456789 ", "http://id.loc.gov/vocabulary/mstatus/current", List.of()),

      // structure B
      arguments("  0123456789", "http://id.loc.gov/vocabulary/mstatus/current", List.of(createSpecRule(true))),
      arguments("n 0123456781", "http://id.loc.gov/vocabulary/mstatus/current", List.of(createSpecRule(true))),
      arguments("nn0123456789", "http://id.loc.gov/vocabulary/mstatus/current", List.of(createSpecRule(true))),
      arguments("nn0123456789", null, List.of(createSpecRule(true))),
      arguments("mmm0123456789", "http://id.loc.gov/vocabulary/mstatus/cancinv", List.of(createSpecRule(true))),
      arguments("mmm0123456789", "http://id.loc.gov/vocabulary/mstatus/current", List.of(createSpecRule(false))),
      arguments("mmm0123456789", "http://id.loc.gov/vocabulary/mstatus/current", List.of()));
  }

  @ParameterizedTest
  @MethodSource("negativeCaseProvider")
  void shouldReturnFalse_ifLccnIsInvalid(String value, String link, List<SpecificationRuleDto> specRules) {
    // given
    var lccnRequest = createLccnRequest(value, link);
    doReturn(specRules).when(specProvider).getSpecRules();

    // expect
    assertFalse(validator.isValid(lccnRequest, null));
  }

  public static Stream<Arguments> negativeCaseProvider() {
    return Stream.of(
      // structure A
      arguments(" 12345678 ", "http://id.loc.gov/vocabulary/mstatus/current", List.of(createSpecRule(true))),
      arguments("  12345678 ", "http://id.loc.gov/vocabulary/mstatus/current", List.of(createSpecRule(true))),
      arguments(" n 12345678 ", "http://id.loc.gov/vocabulary/mstatus/current", List.of(createSpecRule(true))),
      arguments("  n12345678 ", "http://id.loc.gov/vocabulary/mstatus/current", List.of(createSpecRule(true))),
      arguments("   1234567 ", "http://id.loc.gov/vocabulary/mstatus/current", List.of(createSpecRule(true))),
      arguments("   12345678", "http://id.loc.gov/vocabulary/mstatus/current", List.of(createSpecRule(true))),
      arguments(" nnn123456789 ", "http://id.loc.gov/vocabulary/mstatus/current", List.of(createSpecRule(true))),
      arguments("nnn123456789 ", "http://id.loc.gov/vocabulary/mstatus/current", List.of(createSpecRule(true))),
      arguments("nnnn12345678 ", "http://id.loc.gov/vocabulary/mstatus/current", List.of(createSpecRule(true))),
      arguments("nNn12345678 ", "http://id.loc.gov/vocabulary/mstatus/current", List.of(createSpecRule(true))),
      arguments("nNn12345678 ", null, List.of(createSpecRule(true))),
      arguments("n-n12345678 ", "http://id.loc.gov/vocabulary/mstatus/current", List.of(createSpecRule(true))),

      // structure B
      arguments(" 0123456789", "http://id.loc.gov/vocabulary/mstatus/current", List.of(createSpecRule(true))),
      arguments(" m123456789", "http://id.loc.gov/vocabulary/mstatus/current", List.of(createSpecRule(true))),
      arguments("  m123456789 ", "http://id.loc.gov/vocabulary/mstatus/current", List.of(createSpecRule(true))),
      arguments(" mm123456789 ", "http://id.loc.gov/vocabulary/mstatus/current", List.of(createSpecRule(true))),
      arguments("mm123456789 ", "http://id.loc.gov/vocabulary/mstatus/current", List.of(createSpecRule(true))),
      arguments("mmm0123456789", "http://id.loc.gov/vocabulary/mstatus/current", List.of(createSpecRule(true))),
      arguments("nN0123456789", "http://id.loc.gov/vocabulary/mstatus/current", List.of(createSpecRule(true))),
      arguments("nN0123456789", null, List.of(createSpecRule(true))),
      arguments("n-0123456789", "http://id.loc.gov/vocabulary/mstatus/current", List.of(createSpecRule(true))));
  }

  private LccnRequest createLccnRequest(String value, String link) {
    return new LccnRequest()
      .value(List.of(value))
      .status(link == null ? emptyList() : List.of(new Status().link(List.of(link))));
  }

  private static SpecificationRuleDto createSpecRule(boolean enabled) {
    var specRule = new SpecificationRuleDto();
    specRule.setCode("invalidLccnSubfieldValue");
    specRule.setEnabled(enabled);
    return specRule;
  }
}
