package org.folio.linked.data.mapper.resource.monograph.inner.instance.sub;

import static org.folio.linked.data.util.BibframeConstants.LABEL_PRED;
import static org.folio.linked.data.util.BibframeConstants.NOTE;
import static org.folio.linked.data.util.BibframeConstants.NOTE_PRED;
import static org.folio.linked.data.util.BibframeConstants.NOTE_TYPE_PRED;
import static org.folio.linked.data.util.BibframeConstants.NOTE_TYPE_URI;
import static org.folio.linked.data.util.BibframeConstants.NOTE_URL;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.HashMap;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.folio.linked.data.domain.dto.Instance;
import org.folio.linked.data.domain.dto.Note;
import org.folio.linked.data.domain.dto.NoteField;
import org.folio.linked.data.mapper.resource.common.CoreMapper;
import org.folio.linked.data.mapper.resource.common.MapperUnit;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceType;
import org.folio.linked.data.service.dictionary.DictionaryService;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@MapperUnit(type = NOTE, predicate = NOTE_PRED, dtoClass = NoteField.class)
public class InstanceNoteMapperUnit implements InstanceSubResourceMapperUnit {

  private final CoreMapper coreMapper;
  private final DictionaryService<ResourceType> resourceTypeService;

  @Override
  public Instance toDto(Resource source, Instance destination) {
    var note = coreMapper.readResourceDoc(source, Note.class);
    coreMapper.addMappedProperties(source, NOTE_TYPE_PRED, note::addNoteTypeItem);
    destination.addNoteItem(new NoteField().note(note));
    return destination;
  }

  @Override
  public Resource toEntity(Object dto, String predicate) {
    var note = ((NoteField) dto).getNote();
    var resource = new Resource();
    resource.setLabel(NOTE_URL);
    resource.setType(resourceTypeService.get(NOTE));
    resource.setDoc(getDoc(note));
    coreMapper.mapPropertyEdges(note.getNoteType(), resource, NOTE_TYPE_PRED, NOTE_TYPE_URI);
    resource.setResourceHash(coreMapper.hash(resource));
    return resource;
  }

  private JsonNode getDoc(Note note) {
    var map = new HashMap<String, List<String>>();
    map.put(LABEL_PRED, note.getLabel());
    return coreMapper.toJson(map);
  }

}
