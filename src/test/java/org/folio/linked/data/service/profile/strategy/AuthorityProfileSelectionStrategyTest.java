package org.folio.linked.data.service.profile.strategy;

import static org.assertj.core.api.Assertions.assertThat;

import org.folio.ld.dictionary.ResourceTypeDictionary;
import org.folio.linked.data.model.entity.Resource;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@UnitTest
class AuthorityProfileSelectionStrategyTest {

  private final AuthorityProfileSelectionStrategy strategy = new AuthorityProfileSelectionStrategy();


  @ParameterizedTest
  @CsvSource({
    "FAMILY, true",
    "FORM, true",
    "JURISDICTION, true",
    "MEETING, true",
    "ORGANIZATION, true",
    "PERSON, true",
    "PLACE, true",
    "TEMPORAL, true",
    "TOPIC, true",
    "INSTANCE, false",
    "WORK, false",
    "HUB, false"
  })
  void supports_shouldReturnTrueForAuthorityType_andFalseOtherwise(ResourceTypeDictionary type, boolean isSupported) {
    // given
    var resource = new Resource().addTypes(type);

    // when
    var result = strategy.supports(resource);

    // then
    assertThat(result).isEqualTo(isSupported);
  }

  @ParameterizedTest
  @CsvSource({
    "FAMILY, 8",
    "FORM, 9",
    "JURISDICTION, 10",
    "MEETING, 11",
    "ORGANIZATION, 12",
    "PERSON, 13",
    "PLACE, 14",
    "TEMPORAL, 15",
    "TOPIC, 16"
  })
  void selectProfile_shouldReturnExpectedProfileId(ResourceTypeDictionary type, int expectedProfileId) {
    // given
    var resource = new Resource().addTypes(type);

    // when
    var result = strategy.selectProfile(resource);

    // then
    assertThat(result).isEqualTo(expectedProfileId);
  }

  @ParameterizedTest
  @CsvSource({
    "8, true",
    "9, true",
    "10, true",
    "11, true",
    "12, true",
    "13, true",
    "14, true",
    "15, true",
    "16, true",
    "999, false",
    "1, false"
  })
  void supportsProfileId_shouldReturnTrueForAuthorityProfileId_andFalseOtherwise(int profileId,
                                                                                 boolean isAuthorityProfileId) {
    // when
    var result = strategy.supportsProfileId(profileId);

    // then
    assertThat(result).isEqualTo(isAuthorityProfileId);
  }

  @ParameterizedTest
  @CsvSource({
    "8, FAMILY",
    "9, FORM",
    "10, JURISDICTION",
    "11, MEETING",
    "12, ORGANIZATION",
    "13, PERSON",
    "14, PLACE",
    "15, TEMPORAL",
    "16, TOPIC"
  })
  void resourceType_shouldReturnCorrespondingAuthorityType(int profileId, ResourceTypeDictionary expectedType) {
    // when
    var result = strategy.resourceType(profileId);

    // then
    assertThat(result).isEqualTo(expectedType);
  }
}
