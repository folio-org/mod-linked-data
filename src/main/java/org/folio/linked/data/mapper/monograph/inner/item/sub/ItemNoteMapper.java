package org.folio.linked.data.mapper.monograph.inner.item.sub;

import static org.folio.linked.data.util.BibframeConstants.NOTE;
import static org.folio.linked.data.util.MappingUtil.toProperty;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.folio.linked.data.domain.dto.Item;
import org.folio.linked.data.mapper.resource.ResourceMapper;
import org.folio.linked.data.model.entity.Resource;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ResourceMapper(NOTE)
public class ItemNoteMapper implements ItemSubResourceMapper {

  private final ObjectMapper objectMapper;

  @Override
  public Item toDto(Resource source, Item destination) {
    var note = toProperty(objectMapper, source);
    destination.addNoteItem(note);
    return destination;
  }
}
