package org.folio.linked.data.mapper.dto.monograph.work.sub;

import static org.folio.ld.dictionary.PredicateDictionary.GEOGRAPHIC_COVERAGE;
import static org.folio.ld.dictionary.ResourceTypeDictionary.PLACE;

import org.folio.linked.data.domain.dto.Reference;
import org.folio.linked.data.domain.dto.WorkResponse;
import org.folio.linked.data.mapper.dto.common.MapperUnit;
import org.folio.linked.data.service.resource.ResourceMarcAuthorityService;
import org.springframework.stereotype.Component;

@Component
@MapperUnit(type = PLACE, predicate = GEOGRAPHIC_COVERAGE, requestDto = Reference.class)
public class GeographicCoverageMapperUnit extends ReferenceMapperUnit {

  public GeographicCoverageMapperUnit(ResourceMarcAuthorityService resourceMarcAuthorityService) {
    super((geographicCoverage, destination) -> {
      if (destination instanceof WorkResponse work) {
        work.addGeographicCoverageReferenceItem(geographicCoverage);
      }
    }, resourceMarcAuthorityService);
  }

}
