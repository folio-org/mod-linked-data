package org.folio.linked.data.mapper.resource.monograph.inner.work.sub;

import static org.folio.linked.data.util.Bibframe2Constants.OTHER_EDITION_PRED;

import lombok.RequiredArgsConstructor;
import org.folio.linked.data.domain.dto.Work2;
import org.folio.linked.data.mapper.resource.common.CoreMapper;
import org.folio.linked.data.mapper.resource.common.MapperUnit;
import org.folio.linked.data.model.entity.Resource;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@MapperUnit(predicate = OTHER_EDITION_PRED)
public class OtherEditionMapperUnit implements WorkSubResourceMapperUnit {

  private final CoreMapper coreMapper;

  @Override
  public Work2 toDto(Resource source, Work2 destination) {
    var property = coreMapper.toProperty(source);
    destination.addOtherEditionItem(property);
    return destination;
  }
}
