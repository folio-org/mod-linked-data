package org.folio.linked.data.mapper.dto.resource.common.work.sub.reference;

import static org.folio.ld.dictionary.PredicateDictionary.GEOGRAPHIC_COVERAGE;
import static org.folio.ld.dictionary.ResourceTypeDictionary.PLACE;

import java.util.Set;
import org.folio.linked.data.domain.dto.Reference;
import org.folio.linked.data.domain.dto.WorkRequest;
import org.folio.linked.data.domain.dto.WorkResponse;
import org.folio.linked.data.mapper.dto.resource.base.MapperUnit;
import org.folio.linked.data.mapper.dto.resource.common.ReferenceMapperUnit;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.service.reference.ReferenceService;
import org.springframework.stereotype.Component;

@Component
@MapperUnit(type = PLACE, predicate = GEOGRAPHIC_COVERAGE, requestDto = Reference.class)
public class GeographicCoverageMapperUnit extends ReferenceMapperUnit {

  public GeographicCoverageMapperUnit(ReferenceService referenceService) {
    super(referenceService);
  }

  @Override
  public <P> P toDto(Resource resourceToConvert, P parentDto, ResourceMappingContext context) {
    if (parentDto instanceof WorkResponse workDto) {
      var reference = toReference(resourceToConvert);
      workDto.addGeographicCoverageReferenceItem(reference);
    }
    return parentDto;
  }

  @Override
  public Set<Class<?>> supportedParents() {
    return Set.of(WorkRequest.class, WorkResponse.class);
  }
}
