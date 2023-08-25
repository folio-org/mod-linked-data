package org.folio.linked.data.mapper.resource.monograph.inner.instance.sub.title;

import static org.folio.linked.data.util.BibframeConstants.DATE_URL;
import static org.folio.linked.data.util.BibframeConstants.INSTANCE_TITLE_PRED;
import static org.folio.linked.data.util.BibframeConstants.MAIN_TITLE_URL;
import static org.folio.linked.data.util.BibframeConstants.NOTE;
import static org.folio.linked.data.util.BibframeConstants.NOTE_PRED;
import static org.folio.linked.data.util.BibframeConstants.PARALLEL_TITLE;
import static org.folio.linked.data.util.BibframeConstants.PARALLEL_TITLE_URL;
import static org.folio.linked.data.util.BibframeConstants.PART_NAME_URL;
import static org.folio.linked.data.util.BibframeConstants.PART_NUMBER_URL;
import static org.folio.linked.data.util.BibframeConstants.SUBTITLE_URL;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.HashMap;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.folio.linked.data.domain.dto.Instance2;
import org.folio.linked.data.domain.dto.ParallelTitle2;
import org.folio.linked.data.domain.dto.ParallelTitleField2;
import org.folio.linked.data.mapper.resource.common.CoreMapper;
import org.folio.linked.data.mapper.resource.common.MapperUnit;
import org.folio.linked.data.mapper.resource.common.inner.sub.SubResourceMapper;
import org.folio.linked.data.mapper.resource.monograph.inner.common.NoteMapperUnit;
import org.folio.linked.data.mapper.resource.monograph.inner.instance.sub.Instance2SubResourceMapperUnit;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceType;
import org.folio.linked.data.service.dictionary.DictionaryService;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@MapperUnit(type = PARALLEL_TITLE, predicate = INSTANCE_TITLE_PRED, dtoClass = ParallelTitleField2.class)
public class InstanceParallelTitle2MapperUnit implements Instance2SubResourceMapperUnit {

  private final DictionaryService<ResourceType> resourceTypeService;
  private final CoreMapper coreMapper;
  private final NoteMapperUnit<ParallelTitle2> noteMapper;

  @Override
  public Instance2 toDto(Resource source, Instance2 destination) {
    var parallelTitle = coreMapper.readResourceDoc(source, ParallelTitle2.class);
    coreMapper.addMappedResources(noteMapper, source, NOTE_PRED, parallelTitle);
    destination.addTitleItem(new ParallelTitleField2().parallelTitle(parallelTitle));
    return destination;
  }

  @Override
  public Resource toEntity(Object dto, String predicate, SubResourceMapper subResourceMapper) {
    var parallelTitle = ((ParallelTitleField2) dto).getParallelTitle();
    var resource = new Resource();
    resource.setLabel(PARALLEL_TITLE_URL);
    resource.setType(resourceTypeService.get(PARALLEL_TITLE));
    resource.setDoc(getDoc(parallelTitle));
    coreMapper.mapResourceEdges(parallelTitle.getNote(), resource, null, NOTE_PRED,
      (fieldDto, pred) -> noteMapper.toEntity(fieldDto, pred, null));
    coreMapper.mapResourceEdges(parallelTitle.getNote(), resource, NOTE, NOTE_PRED,
      (fieldDto, pred) -> noteMapper.toEntity(fieldDto, pred, null));
    resource.setResourceHash(coreMapper.hash(resource));
    return resource;
  }

  private JsonNode getDoc(ParallelTitle2 dto) {
    var map = new HashMap<String, List<String>>();
    map.put(PART_NAME_URL, dto.getPartName());
    map.put(PART_NUMBER_URL, dto.getPartNumber());
    map.put(MAIN_TITLE_URL, dto.getMainTitle());
    map.put(DATE_URL, dto.getDate());
    map.put(SUBTITLE_URL, dto.getSubtitle());
    return coreMapper.toJson(map);
  }

}
