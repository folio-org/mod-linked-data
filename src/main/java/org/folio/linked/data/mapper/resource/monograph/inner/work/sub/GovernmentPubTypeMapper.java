package org.folio.linked.data.mapper.resource.monograph.inner.work.sub;

import static org.folio.linked.data.util.BibframeConstants.GOVERNMENT_PUB_TYPE_PRED;
import static org.folio.linked.data.util.MappingUtil.toProperty;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.folio.linked.data.domain.dto.Work;
import org.folio.linked.data.mapper.resource.common.ResourceMapper;
import org.folio.linked.data.model.entity.Resource;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ResourceMapper(predicate = GOVERNMENT_PUB_TYPE_PRED)
public class GovernmentPubTypeMapper implements WorkSubResourceMapper {

  private final ObjectMapper mapper;

  @Override
  public Work toDto(Resource source, Work destination) {
    var property = toProperty(mapper, source);
    destination.addGovernmentPubTypeItem(property);
    return destination;
  }
}
