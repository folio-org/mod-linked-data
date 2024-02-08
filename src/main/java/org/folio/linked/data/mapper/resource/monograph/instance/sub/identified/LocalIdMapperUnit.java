package org.folio.linked.data.mapper.resource.monograph.instance.sub.identified;

import static org.folio.ld.dictionary.PredicateDictionary.MAP;
import static org.folio.ld.dictionary.PropertyDictionary.ASSIGNING_SOURCE;
import static org.folio.ld.dictionary.PropertyDictionary.LOCAL_ID_VALUE;
import static org.folio.ld.dictionary.ResourceTypeDictionary.ID_LOCAL;
import static org.folio.linked.data.util.BibframeUtils.getFirstValue;
import static org.folio.linked.data.util.BibframeUtils.putProperty;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.HashMap;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.folio.linked.data.domain.dto.Instance;
import org.folio.linked.data.domain.dto.LocalId;
import org.folio.linked.data.domain.dto.LocalIdField;
import org.folio.linked.data.mapper.resource.common.CoreMapper;
import org.folio.linked.data.mapper.resource.common.MapperUnit;
import org.folio.linked.data.mapper.resource.monograph.instance.sub.InstanceSubResourceMapperUnit;
import org.folio.linked.data.model.entity.Resource;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@MapperUnit(type = ID_LOCAL, predicate = MAP, dtoClass = LocalIdField.class)
public class LocalIdMapperUnit implements InstanceSubResourceMapperUnit {

  private final CoreMapper coreMapper;

  @Override
  public <P> P toDto(Resource source, P parentDto, Resource parentResource) {
    if (parentDto instanceof Instance instance) {
      var localId = coreMapper.toDtoWithEdges(source, LocalId.class, false);
      localId.setId(String.valueOf(source.getResourceHash()));
      instance.addMapItem(new LocalIdField().localId(localId));
    }
    return parentDto;
  }

  @Override
  public Resource toEntity(Object dto, Resource parentEntity) {
    var localId = ((LocalIdField) dto).getLocalId();
    var resource = new Resource();
    resource.setLabel(getFirstValue(localId::getValue));
    resource.addType(ID_LOCAL);
    resource.setDoc(getDoc(localId));
    resource.setResourceHash(coreMapper.hash(resource));
    return resource;
  }

  private JsonNode getDoc(LocalId dto) {
    var map = new HashMap<String, List<String>>();
    putProperty(map, LOCAL_ID_VALUE, dto.getValue());
    putProperty(map, ASSIGNING_SOURCE, dto.getAssigner());
    return map.isEmpty() ? null : coreMapper.toJson(map);
  }

}
