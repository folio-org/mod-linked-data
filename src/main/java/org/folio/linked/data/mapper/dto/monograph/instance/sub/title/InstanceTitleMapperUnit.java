package org.folio.linked.data.mapper.dto.monograph.instance.sub.title;

import static org.folio.ld.dictionary.PropertyDictionary.MAIN_TITLE;
import static org.folio.ld.dictionary.PropertyDictionary.NON_SORT_NUM;
import static org.folio.ld.dictionary.PropertyDictionary.PART_NAME;
import static org.folio.ld.dictionary.PropertyDictionary.PART_NUMBER;
import static org.folio.ld.dictionary.PropertyDictionary.SUBTITLE;
import static org.folio.ld.dictionary.ResourceTypeDictionary.TITLE;
import static org.folio.linked.data.util.BibframeUtils.getFirstValue;
import static org.folio.linked.data.util.BibframeUtils.putProperty;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.HashMap;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.folio.ld.dictionary.PredicateDictionary;
import org.folio.linked.data.domain.dto.Instance;
import org.folio.linked.data.domain.dto.InstanceReference;
import org.folio.linked.data.domain.dto.InstanceTitle;
import org.folio.linked.data.domain.dto.InstanceTitleField;
import org.folio.linked.data.mapper.dto.common.CoreMapper;
import org.folio.linked.data.mapper.dto.common.MapperUnit;
import org.folio.linked.data.mapper.dto.monograph.instance.sub.InstanceSubResourceMapperUnit;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.service.HashService;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@MapperUnit(type = TITLE, predicate = PredicateDictionary.TITLE, dtoClass = InstanceTitleField.class)
public class InstanceTitleMapperUnit implements InstanceSubResourceMapperUnit {

  private final CoreMapper coreMapper;
  private final HashService hashService;

  @Override
  public <P> P toDto(Resource source, P parentDto, Resource parentResource) {
    var instanceTitle = coreMapper.toDtoWithEdges(source, InstanceTitle.class, false);
    instanceTitle.setId(String.valueOf(source.getResourceHash()));
    if (parentDto instanceof Instance instance) {
      instance.addTitleItem(new InstanceTitleField().instanceTitle(instanceTitle));
    }
    if (parentDto instanceof InstanceReference instance) {
      instance.addTitleItem(new InstanceTitleField().instanceTitle(instanceTitle));
    }
    return parentDto;
  }

  @Override
  public Resource toEntity(Object dto, Resource parentEntity) {
    var instanceTitle = ((InstanceTitleField) dto).getInstanceTitle();
    var resource = new Resource(true);
    resource.setLabel(getFirstValue(instanceTitle::getMainTitle));
    resource.addType(TITLE);
    resource.setDoc(getDoc(instanceTitle));
    resource.setResourceHash(hashService.hash(resource));
    return resource;
  }

  private JsonNode getDoc(InstanceTitle dto) {
    var map = new HashMap<String, List<String>>();
    putProperty(map, PART_NAME, dto.getPartName());
    putProperty(map, PART_NUMBER, dto.getPartNumber());
    putProperty(map, MAIN_TITLE, dto.getMainTitle());
    putProperty(map, NON_SORT_NUM, dto.getNonSortNum());
    putProperty(map, SUBTITLE, dto.getSubTitle());
    return map.isEmpty() ? null : coreMapper.toJson(map);
  }

}
