package org.folio.linked.data.mapper.dto.monograph.work.sub;

import static org.folio.ld.dictionary.PredicateDictionary.GEOGRAPHIC_COVERAGE;
import static org.folio.ld.dictionary.ResourceTypeDictionary.PLACE;

import org.folio.linked.data.domain.dto.Reference;
import org.folio.linked.data.domain.dto.Work;
import org.folio.linked.data.domain.dto.WorkReference;
import org.folio.linked.data.mapper.dto.common.MapperUnit;
import org.folio.linked.data.repo.ResourceRepository;
import org.springframework.stereotype.Component;

@Component
@MapperUnit(type = PLACE, predicate = GEOGRAPHIC_COVERAGE, dtoClass = Reference.class)
public class GeographicCoverageMapperUnit extends ReferenceMapperUnit {

  public GeographicCoverageMapperUnit(ResourceRepository resourceRepository) {
    super((geographicCoverage, destination) -> {
      if (destination instanceof Work work) {
        work.addGeographicCoverageReferenceItem(geographicCoverage);
      } else if (destination instanceof WorkReference workReference) {
        workReference.addGeographicCoverageReferenceItem(geographicCoverage);
      }
    }, resourceRepository);
  }
}
