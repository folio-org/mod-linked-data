package org.folio.linked.data.mapper.dto.monograph.work.sub;

import static org.folio.ld.dictionary.PredicateDictionary.ASSIGNING_SOURCE;
import static org.folio.ld.dictionary.ResourceTypeDictionary.ORGANIZATION;

import java.util.Set;
import org.folio.linked.data.domain.dto.Classification;
import org.folio.linked.data.domain.dto.Reference;
import org.folio.linked.data.mapper.dto.common.MapperUnit;
import org.folio.linked.data.repo.ResourceRepository;
import org.springframework.stereotype.Component;

@Component
@MapperUnit(type = ORGANIZATION, predicate = ASSIGNING_SOURCE, dtoClass = Reference.class)
public class AssigningSourceMapperUnit extends ReferenceMapperUnit {

  private static final Set<Class<?>> SUPPORTED_PARENTS = Set.of(Classification.class);

  public AssigningSourceMapperUnit(ResourceRepository resourceRepository) {
    super((assigningSource, destination) -> {
      if (destination instanceof Classification deweyDecimalClassification) {
        deweyDecimalClassification.addAssigningSourceReferenceItem(assigningSource);
      }
    }, resourceRepository);
  }

  @Override
  public Set<Class<?>> supportedParents() {
    return SUPPORTED_PARENTS;
  }
}
