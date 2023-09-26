package org.folio.linked.data.mapper.resource.monograph.inner.instance.sub.title;

import static org.folio.linked.data.util.BibframeConstants.DATE;
import static org.folio.linked.data.util.BibframeConstants.INSTANCE_TITLE_PRED;
import static org.folio.linked.data.util.BibframeConstants.MAIN_TITLE;
import static org.folio.linked.data.util.BibframeConstants.NOTE;
import static org.folio.linked.data.util.BibframeConstants.PARALLEL_TITLE;
import static org.folio.linked.data.util.BibframeConstants.PART_NAME;
import static org.folio.linked.data.util.BibframeConstants.PART_NUMBER;
import static org.folio.linked.data.util.BibframeConstants.SUBTITLE;
import static org.folio.linked.data.util.BibframeUtils.getLabelOrFirstValue;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.HashMap;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.folio.linked.data.domain.dto.Instance;
import org.folio.linked.data.domain.dto.ParallelTitle;
import org.folio.linked.data.domain.dto.ParallelTitleField;
import org.folio.linked.data.mapper.resource.common.CoreMapper;
import org.folio.linked.data.mapper.resource.common.MapperUnit;
import org.folio.linked.data.mapper.resource.common.inner.sub.SubResourceMapper;
import org.folio.linked.data.mapper.resource.monograph.inner.instance.sub.InstanceSubResourceMapperUnit;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceType;
import org.folio.linked.data.service.dictionary.DictionaryService;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@MapperUnit(type = PARALLEL_TITLE, predicate = INSTANCE_TITLE_PRED, dtoClass = ParallelTitleField.class)
public class InstanceParallelTitleMapperUnit implements InstanceSubResourceMapperUnit {

  private final DictionaryService<ResourceType> resourceTypeService;
  private final CoreMapper coreMapper;

  @Override
  public Instance toDto(Resource source, Instance destination) {
    var parallelTitle = coreMapper.readResourceDoc(source, ParallelTitle.class);
    parallelTitle.setId(String.valueOf(source.getResourceHash()));
    parallelTitle.setLabel(source.getLabel());
    destination.addTitleItem(new ParallelTitleField().parallelTitle(parallelTitle));
    return destination;
  }

  @Override
  public Resource toEntity(Object dto, String predicate, SubResourceMapper subResourceMapper) {
    var parallelTitle = ((ParallelTitleField) dto).getParallelTitle();
    var resource = new Resource();
    resource.setLabel(getLabelOrFirstValue(parallelTitle.getLabel(), parallelTitle::getMainTitle));
    resource.addType(resourceTypeService.get(PARALLEL_TITLE));
    resource.setDoc(getDoc(parallelTitle));
    resource.setResourceHash(coreMapper.hash(resource));
    return resource;
  }

  private JsonNode getDoc(ParallelTitle dto) {
    var map = new HashMap<String, List<String>>();
    map.put(PART_NAME, dto.getPartName());
    map.put(PART_NUMBER, dto.getPartNumber());
    map.put(MAIN_TITLE, dto.getMainTitle());
    map.put(DATE, dto.getDate());
    map.put(SUBTITLE, dto.getSubtitle());
    map.put(NOTE, dto.getNote());
    return coreMapper.toJson(map);
  }

}
