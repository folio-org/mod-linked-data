package org.folio.linked.data.mapper.resource.monograph.inner.instance.sub.title;

import static org.folio.linked.data.util.BibframeConstants.DATE_URL;
import static org.folio.linked.data.util.BibframeConstants.INSTANCE_TITLE_PRED;
import static org.folio.linked.data.util.BibframeConstants.MAIN_TITLE_URL;
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
import org.folio.linked.data.domain.dto.Instance;
import org.folio.linked.data.domain.dto.ParallelTitle;
import org.folio.linked.data.domain.dto.ParallelTitleField;
import org.folio.linked.data.mapper.resource.common.CommonMapper;
import org.folio.linked.data.mapper.resource.common.ResourceMapper;
import org.folio.linked.data.mapper.resource.monograph.inner.instance.sub.InstanceNoteMapper;
import org.folio.linked.data.mapper.resource.monograph.inner.instance.sub.InstanceSubResourceMapper;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceType;
import org.folio.linked.data.service.dictionary.DictionaryService;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ResourceMapper(type = PARALLEL_TITLE, predicate = INSTANCE_TITLE_PRED, dtoClass = ParallelTitleField.class)
public class InstanceParallelTitleMapper implements InstanceSubResourceMapper {

  private final DictionaryService<ResourceType> resourceTypeService;
  private final CommonMapper commonMapper;
  private final InstanceNoteMapper noteMapper;

  @Override
  public Instance toDto(Resource source, Instance destination) {
    var parallelTitle = commonMapper.readResourceDoc(source, ParallelTitle.class);
    commonMapper.addMappedProperties(source, NOTE_PRED, parallelTitle::addNoteItem);
    destination.addTitleItem(new ParallelTitleField().parallelTitle(parallelTitle));
    return destination;
  }

  @Override
  public Resource toEntity(Object dto, String predicate) {
    var parallelTitle = ((ParallelTitleField) dto).getParallelTitle();
    var resource = new Resource();
    resource.setLabel(PARALLEL_TITLE_URL);
    resource.setType(resourceTypeService.get(PARALLEL_TITLE));
    resource.setDoc(getDoc(parallelTitle));
    commonMapper.mapResourceEdges(parallelTitle.getNote(), resource, NOTE_PRED, noteMapper::toEntity);
    resource.setResourceHash(commonMapper.hash(resource));
    return resource;
  }

  private JsonNode getDoc(ParallelTitle dto) {
    var map = new HashMap<String, List<String>>();
    map.put(PART_NAME_URL, dto.getPartName());
    map.put(PART_NUMBER_URL, dto.getPartNumber());
    map.put(MAIN_TITLE_URL, dto.getMainTitle());
    map.put(DATE_URL, dto.getDate());
    map.put(SUBTITLE_URL, dto.getSubtitle());
    return commonMapper.toJson(map);
  }

}
