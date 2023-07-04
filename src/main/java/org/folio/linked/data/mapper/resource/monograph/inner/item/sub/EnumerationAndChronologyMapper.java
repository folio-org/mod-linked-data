package org.folio.linked.data.mapper.resource.monograph.inner.item.sub;

import static org.folio.linked.data.util.BibframeConstants.ENUMERATION_AND_CHRONOLOGY_PRED;

import lombok.RequiredArgsConstructor;
import org.folio.linked.data.domain.dto.Item;
import org.folio.linked.data.mapper.resource.common.CommonMapper;
import org.folio.linked.data.mapper.resource.common.ResourceMapper;
import org.folio.linked.data.model.entity.Resource;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ResourceMapper(predicate = ENUMERATION_AND_CHRONOLOGY_PRED)
public class EnumerationAndChronologyMapper implements ItemSubResourceMapper {

  private final CommonMapper commonMapper;

  @Override
  public Item toDto(Resource source, Item destination) {
    var item = commonMapper.toProperty(source);
    destination.addEnumerationAndChronologyItem(item);
    return destination;
  }
}