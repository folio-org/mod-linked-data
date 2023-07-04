package org.folio.linked.data.mapper.resource.monograph.inner.work.sub;

import static org.folio.linked.data.util.BibframeConstants.ORIGIN_PLACE_PRED;

import lombok.RequiredArgsConstructor;
import org.folio.linked.data.domain.dto.Work;
import org.folio.linked.data.mapper.resource.common.CommonMapper;
import org.folio.linked.data.mapper.resource.common.ResourceMapper;
import org.folio.linked.data.model.entity.Resource;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ResourceMapper(predicate = ORIGIN_PLACE_PRED)
public class OriginPlaceMapper implements WorkSubResourceMapper {

  private final CommonMapper commonMapper;

  @Override
  public Work toDto(Resource source, Work destination) {
    var property = commonMapper.toProperty(source);
    destination.addOriginPlaceItem(property);
    return destination;
  }
}