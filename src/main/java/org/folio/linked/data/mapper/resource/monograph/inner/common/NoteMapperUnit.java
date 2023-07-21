package org.folio.linked.data.mapper.resource.monograph.inner.common;

import static org.folio.linked.data.util.BibframeConstants.LABEL_PRED;
import static org.folio.linked.data.util.BibframeConstants.NOTE;
import static org.folio.linked.data.util.BibframeConstants.NOTE_PRED;
import static org.folio.linked.data.util.BibframeConstants.NOTE_TYPE_PRED;
import static org.folio.linked.data.util.BibframeConstants.NOTE_TYPE_URI;
import static org.folio.linked.data.util.BibframeConstants.NOTE_URL;
import static org.folio.linked.data.util.Constants.IS_NOT_SUPPORTED_FOR_PREDICATE;
import static org.folio.linked.data.util.Constants.RESOURCE_TYPE;
import static org.folio.linked.data.util.Constants.RIGHT_SQUARE_BRACKET;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.folio.linked.data.domain.dto.Extent;
import org.folio.linked.data.domain.dto.Instance;
import org.folio.linked.data.domain.dto.Note;
import org.folio.linked.data.domain.dto.NoteField;
import org.folio.linked.data.domain.dto.ParallelTitle;
import org.folio.linked.data.domain.dto.Url;
import org.folio.linked.data.domain.dto.VariantTitle;
import org.folio.linked.data.exception.NotSupportedException;
import org.folio.linked.data.mapper.resource.common.CoreMapper;
import org.folio.linked.data.mapper.resource.common.MapperUnit;
import org.folio.linked.data.mapper.resource.common.inner.sub.SubResourceMapperUnit;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceType;
import org.folio.linked.data.service.dictionary.DictionaryService;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@MapperUnit(type = NOTE, predicate = NOTE_PRED, dtoClass = NoteField.class)
public class NoteMapperUnit<T> implements SubResourceMapperUnit<T> {

  private static final Set<Class> SUPPORTED_PARENTS =
    Set.of(Instance.class, Extent.class, ParallelTitle.class, VariantTitle.class, Url.class);
  private final CoreMapper coreMapper;
  private final DictionaryService<ResourceType> resourceTypeService;

  @Override
  public T toDto(Resource source, T destination) {
    var note = coreMapper.readResourceDoc(source, Note.class);
    coreMapper.addMappedProperties(source, NOTE_TYPE_PRED, note::addNoteTypeItem);
    var noteField = new NoteField().note(note);
    if (destination instanceof Instance instance) {
      instance.addNoteItem(noteField);
    } else if (destination instanceof Extent extent) {
      extent.addNoteItem(noteField);
    } else if (destination instanceof ParallelTitle parallelTitle) {
      parallelTitle.addNoteItem(noteField);
    } else if (destination instanceof VariantTitle variantTitle) {
      variantTitle.addNoteItem(noteField);
    } else if (destination instanceof Url url) {
      url.addNoteItem(noteField);
    } else {
      throw new NotSupportedException(RESOURCE_TYPE + destination.getClass().getSimpleName()
        + IS_NOT_SUPPORTED_FOR_PREDICATE + NOTE_PRED + RIGHT_SQUARE_BRACKET);
    }
    return destination;
  }

  @Override
  public Set<Class> getParentDto() {
    return SUPPORTED_PARENTS;
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
