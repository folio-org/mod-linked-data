package org.folio.linked.data.service.profile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.linked.data.test.TestUtil.TEST_JSON_MAPPER;

import java.util.List;
import java.util.Map;
import org.folio.ld.dictionary.PredicateDictionary;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@UnitTest
class WorkRelationshipPredicateDictionaryTest {

  @ParameterizedTest
  @ValueSource(strings = {"profiles/work-books.json", "profiles/work-serials.json"})
  void hubRelationshipUris_shouldExistInPredicateDictionary(String profileFile) {
    var profile = readProfile(profileFile);
    var relationshipUris = extractRelationshipUris(profile, "Profile:Work:Hubs:Relationship:");

    assertThat(relationshipUris)
      .as("Hub relationship URIs must exist in %s", profileFile)
      .isNotEmpty();

    relationshipUris.forEach(uri -> assertThat(PredicateDictionary.fromUri(uri))
      .as("PredicateDictionary.fromUri should resolve URI %s from %s", uri, profileFile)
      .isPresent());
  }

  @ParameterizedTest
  @ValueSource(strings = {"profiles/work-books.json", "profiles/work-serials.json"})
  void languageRelationshipUris_shouldExistInPredicateDictionary(String profileFile) {
    var profile = readProfile(profileFile);
    var relationshipUris = extractRelationshipUris(profile, "Profile:Work:LanguageCode:Relationship:");

    assertThat(relationshipUris)
      .as("Language code relationship URIs must exist in %s", profileFile)
      .isNotEmpty();

    relationshipUris.forEach(uri -> assertThat(PredicateDictionary.fromUri(uri))
      .as("PredicateDictionary.fromUri should resolve URI %s from %s", uri, profileFile)
      .isPresent());
  }

  @SuppressWarnings("unchecked")
  private List<String> extractRelationshipUris(Map<String, Object> profile, String relationshipPrefix) {
    var values = (List<Map<String, Object>>) profile.get("value");

    return values.stream()
      .filter(field -> {
        var id = field.get("id");
        return id instanceof String idValue && idValue.startsWith(relationshipPrefix);
      })
      .map(field -> field.get("uriBFLite"))
      .map(String.class::cast)
      .toList();
  }

  @SuppressWarnings("unchecked")
  private Map<String, Object> readProfile(String profileFile) {
    var stream = getClass().getClassLoader().getResourceAsStream(profileFile);
    assertThat(stream)
      .as("Profile file %s must exist on classpath", profileFile)
      .isNotNull();
    return TEST_JSON_MAPPER.readValue(stream, Map.class);
  }
}
