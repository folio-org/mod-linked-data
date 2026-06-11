package org.folio.linked.data.service.profile.strategy;

import static org.folio.ld.dictionary.ResourceTypeDictionary.FAMILY;
import static org.folio.ld.dictionary.ResourceTypeDictionary.FORM;
import static org.folio.ld.dictionary.ResourceTypeDictionary.JURISDICTION;
import static org.folio.ld.dictionary.ResourceTypeDictionary.MEETING;
import static org.folio.ld.dictionary.ResourceTypeDictionary.ORGANIZATION;
import static org.folio.ld.dictionary.ResourceTypeDictionary.PERSON;
import static org.folio.ld.dictionary.ResourceTypeDictionary.PLACE;
import static org.folio.ld.dictionary.ResourceTypeDictionary.TOPIC;

import java.util.Map;
import org.folio.ld.dictionary.ResourceTypeDictionary;
import org.folio.linked.data.model.entity.Resource;
import org.springframework.stereotype.Component;

@Component
public class AuthorityProfileSelectionStrategy implements ProfileSelectionStrategy {
  private static final int AUTHORITY_FAMILY_PROFILE_ID = 8;
  private static final int AUTHORITY_FORM_PROFILE_ID = 9;
  private static final int AUTHORITY_JURISDICTION_PROFILE_ID = 10;
  private static final int AUTHORITY_MEETING_PROFILE_ID = 11;
  private static final int AUTHORITY_ORGANIZATION_PROFILE_ID = 12;
  private static final int AUTHORITY_PERSON_PROFILE_ID = 13;
  private static final int AUTHORITY_PLACE_PROFILE_ID = 14;
  private static final int AUTHORITY_TOPIC_PROFILE_ID = 15;

  private static final Map<Integer, ResourceTypeDictionary> TYPE_BY_PROFILE_ID = Map.of(
    AUTHORITY_FAMILY_PROFILE_ID, FAMILY,
    AUTHORITY_FORM_PROFILE_ID, FORM,
    AUTHORITY_JURISDICTION_PROFILE_ID, JURISDICTION,
    AUTHORITY_MEETING_PROFILE_ID, MEETING,
    AUTHORITY_ORGANIZATION_PROFILE_ID, ORGANIZATION,
    AUTHORITY_PERSON_PROFILE_ID, PERSON,
    AUTHORITY_PLACE_PROFILE_ID, PLACE,
    AUTHORITY_TOPIC_PROFILE_ID, TOPIC
  );

  @Override
  public boolean supports(Resource resource) {
    return TYPE_BY_PROFILE_ID.values()
      .stream()
      .anyMatch(resource::isOfType);
  }

  @Override
  public Integer selectProfile(Resource resource) {
    return TYPE_BY_PROFILE_ID.entrySet()
      .stream()
      .filter(entry -> resource.isOfType(entry.getValue()))
      .findFirst()
      .map(Map.Entry::getKey)
      .orElseThrow(IllegalArgumentException::new);
  }

  @Override
  public boolean supportsProfileId(Integer profileId) {
    return TYPE_BY_PROFILE_ID.containsKey(profileId);
  }

  @Override
  public ResourceTypeDictionary resourceType(Integer profileId) {
    return TYPE_BY_PROFILE_ID.get(profileId);
  }
}
