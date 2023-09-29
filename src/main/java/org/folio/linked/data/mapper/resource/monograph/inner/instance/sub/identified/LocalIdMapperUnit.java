package org.folio.linked.data.mapper.resource.monograph.inner.instance.sub.identified;

import static org.folio.linked.data.util.BibframeConstants.ASSIGNING_SOURCE;
import static org.folio.linked.data.util.BibframeConstants.LOCAL_ID;
import static org.folio.linked.data.util.BibframeConstants.LOCAL_ID_VALUE;
import static org.folio.linked.data.util.BibframeConstants.MAP_PRED;
import static org.folio.linked.data.util.BibframeUtils.getFirstValue;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.HashMap;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.folio.linked.data.domain.dto.Instance;
import org.folio.linked.data.domain.dto.LocalId;
import org.folio.linked.data.domain.dto.LocalIdField;
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
@MapperUnit(type = LOCAL_ID, predicate = MAP_PRED, dtoClass = LocalIdField.class)
public class LocalIdMapperUnit implements InstanceSubResourceMapperUnit {

  private final DictionaryService<ResourceType> resourceTypeService;
  private final CoreMapper coreMapper;

  @Override
  public Instance toDto(Resource source, Instance destination) {
    var localId = coreMapper.readResourceDoc(source, LocalId.class);
    localId.setId(String.valueOf(source.getResourceHash()));
    destination.addMapItem(new LocalIdField().localId(localId));
    return destination;
  }

  @Override
  public Resource toEntity(Object dto, String predicate, SubResourceMapper subResourceMapper) {
    var localId = ((LocalIdField) dto).getLocalId();
    var resource = new Resource();
    resource.setLabel(getFirstValue(localId::getValue));
    resource.addType(resourceTypeService.get(LOCAL_ID));
    resource.setDoc(getDoc(localId));
    resource.setResourceHash(coreMapper.hash(resource));
    return resource;
  }

  private JsonNode getDoc(LocalId dto) {
    var map = new HashMap<String, List<String>>();
    map.put(LOCAL_ID_VALUE, dto.getValue());
    map.put(ASSIGNING_SOURCE, dto.getAssigner());
    return coreMapper.toJson(map);
  }
}
