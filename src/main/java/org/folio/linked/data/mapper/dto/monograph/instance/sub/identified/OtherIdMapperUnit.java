package org.folio.linked.data.mapper.dto.monograph.instance.sub.identified;

import static org.folio.ld.dictionary.PredicateDictionary.MAP;
import static org.folio.ld.dictionary.PropertyDictionary.NAME;
import static org.folio.ld.dictionary.PropertyDictionary.QUALIFIER;
import static org.folio.ld.dictionary.ResourceTypeDictionary.IDENTIFIER;
import static org.folio.ld.dictionary.ResourceTypeDictionary.ID_UNKNOWN;
import static org.folio.linked.data.util.ResourceUtils.getFirstValue;
import static org.folio.linked.data.util.ResourceUtils.putProperty;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.HashMap;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.folio.linked.data.domain.dto.InstanceResponse;
import org.folio.linked.data.domain.dto.OtherId;
import org.folio.linked.data.domain.dto.OtherIdField;
import org.folio.linked.data.domain.dto.OtherIdFieldResponse;
import org.folio.linked.data.domain.dto.OtherIdResponse;
import org.folio.linked.data.mapper.dto.common.CoreMapper;
import org.folio.linked.data.mapper.dto.common.MapperUnit;
import org.folio.linked.data.mapper.dto.monograph.instance.sub.InstanceSubResourceMapperUnit;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.service.resource.hash.HashService;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@MapperUnit(type = ID_UNKNOWN, predicate = MAP, requestDto = OtherIdField.class)
public class OtherIdMapperUnit implements InstanceSubResourceMapperUnit {

  private final CoreMapper coreMapper;
  private final HashService hashService;

  @Override
  public <P> P toDto(Resource resourceToConvert, P parentDto, ResourceMappingContext context) {
    if (parentDto instanceof InstanceResponse instance) {
      var otherId = coreMapper.toDtoWithEdges(resourceToConvert, OtherIdResponse.class, false);
      otherId.setId(String.valueOf(resourceToConvert.getId()));
      instance.addMapItem(new OtherIdFieldResponse().identifier(otherId));
    }
    return parentDto;
  }

  @Override
  public Resource toEntity(Object dto, Resource parentEntity) {
    var otherId = ((OtherIdField) dto).getIdentifier();
    var resource = new Resource();
    resource.setLabel(getFirstValue(otherId::getValue));
    resource.addTypes(IDENTIFIER, ID_UNKNOWN);
    resource.setDoc(getDoc(otherId));
    resource.setId(hashService.hash(resource));
    return resource;
  }

  private JsonNode getDoc(OtherId dto) {
    var map = new HashMap<String, List<String>>();
    putProperty(map, NAME, dto.getValue());
    putProperty(map, QUALIFIER, dto.getQualifier());
    return map.isEmpty() ? null : coreMapper.toJson(map);
  }

}
