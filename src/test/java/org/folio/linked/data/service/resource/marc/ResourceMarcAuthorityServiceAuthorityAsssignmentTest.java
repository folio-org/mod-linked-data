package org.folio.linked.data.service.resource.marc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.ld.dictionary.ResourceTypeDictionary.CONCEPT;
import static org.folio.ld.dictionary.ResourceTypeDictionary.FORM;
import static org.folio.ld.dictionary.ResourceTypeDictionary.HUB;
import static org.folio.ld.dictionary.ResourceTypeDictionary.PERSON;
import static org.folio.linked.data.domain.dto.AssignmentCheckResponseDto.InvalidAssignmentReasonEnum.NOT_VALID_FOR_TARGET;
import static org.folio.linked.data.domain.dto.AssignmentCheckResponseDto.InvalidAssignmentReasonEnum.UNSUPPORTED_MARC;
import static org.folio.linked.data.service.resource.marc.AssignAuthorityTarget.CREATOR_OF_WORK;
import static org.folio.linked.data.service.resource.marc.AssignAuthorityTarget.SUBJECT_OF_WORK;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import org.folio.ld.dictionary.ResourceTypeDictionary;
import org.folio.marc4ld.service.marc2ld.authority.MarcAuthority2ldMapper;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@UnitTest
@ExtendWith(MockitoExtension.class)
class ResourceMarcAuthorityServiceAuthorityAsssignmentTest {

  private static final String VALID_MARC = """
        {
           "fields":[
              { "010":{ "subfields":[ { "a":"no2023016747" } ] } }
           ]
        }
      """;

  @InjectMocks
  private ResourceMarcAuthorityServiceImpl resourceMarcAuthorityService;
  @Mock
  private MarcAuthority2ldMapper marcAuthority2ldMapper;

  @ParameterizedTest
  @MethodSource("authorityAssignmentArguments_validTargets")
  void validateAuthorityAssignment_shouldReturnTrueIfValidTarget(Set<ResourceTypeDictionary> authorityTypes,
                                      AssignAuthorityTarget target) {
    // given
    when(marcAuthority2ldMapper.fromMarcJson(any()))
      .thenReturn(List.of(new org.folio.ld.dictionary.model.Resource().setTypes(authorityTypes)));

    // when
    var result = resourceMarcAuthorityService.validateAuthorityAssignment(VALID_MARC, target);

    // then
    assertThat(result.getValidAssignment()).isTrue();
    assertThat(result.getInvalidAssignmentReason()).isNull();
    verify(marcAuthority2ldMapper).fromMarcJson(any());
  }

  @ParameterizedTest
  @MethodSource("authorityAssignmentArguments_invalidTargets")
  void validateAuthorityAssignment_shouldReturnFalseIfInValidTarget(Set<ResourceTypeDictionary> authorityTypes,
                                                                 AssignAuthorityTarget target) {
    // given
    when(marcAuthority2ldMapper.fromMarcJson(any()))
      .thenReturn(List.of(new org.folio.ld.dictionary.model.Resource().setTypes(authorityTypes)));

    // when
    var result = resourceMarcAuthorityService.validateAuthorityAssignment(VALID_MARC, target);

    // then
    assertThat(result.getValidAssignment()).isFalse();
    assertThat(result.getInvalidAssignmentReason()).isEqualTo(NOT_VALID_FOR_TARGET);
    verify(marcAuthority2ldMapper).fromMarcJson(any());
  }

  @Test
  void validateAuthorityAssignment_shouldReturnFalse_ifAuthorityIsEmpty() {
    // given
    when(marcAuthority2ldMapper.fromMarcJson(any())).thenReturn(List.of());

    // when
    var result = resourceMarcAuthorityService.validateAuthorityAssignment(VALID_MARC, CREATOR_OF_WORK);

    // then
    assertThat(result.getValidAssignment()).isFalse();
    assertThat(result.getInvalidAssignmentReason()).isEqualTo(UNSUPPORTED_MARC);
  }

  private static Stream<Arguments> authorityAssignmentArguments_validTargets() {
    return Stream.of(
      Arguments.of(Set.of(PERSON), CREATOR_OF_WORK),
      Arguments.of(Set.of(FORM), SUBJECT_OF_WORK),
      Arguments.of(Set.of(CONCEPT, FORM), SUBJECT_OF_WORK),
      Arguments.of(Set.of(HUB), SUBJECT_OF_WORK)
    );
  }

  private static Stream<Arguments> authorityAssignmentArguments_invalidTargets() {
    return Stream.of(
      Arguments.of(Set.of(CONCEPT, PERSON), CREATOR_OF_WORK),
      Arguments.of(Set.of(PERSON, FORM), SUBJECT_OF_WORK),
      Arguments.of(Set.of(CONCEPT, FORM, PERSON), SUBJECT_OF_WORK),
      Arguments.of(Set.of(HUB), CREATOR_OF_WORK)
    );
  }
}
