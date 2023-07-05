package org.folio.linked.data.mapper.resource.monograph.inner.instance.sub;

import static org.folio.linked.data.util.BibframeConstants.NOTE;
import static org.folio.linked.data.util.BibframeConstants.NOTE_PRED;

import lombok.RequiredArgsConstructor;
import org.folio.linked.data.domain.dto.Instance;
import org.folio.linked.data.domain.dto.Property;
import org.folio.linked.data.mapper.resource.common.CoreMapper;
import org.folio.linked.data.mapper.resource.common.MapperUnit;
import org.folio.linked.data.model.entity.Resource;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@MapperUnit(type = NOTE, predicate = NOTE_PRED)
public class InstanceNoteMapperUnit implements InstanceSubResourceMapperUnit {

  private final CoreMapper coreMapper;

  @Override
  public Instance toDto(Resource source, Instance destination) {
    var note = coreMapper.toProperty(source);
    destination.addNoteItem(note);
    return destination;
  }

  @Override
  public Resource toEntity(Object dto, String predicate) {
    return coreMapper.propertyToEntity((Property) dto, NOTE);
  }

}
