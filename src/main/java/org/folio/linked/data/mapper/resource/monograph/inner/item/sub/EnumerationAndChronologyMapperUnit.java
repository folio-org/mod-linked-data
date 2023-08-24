package org.folio.linked.data.mapper.resource.monograph.inner.item.sub;

import static org.folio.linked.data.util.BibframeConstants.ENUMERATION_AND_CHRONOLOGY_PRED;

import lombok.RequiredArgsConstructor;
import org.folio.linked.data.domain.dto.Item2;
import org.folio.linked.data.mapper.resource.common.CoreMapper;
import org.folio.linked.data.mapper.resource.common.MapperUnit;
import org.folio.linked.data.model.entity.Resource;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@MapperUnit(predicate = ENUMERATION_AND_CHRONOLOGY_PRED)
public class EnumerationAndChronologyMapperUnit implements ItemSubResourceMapperUnit {

  private final CoreMapper coreMapper;

  @Override
  public Item2 toDto(Resource source, Item2 destination) {
    var item = coreMapper.toProperty(source);
    destination.addEnumerationAndChronologyItem(item);
    return destination;
  }
}
