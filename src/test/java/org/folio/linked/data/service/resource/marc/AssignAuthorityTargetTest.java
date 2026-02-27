package org.folio.linked.data.service.resource.marc;

import static org.folio.ld.dictionary.ResourceTypeDictionary.CONCEPT;
import static org.folio.ld.dictionary.ResourceTypeDictionary.FAMILY;
import static org.folio.ld.dictionary.ResourceTypeDictionary.FORM;
import static org.folio.ld.dictionary.ResourceTypeDictionary.HUB;
import static org.folio.ld.dictionary.ResourceTypeDictionary.JURISDICTION;
import static org.folio.ld.dictionary.ResourceTypeDictionary.MEETING;
import static org.folio.ld.dictionary.ResourceTypeDictionary.ORGANIZATION;
import static org.folio.ld.dictionary.ResourceTypeDictionary.PERSON;
import static org.folio.ld.dictionary.ResourceTypeDictionary.PLACE;
import static org.folio.ld.dictionary.ResourceTypeDictionary.TEMPORAL;
import static org.folio.ld.dictionary.ResourceTypeDictionary.TOPIC;
import static org.folio.linked.data.service.resource.marc.AssignAuthorityTarget.CREATOR_OF_WORK;
import static org.folio.linked.data.service.resource.marc.AssignAuthorityTarget.SUBJECT_OF_WORK;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.folio.ld.dictionary.ResourceTypeDictionary;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@UnitTest
class AssignAuthorityTargetTest {

  private static Stream<Arguments> authorityAssignmentTargetTypes() {
    return Stream.of(
      Arguments.of(
        CREATOR_OF_WORK,
        Set.of(PERSON, FAMILY, ORGANIZATION, JURISDICTION, MEETING),
        Set.of(), true),
      Arguments.of(
        CREATOR_OF_WORK,
        Set.of(PERSON, FAMILY, ORGANIZATION, JURISDICTION, MEETING),
        Set.of(CONCEPT), false),
      Arguments.of(
        CREATOR_OF_WORK,
        getAllTypesExcluding(Set.of(PERSON, FAMILY, ORGANIZATION, JURISDICTION, MEETING)),
        Set.of(), false),
      Arguments.of(
        SUBJECT_OF_WORK,
        Set.of(FAMILY, ORGANIZATION, JURISDICTION, MEETING, PERSON, FORM, TOPIC, TEMPORAL, PLACE, HUB),
        Set.of(CONCEPT), true),
      Arguments.of(
        SUBJECT_OF_WORK,
        Set.of(FAMILY, ORGANIZATION, JURISDICTION, MEETING, PERSON, FORM, TOPIC, TEMPORAL, PLACE, HUB),
        Set.of(), true),
      Arguments.of(
        SUBJECT_OF_WORK,
        getAllTypesExcluding(
          Set.of(FAMILY, ORGANIZATION, JURISDICTION, MEETING, PERSON, FORM, TOPIC, TEMPORAL, PLACE, HUB)
        ),
        Set.of(), false),
      Arguments.of(
        SUBJECT_OF_WORK,
        getAllTypesExcluding(
          Set.of(FAMILY, ORGANIZATION, JURISDICTION, MEETING, PERSON, FORM, TOPIC, TEMPORAL, PLACE, HUB)
        ),
        Set.of(CONCEPT), false)
    );
  }

  @ParameterizedTest
  @MethodSource("authorityAssignmentTargetTypes")
  void checkAuthorityAssignments(AssignAuthorityTarget target,
                                 Set<ResourceTypeDictionary> allowed,
                                 Set<ResourceTypeDictionary> any,
                                 boolean expected) {
    allowed.forEach(e -> {
      var combined = new HashSet<>(any);
      combined.add(e);
      assertEquals(expected, target.isCompatibleWith(combined),
        () -> "Unexpected compatibility for item: " + e
          + ", target: " + target
          + ", combined types: " + combined);
    });
  }

  private static Set<ResourceTypeDictionary> getAllTypesExcluding(Set<ResourceTypeDictionary> allowed) {
    return Stream.of(ResourceTypeDictionary.values())
      .filter(v -> !allowed.contains(v))
      .collect(Collectors.toSet());
  }
}
