package org.folio.linked.data.mapper.resource.monograph.inner.item.sub;

import static org.folio.linked.data.util.BibframeConstants.CLASSIFICATION_PRED;
import static org.folio.linked.data.util.BibframeConstants.NOTE;

import lombok.RequiredArgsConstructor;
import org.folio.linked.data.domain.dto.Item;
import org.folio.linked.data.domain.dto.ItemClassificationLcc;
import org.folio.linked.data.mapper.resource.common.CommonMapper;
import org.folio.linked.data.mapper.resource.common.ResourceMapper;
import org.folio.linked.data.model.entity.Resource;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ResourceMapper(predicate = CLASSIFICATION_PRED)
public class ClassificationMapper implements ItemSubResourceMapper {

  private final CommonMapper commonMapper;

  @Override
  public Item toDto(Resource source, Item destination) {
    var classification = commonMapper.readResourceDoc(source, ItemClassificationLcc.class);
    commonMapper.addMappedProperties(source, NOTE, classification::addNoteItem);
    return destination;
  }
}
