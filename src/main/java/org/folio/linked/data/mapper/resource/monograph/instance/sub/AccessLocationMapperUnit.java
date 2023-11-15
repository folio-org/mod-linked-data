package org.folio.linked.data.mapper.resource.monograph.instance.sub;

import static org.folio.ld.dictionary.PredicateDictionary.ACCESS_LOCATION;
import static org.folio.ld.dictionary.PropertyDictionary.LINK;
import static org.folio.ld.dictionary.PropertyDictionary.NOTE;
import static org.folio.ld.dictionary.ResourceTypeDictionary.ANNOTATION;
import static org.folio.linked.data.util.BibframeUtils.getFirstValue;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.HashMap;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.folio.linked.data.domain.dto.AccessLocation;
import org.folio.linked.data.domain.dto.Instance;
import org.folio.linked.data.mapper.resource.common.CoreMapper;
import org.folio.linked.data.mapper.resource.common.MapperUnit;
import org.folio.linked.data.model.entity.Resource;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@MapperUnit(type = ANNOTATION, predicate = ACCESS_LOCATION, dtoClass = AccessLocation.class)
public class AccessLocationMapperUnit implements InstanceSubResourceMapperUnit {

  private final CoreMapper coreMapper;

  @Override
  public Instance toDto(Resource source, Instance destination) {
    var accessLocation = coreMapper.readResourceDoc(source, AccessLocation.class);
    accessLocation.setId(String.valueOf(source.getResourceHash()));
    destination.addAccessLocationItem(accessLocation);
    return destination;
  }

  @Override
  public Resource toEntity(Object dto) {
    var accessLocation = (AccessLocation) dto;
    var resource = new Resource();
    resource.setLabel(getFirstValue(accessLocation::getLink));
    resource.addType(ANNOTATION);
    resource.setDoc(getDoc(accessLocation));
    resource.setResourceHash(coreMapper.hash(resource));
    return resource;
  }

  private JsonNode getDoc(AccessLocation dto) {
    var map = new HashMap<String, List<String>>();
    map.put(LINK.getValue(), dto.getLink());
    map.put(NOTE.getValue(), dto.getNote());
    return coreMapper.toJson(map);
  }
}
