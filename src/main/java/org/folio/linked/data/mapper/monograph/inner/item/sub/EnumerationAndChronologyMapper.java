package org.folio.linked.data.mapper.monograph.inner.item.sub;

import static org.folio.linked.data.util.BibframeConstants.ENUMERATION_AND_CHRONOLOGY_PRED;
import static org.folio.linked.data.util.MappingUtil.toProperty;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.folio.linked.data.domain.dto.Item;
import org.folio.linked.data.mapper.resource.ResourceMapper;
import org.folio.linked.data.model.entity.Resource;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ResourceMapper(ENUMERATION_AND_CHRONOLOGY_PRED)
public class EnumerationAndChronologyMapper implements ItemSubResourceMapper {

  private final ObjectMapper mapper;

  @Override
  public Item toDto(Resource source, Item destination) {
    var item = toProperty(mapper, source);
    destination.addEnumerationAndChronologyItem(item);
    return destination;
  }
}
