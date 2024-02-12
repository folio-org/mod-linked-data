package org.folio.linked.data.mapper.resource.monograph.work.sub;

import static org.folio.ld.dictionary.PredicateDictionary.GEOGRAPHIC_COVERAGE;
import static org.folio.ld.dictionary.ResourceTypeDictionary.PLACE;
import static org.folio.linked.data.util.Constants.IS_NOT_FOUND;
import static org.folio.linked.data.util.Constants.RESOURCE_WITH_GIVEN_ID;

import lombok.RequiredArgsConstructor;
import org.folio.linked.data.domain.dto.GeographicCoverage;
import org.folio.linked.data.domain.dto.Work;
import org.folio.linked.data.exception.NotFoundException;
import org.folio.linked.data.mapper.resource.common.MapperUnit;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.repo.ResourceRepository;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@MapperUnit(type = PLACE, predicate = GEOGRAPHIC_COVERAGE, dtoClass = GeographicCoverage.class)
public class GeographicCoverageMapperUnit implements WorkSubResourceMapperUnit {

  private final ResourceRepository resourceRepository;

  @Override
  public <P> P toDto(Resource source, P parentDto, Resource parentResource) {
    var geographicCoverage = new GeographicCoverage()
      .id(source.getResourceHash().toString())
      .label(source.getLabel());
    if (parentDto instanceof Work work) {
      work.addGeographicCoverageReferenceItem(geographicCoverage);
    }
    return parentDto;
  }

  @Override
  public Resource toEntity(Object dto, Resource parentEntity) {
    var geographicCoverage = (GeographicCoverage) dto;
    return resourceRepository
      .findById(Long.parseLong(geographicCoverage.getId()))
      .orElseThrow(() -> new NotFoundException(RESOURCE_WITH_GIVEN_ID + geographicCoverage.getId() + IS_NOT_FOUND));
  }
}
