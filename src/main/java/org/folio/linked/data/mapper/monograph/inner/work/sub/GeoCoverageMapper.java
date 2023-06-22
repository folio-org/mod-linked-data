package org.folio.linked.data.mapper.monograph.inner.work.sub;

import static org.folio.linked.data.util.BibframeConstants.GEOGRAPHIC_COVERAGE_PRED;
import static org.folio.linked.data.util.BibframeConstants.SAME_AS_PRED;
import static org.folio.linked.data.util.MappingUtil.addMappedProperties;
import static org.folio.linked.data.util.MappingUtil.readResourceDoc;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.folio.linked.data.domain.dto.GeographicCoverage;
import org.folio.linked.data.domain.dto.GeographicCoverageField;
import org.folio.linked.data.domain.dto.Work;
import org.folio.linked.data.mapper.resource.ResourceMapper;
import org.folio.linked.data.model.entity.Resource;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ResourceMapper(GEOGRAPHIC_COVERAGE_PRED)
public class GeoCoverageMapper implements WorkSubResourceMapper {

  private final ObjectMapper objectMapper;

  @Override
  public Work toDto(Resource source, Work destination) {
    var geoCoverage = readResourceDoc(objectMapper, source, GeographicCoverage.class);
    addMappedProperties(objectMapper, source, SAME_AS_PRED, geoCoverage::addSameAsItem);
    destination.addGeographicCoverageItem(new GeographicCoverageField().geographicCoverage(geoCoverage));
    return destination;
  }
}
