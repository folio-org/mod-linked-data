package org.folio.linked.data.mapper.resource.monograph.inner.item.sub;

import static org.folio.linked.data.util.BibframeConstants.CLASSIFICATION_PRED;
import static org.folio.linked.data.util.BibframeConstants.NOTE;

import lombok.RequiredArgsConstructor;
import org.folio.linked.data.domain.dto.Item;
import org.folio.linked.data.domain.dto.ItemClassificationLcc;
import org.folio.linked.data.mapper.resource.common.CoreMapper;
import org.folio.linked.data.mapper.resource.common.MapperUnit;
import org.folio.linked.data.model.entity.Resource;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@MapperUnit(predicate = CLASSIFICATION_PRED)
public class ClassificationMapperUnit implements ItemSubResourceMapperUnit {

  private final CoreMapper coreMapper;

  @Override
  public Item toDto(Resource source, Item destination) {
    var classification = coreMapper.readResourceDoc(source, ItemClassificationLcc.class);
    coreMapper.addMappedProperties(source, NOTE, classification::addNoteItem);
    return destination;
  }
}
