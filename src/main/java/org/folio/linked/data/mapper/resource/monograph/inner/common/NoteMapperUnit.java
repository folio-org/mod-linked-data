package org.folio.linked.data.mapper.resource.monograph.inner.common;

import static org.folio.linked.data.util.BibframeConstants.LABEL_PRED;
import static org.folio.linked.data.util.BibframeConstants.NOTE_2;
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
import org.folio.linked.data.domain.dto.Extent2;
import org.folio.linked.data.domain.dto.Instance2;
import org.folio.linked.data.domain.dto.Note2;
import org.folio.linked.data.domain.dto.NoteField2;
import org.folio.linked.data.domain.dto.ParallelTitle2;
import org.folio.linked.data.domain.dto.Url2;
import org.folio.linked.data.domain.dto.VariantTitle2;
import org.folio.linked.data.exception.NotSupportedException;
import org.folio.linked.data.mapper.resource.common.CoreMapper;
import org.folio.linked.data.mapper.resource.common.MapperUnit;
import org.folio.linked.data.mapper.resource.common.inner.sub.SubResourceMapper;
import org.folio.linked.data.mapper.resource.common.inner.sub.SubResourceMapperUnit;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceType;
import org.folio.linked.data.service.dictionary.DictionaryService;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@MapperUnit(type = NOTE_URL, predicate = NOTE_PRED, dtoClass = NoteField2.class)
public class NoteMapperUnit<T> implements SubResourceMapperUnit<T> {

  private static final Set<Class> SUPPORTED_PARENTS =
    Set.of(Instance2.class, Extent2.class, ParallelTitle2.class, VariantTitle2.class, Url2.class);
  private final CoreMapper coreMapper;
  private final DictionaryService<ResourceType> resourceTypeService;

  @Override
  public T toDto(Resource source, T destination) {
    var note = coreMapper.readResourceDoc(source, Note2.class);
    coreMapper.addMappedProperties(source, NOTE_TYPE_PRED, note::addNoteTypeItem);
    var noteField = new NoteField2().note(note);
    if (destination instanceof Instance2 instance) {
      instance.addNoteItem(noteField);
    } else if (destination instanceof Extent2 extent) {
      extent.addNoteItem(noteField);
    } else if (destination instanceof ParallelTitle2 parallelTitle) {
      parallelTitle.addNoteItem(noteField);
    } else if (destination instanceof VariantTitle2 variantTitle) {
      variantTitle.addNoteItem(noteField);
    } else if (destination instanceof Url2 url) {
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
  public Resource toEntity(Object dto, String predicate, SubResourceMapper subResourceMapper) {
    var note = ((NoteField2) dto).getNote();
    var resource = new Resource();
    resource.setLabel(NOTE_URL);
    resource.setType(resourceTypeService.get(NOTE_2));
    resource.setDoc(getDoc(note));
    coreMapper.mapPropertyEdges(note.getNoteType(), resource, NOTE_TYPE_PRED, NOTE_TYPE_URI);
    resource.setResourceHash(coreMapper.hash(resource));
    return resource;
  }

  private JsonNode getDoc(Note2 note) {
    var map = new HashMap<String, List<String>>();
    map.put(LABEL_PRED, note.getLabel());
    return coreMapper.toJson(map);
  }

}
