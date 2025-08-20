package org.folio.linked.data.mapper.dto.resource.common.instance.sub;

import static org.folio.ld.dictionary.PredicateDictionary.ACCESS_LOCATION;
import static org.folio.ld.dictionary.PropertyDictionary.LINK;
import static org.folio.ld.dictionary.PropertyDictionary.NOTE;
import static org.folio.ld.dictionary.ResourceTypeDictionary.ANNOTATION;
import static org.folio.linked.data.util.ResourceUtils.getFirstValue;
import static org.folio.linked.data.util.ResourceUtils.putProperty;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.HashMap;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.folio.linked.data.domain.dto.AccessLocation;
import org.folio.linked.data.domain.dto.AccessLocationResponse;
import org.folio.linked.data.domain.dto.InstanceResponse;
import org.folio.linked.data.mapper.dto.resource.base.CoreMapper;
import org.folio.linked.data.mapper.dto.resource.base.MapperUnit;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.service.resource.hash.HashService;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@MapperUnit(type = ANNOTATION, predicate = ACCESS_LOCATION, requestDto = AccessLocation.class)
public class AccessLocationMapperUnit implements InstanceSubResourceMapperUnit {

  private final CoreMapper coreMapper;
  private final HashService hashService;

  @Override
  public <P> P toDto(Resource resourceToConvert, P parentDto, ResourceMappingContext context) {
    if (parentDto instanceof InstanceResponse instance) {
      var accessLocation = coreMapper.toDtoWithEdges(resourceToConvert, AccessLocationResponse.class, false);
      accessLocation.setId(String.valueOf(resourceToConvert.getId()));
      instance.addAccessLocationItem(accessLocation);
    }
    return parentDto;
  }

  @Override
  public Resource toEntity(Object dto, Resource parentEntity) {
    var accessLocation = (AccessLocation) dto;
    var resource = new Resource();
    resource.setLabel(getFirstValue(accessLocation::getLink));
    resource.addTypes(ANNOTATION);
    resource.setDoc(getDoc(accessLocation));
    resource.setId(hashService.hash(resource));
    return resource;
  }

  private JsonNode getDoc(AccessLocation dto) {
    var map = new HashMap<String, List<String>>();
    putProperty(map, LINK, dto.getLink());
    putProperty(map, NOTE, dto.getNote());
    return map.isEmpty() ? null : coreMapper.toJson(map);
  }

}
