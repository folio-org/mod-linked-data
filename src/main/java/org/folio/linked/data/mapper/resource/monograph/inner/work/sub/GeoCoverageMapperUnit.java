package org.folio.linked.data.mapper.resource.monograph.inner.work.sub;

import static org.folio.linked.data.util.Bibframe2Constants.GEOGRAPHIC_COVERAGE_PRED;
import static org.folio.linked.data.util.Bibframe2Constants.SAME_AS_PRED;

import lombok.RequiredArgsConstructor;
import org.folio.linked.data.domain.dto.GeographicCoverage2;
import org.folio.linked.data.domain.dto.GeographicCoverageField2;
import org.folio.linked.data.domain.dto.Work2;
import org.folio.linked.data.mapper.resource.common.CoreMapper;
import org.folio.linked.data.mapper.resource.common.MapperUnit;
import org.folio.linked.data.model.entity.Resource;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@MapperUnit(predicate = GEOGRAPHIC_COVERAGE_PRED)
public class GeoCoverageMapperUnit implements WorkSubResourceMapperUnit {

  private final CoreMapper coreMapper;

  @Override
  public Work2 toDto(Resource source, Work2 destination) {
    var geoCoverage = coreMapper.readResourceDoc(source, GeographicCoverage2.class);
    coreMapper.addMappedProperties(source, SAME_AS_PRED, geoCoverage::addSameAsItem);
    destination.addGeographicCoverageItem(new GeographicCoverageField2().geographicCoverage(geoCoverage));
    return destination;
  }
}
