package org.folio.linked.data.mapper.resource.monograph.inner.instance.sub.title;

import static org.folio.linked.data.util.BibframeConstants.INSTANCE_TITLE;
import static org.folio.linked.data.util.BibframeConstants.INSTANCE_TITLE_PRED;
import static org.folio.linked.data.util.BibframeConstants.INSTANCE_TITLE_URL;
import static org.folio.linked.data.util.BibframeConstants.MAIN_TITLE_URL;
import static org.folio.linked.data.util.BibframeConstants.NON_SORT_NUM_URL;
import static org.folio.linked.data.util.BibframeConstants.PART_NAME_URL;
import static org.folio.linked.data.util.BibframeConstants.PART_NUMBER_URL;
import static org.folio.linked.data.util.BibframeConstants.SUBTITLE_URL;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.HashMap;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.folio.linked.data.domain.dto.Instance;
import org.folio.linked.data.domain.dto.InstanceTitle;
import org.folio.linked.data.domain.dto.InstanceTitleField;
import org.folio.linked.data.mapper.resource.common.CoreMapper;
import org.folio.linked.data.mapper.resource.common.MapperUnit;
import org.folio.linked.data.mapper.resource.monograph.inner.instance.sub.InstanceSubResourceMapperUnit;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceType;
import org.folio.linked.data.service.dictionary.DictionaryService;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@MapperUnit(type = INSTANCE_TITLE, predicate = INSTANCE_TITLE_PRED, dtoClass = InstanceTitleField.class)
public class InstanceTitleMapperUnit implements InstanceSubResourceMapperUnit {

  private final DictionaryService<ResourceType> resourceTypeService;
  private final CoreMapper coreMapper;

  @Override
  public Instance toDto(Resource source, Instance destination) {
    var instanceTitle = coreMapper.readResourceDoc(source, InstanceTitle.class);
    destination.addTitleItem(new InstanceTitleField().instanceTitle(instanceTitle));
    return destination;
  }

  @Override
  public Resource toEntity(Object dto, String predicate) {
    var instanceTitle = ((InstanceTitleField) dto).getInstanceTitle();
    var resource = new Resource();
    resource.setLabel(INSTANCE_TITLE_URL);
    resource.setType(resourceTypeService.get(INSTANCE_TITLE));
    resource.setDoc(getDoc(instanceTitle));
    resource.setResourceHash(coreMapper.hash(resource));
    return resource;
  }

  private JsonNode getDoc(InstanceTitle dto) {
    var map = new HashMap<String, List<String>>();
    map.put(PART_NAME_URL, dto.getPartName());
    map.put(PART_NUMBER_URL, dto.getPartNumber());
    map.put(MAIN_TITLE_URL, dto.getMainTitle());
    map.put(NON_SORT_NUM_URL, dto.getNonSortNum());
    map.put(SUBTITLE_URL, dto.getSubtitle());
    return coreMapper.toJson(map);
  }

}