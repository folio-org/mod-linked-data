package org.folio.linked.data.mapper.monograph.inner.work.sub;

import static org.folio.linked.data.util.BibframeConstants.OTHER_PHYSICAL_FORMAT_PRED;
import static org.folio.linked.data.util.MappingUtil.toProperty;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.folio.linked.data.domain.dto.Work;
import org.folio.linked.data.mapper.resource.ResourceMapper;
import org.folio.linked.data.model.entity.Resource;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ResourceMapper(OTHER_PHYSICAL_FORMAT_PRED)
public class OtherPhysicalFormatMapper implements WorkSubResourceMapper {

  private final ObjectMapper mapper;

  @Override
  public Work toDto(Resource source, Work destination) {
    var property = toProperty(mapper, source);
    destination.addOtherPhysicalFormatItem(property);
    return destination;
  }
}
