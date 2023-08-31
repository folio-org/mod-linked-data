package org.folio.linked.data.mapper.resource.monograph.inner.instance.sub.title;

import static org.folio.linked.data.util.Bibframe2Constants.INSTANCE_TITLE_2;
import static org.folio.linked.data.util.Bibframe2Constants.INSTANCE_TITLE_2_PRED;
import static org.folio.linked.data.util.Bibframe2Constants.INSTANCE_TITLE_URL;
import static org.folio.linked.data.util.Bibframe2Constants.MAIN_TITLE_URL;
import static org.folio.linked.data.util.Bibframe2Constants.NON_SORT_NUM_URL;
import static org.folio.linked.data.util.Bibframe2Constants.PART_NAME_URL;
import static org.folio.linked.data.util.Bibframe2Constants.PART_NUMBER_URL;
import static org.folio.linked.data.util.Bibframe2Constants.SUBTITLE_URL;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.HashMap;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.folio.linked.data.domain.dto.Instance2;
import org.folio.linked.data.domain.dto.InstanceTitle2;
import org.folio.linked.data.domain.dto.InstanceTitleField2;
import org.folio.linked.data.mapper.resource.common.CoreMapper;
import org.folio.linked.data.mapper.resource.common.MapperUnit;
import org.folio.linked.data.mapper.resource.common.inner.sub.SubResourceMapper;
import org.folio.linked.data.mapper.resource.monograph.inner.instance.sub.Instance2SubResourceMapperUnit;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceType;
import org.folio.linked.data.service.dictionary.DictionaryService;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@MapperUnit(type = INSTANCE_TITLE_URL, predicate = INSTANCE_TITLE_2_PRED, dtoClass = InstanceTitleField2.class)
public class InstanceTitle2MapperUnit implements Instance2SubResourceMapperUnit {

  private final DictionaryService<ResourceType> resourceTypeService;
  private final CoreMapper coreMapper;

  @Override
  public Instance2 toDto(Resource source, Instance2 destination) {
    var instanceTitle = coreMapper.readResourceDoc(source, InstanceTitle2.class);
    destination.addTitleItem(new InstanceTitleField2().instanceTitle(instanceTitle));
    return destination;
  }

  @Override
  public Resource toEntity(Object dto, String predicate, SubResourceMapper subResourceMapper) {
    var instanceTitle = ((InstanceTitleField2) dto).getInstanceTitle();
    var resource = new Resource();
    resource.setLabel(INSTANCE_TITLE_URL);
    resource.setType(resourceTypeService.get(INSTANCE_TITLE_2));
    resource.setDoc(getDoc(instanceTitle));
    resource.setResourceHash(coreMapper.hash(resource));
    return resource;
  }

  private JsonNode getDoc(InstanceTitle2 dto) {
    var map = new HashMap<String, List<String>>();
    map.put(PART_NAME_URL, dto.getPartName());
    map.put(PART_NUMBER_URL, dto.getPartNumber());
    map.put(MAIN_TITLE_URL, dto.getMainTitle());
    map.put(NON_SORT_NUM_URL, dto.getNonSortNum());
    map.put(SUBTITLE_URL, dto.getSubtitle());
    return coreMapper.toJson(map);
  }

}
