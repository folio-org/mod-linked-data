package org.folio.linked.data.mapper.dto.monograph.instance.sub;

import static org.folio.ld.dictionary.PredicateDictionary.ACCESS_LOCATION;
import static org.folio.ld.dictionary.PropertyDictionary.LINK;
import static org.folio.ld.dictionary.PropertyDictionary.NOTE;
import static org.folio.ld.dictionary.ResourceTypeDictionary.ANNOTATION;
import static org.folio.linked.data.model.entity.Resource.withInitializedSets;
import static org.folio.linked.data.util.BibframeUtils.getFirstValue;
import static org.folio.linked.data.util.BibframeUtils.putProperty;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.HashMap;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.folio.linked.data.domain.dto.AccessLocation;
import org.folio.linked.data.domain.dto.Instance;
import org.folio.linked.data.mapper.dto.common.CoreMapper;
import org.folio.linked.data.mapper.dto.common.MapperUnit;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.service.HashService;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@MapperUnit(type = ANNOTATION, predicate = ACCESS_LOCATION, dtoClass = AccessLocation.class)
public class AccessLocationMapperUnit implements InstanceSubResourceMapperUnit {

  private final CoreMapper coreMapper;
  private final HashService hashService;

  @Override
  public <P> P toDto(Resource source, P parentDto, Resource parentResource) {
    if (parentDto instanceof Instance instance) {
      var accessLocation = coreMapper.toDtoWithEdges(source, AccessLocation.class, false);
      accessLocation.setId(String.valueOf(source.getId()));
      instance.addAccessLocationItem(accessLocation);
    }
    return parentDto;
  }

  @Override
  public Resource toEntity(Object dto, Resource parentEntity) {
    var accessLocation = (AccessLocation) dto;
    var resource = withInitializedSets();
    resource.setLabel(getFirstValue(accessLocation::getLink));
    resource.addType(ANNOTATION);
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
