package org.folio.linked.data.mapper.dto.resource.common.work.sub.reference;

import static org.folio.ld.dictionary.PredicateDictionary.ASSIGNING_SOURCE;
import static org.folio.ld.dictionary.ResourceTypeDictionary.ORGANIZATION;

import java.util.Set;
import org.folio.linked.data.domain.dto.Classification;
import org.folio.linked.data.domain.dto.ClassificationResponse;
import org.folio.linked.data.domain.dto.Reference;
import org.folio.linked.data.mapper.dto.resource.base.MapperUnit;
import org.folio.linked.data.service.reference.ReferenceService;
import org.springframework.stereotype.Component;

@Component
@MapperUnit(type = ORGANIZATION, predicate = ASSIGNING_SOURCE, requestDto = Reference.class)
public class AssigningSourceMapperUnit extends ReferenceMapperUnit {

  private static final Set<Class<?>> SUPPORTED_PARENTS = Set.of(
    Classification.class,
    ClassificationResponse.class
  );

  public AssigningSourceMapperUnit(ReferenceService referenceService) {
    super((assigningSource, destination) -> {
      if (destination instanceof ClassificationResponse classification) {
        classification.addAssigningSourceReferenceItem(assigningSource);
      }
    }, referenceService);
  }

  @Override
  public Set<Class<?>> supportedParents() {
    return SUPPORTED_PARENTS;
  }
}
