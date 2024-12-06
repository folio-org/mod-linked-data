package org.folio.linked.data.service.resource.marc;

import static org.apache.commons.collections4.CollectionUtils.containsAny;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;
import static org.folio.ld.dictionary.ResourceTypeDictionary.CONCEPT;
import static org.folio.ld.dictionary.ResourceTypeDictionary.FAMILY;
import static org.folio.ld.dictionary.ResourceTypeDictionary.FORM;
import static org.folio.ld.dictionary.ResourceTypeDictionary.JURISDICTION;
import static org.folio.ld.dictionary.ResourceTypeDictionary.MEETING;
import static org.folio.ld.dictionary.ResourceTypeDictionary.ORGANIZATION;
import static org.folio.ld.dictionary.ResourceTypeDictionary.PERSON;
import static org.folio.ld.dictionary.ResourceTypeDictionary.PLACE;
import static org.folio.ld.dictionary.ResourceTypeDictionary.TEMPORAL;
import static org.folio.ld.dictionary.ResourceTypeDictionary.TOPIC;

import java.util.Collection;
import java.util.Set;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.folio.ld.dictionary.ResourceTypeDictionary;

@Getter
@RequiredArgsConstructor
public enum AssignAuthorityTarget {

  CREATOR_OF_WORK(Set.of(PERSON, FAMILY, ORGANIZATION, JURISDICTION, MEETING), Set.of()),
  SUBJECT_OF_WORK(Set.of(FAMILY, ORGANIZATION, JURISDICTION, MEETING, PERSON, FORM, TOPIC, TEMPORAL, PLACE),
    Set.of(CONCEPT));

  private static final int TYPES_MAX_SIZE = 2;

  private final Set<ResourceTypeDictionary> exact;
  private final Set<ResourceTypeDictionary> any;

  public Boolean isCompatibleWith(Collection<ResourceTypeDictionary> types) {
    var isWithinExpectedSize = isWithinExpectedSize(types);
    return isWithinExpectedSize
      && (hasSingleTypeAndMatchesExact(types) || hasCombinedType(types));
  }

  private boolean isWithinExpectedSize(Collection<ResourceTypeDictionary> types) {
    return !isEmpty(types) && types.size() <= TYPES_MAX_SIZE;
  }

  private boolean hasSingleTypeAndMatchesExact(Collection<ResourceTypeDictionary> types) {
    return types.size() == 1 && exact.containsAll(types);
  }

  private boolean hasCombinedType(Collection<ResourceTypeDictionary> types) {
    return containsAny(types, any) && containsAny(types, exact);
  }
}
