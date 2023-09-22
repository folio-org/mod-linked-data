package org.folio.linked.data.mapper.resource.monograph.inner.instance.sub;

import static com.google.common.collect.Iterables.getFirst;
import static org.folio.linked.data.util.BibframeConstants.ACCESS_LOCATION;
import static org.folio.linked.data.util.BibframeConstants.ACCESS_LOCATION_PRED;
import static org.folio.linked.data.util.BibframeConstants.LINK;
import static org.folio.linked.data.util.BibframeConstants.NOTE;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.HashMap;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.folio.linked.data.domain.dto.AccessLocation;
import org.folio.linked.data.domain.dto.Instance;
import org.folio.linked.data.mapper.resource.common.CoreMapper;
import org.folio.linked.data.mapper.resource.common.MapperUnit;
import org.folio.linked.data.mapper.resource.common.inner.sub.SubResourceMapper;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceType;
import org.folio.linked.data.service.dictionary.DictionaryService;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@MapperUnit(type = ACCESS_LOCATION, predicate = ACCESS_LOCATION_PRED, dtoClass = AccessLocation.class)
public class AccessLocationMapperUnit implements InstanceSubResourceMapperUnit {

  private final CoreMapper coreMapper;
  private final DictionaryService<ResourceType> resourceTypeService;

  @Override
  public Instance toDto(Resource source, Instance destination) {
    var accessLocation = coreMapper.readResourceDoc(source, AccessLocation.class);
    destination.addAccessLocationItem(accessLocation);
    return destination;
  }

  @Override
  public Resource toEntity(Object dto, String predicate, SubResourceMapper subResourceMapper) {
    var accessLocation = (AccessLocation) dto;
    var resource = new Resource();
    resource.setLabel(getFirst(accessLocation.getLabel(), getFirst(accessLocation.getLink(), "")));
    resource.addType(resourceTypeService.get(ACCESS_LOCATION));
    resource.setDoc(getDoc(accessLocation));
    resource.setResourceHash(coreMapper.hash(resource));
    return resource;
  }

  private JsonNode getDoc(AccessLocation dto) {
    var map = new HashMap<String, List<String>>();
    map.put(LINK, dto.getLink());
    map.put(NOTE, dto.getNote());
    return coreMapper.toJson(map);
  }
}
