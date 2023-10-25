package org.folio.linked.data.mapper.resource.monograph.inner.instance.sub.title;

import static org.folio.ld.dictionary.PropertyDictionary.MAIN_TITLE;
import static org.folio.ld.dictionary.PropertyDictionary.NON_SORT_NUM;
import static org.folio.ld.dictionary.PropertyDictionary.PART_NAME;
import static org.folio.ld.dictionary.PropertyDictionary.PART_NUMBER;
import static org.folio.ld.dictionary.PropertyDictionary.SUBTITLE;
import static org.folio.ld.dictionary.ResourceTypeDictionary.TITLE;
import static org.folio.linked.data.util.BibframeUtils.getFirstValue;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.HashMap;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.folio.ld.dictionary.PredicateDictionary;
import org.folio.linked.data.domain.dto.Instance;
import org.folio.linked.data.domain.dto.InstanceTitle;
import org.folio.linked.data.domain.dto.InstanceTitleField;
import org.folio.linked.data.mapper.resource.common.CoreMapper;
import org.folio.linked.data.mapper.resource.common.MapperUnit;
import org.folio.linked.data.mapper.resource.monograph.inner.instance.sub.InstanceSubResourceMapperUnit;
import org.folio.linked.data.model.entity.Resource;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@MapperUnit(type = TITLE, predicate = PredicateDictionary.TITLE, dtoClass = InstanceTitleField.class)
public class InstanceTitleMapperUnit implements InstanceSubResourceMapperUnit {

  private final CoreMapper coreMapper;

  @Override
  public Instance toDto(Resource source, Instance destination) {
    var instanceTitle = coreMapper.readResourceDoc(source, InstanceTitle.class);
    instanceTitle.setId(String.valueOf(source.getResourceHash()));
    destination.addTitleItem(new InstanceTitleField().instanceTitle(instanceTitle));
    return destination;
  }

  @Override
  public Resource toEntity(Object dto) {
    var instanceTitle = ((InstanceTitleField) dto).getInstanceTitle();
    var resource = new Resource();
    resource.setLabel(getFirstValue(instanceTitle::getMainTitle));
    resource.addType(TITLE);
    resource.setDoc(getDoc(instanceTitle));
    resource.setResourceHash(coreMapper.hash(resource));
    return resource;
  }

  private JsonNode getDoc(InstanceTitle dto) {
    var map = new HashMap<String, List<String>>();
    map.put(PART_NAME.getValue(), dto.getPartName());
    map.put(PART_NUMBER.getValue(), dto.getPartNumber());
    map.put(MAIN_TITLE.getValue(), dto.getMainTitle());
    map.put(NON_SORT_NUM.getValue(), dto.getNonSortNum());
    map.put(SUBTITLE.getValue(), dto.getSubTitle());
    return coreMapper.toJson(map);
  }

}
