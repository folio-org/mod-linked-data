package org.folio.linked.data.mapper.resource.monograph.inner.item.sub;

import static org.folio.linked.data.util.BibframeConstants.CLASSIFICATION_PRED;
import static org.folio.linked.data.util.BibframeConstants.NOTE;
import static org.folio.linked.data.util.MappingUtil.addMappedProperties;
import static org.folio.linked.data.util.MappingUtil.readResourceDoc;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.folio.linked.data.domain.dto.Item;
import org.folio.linked.data.domain.dto.ItemClassificationLcc;
import org.folio.linked.data.mapper.resource.common.ResourceMapper;
import org.folio.linked.data.model.entity.Resource;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ResourceMapper(predicate = CLASSIFICATION_PRED)
public class ClassificationMapper implements ItemSubResourceMapper {

  private final ObjectMapper objectMapper;

  @Override
  public Item toDto(Resource source, Item destination) {
    var classification = readResourceDoc(objectMapper, source, ItemClassificationLcc.class);
    addMappedProperties(objectMapper, source, NOTE, classification::addNoteItem);
    return destination;
  }
}
