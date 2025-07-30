package org.folio.linked.data.mapper.dto.monograph.instance.sub.identified;

import static org.folio.ld.dictionary.PredicateDictionary.MAP;
import static org.folio.ld.dictionary.PropertyDictionary.EAN_VALUE;
import static org.folio.ld.dictionary.PropertyDictionary.QUALIFIER;
import static org.folio.ld.dictionary.ResourceTypeDictionary.IDENTIFIER;
import static org.folio.ld.dictionary.ResourceTypeDictionary.ID_EAN;
import static org.folio.linked.data.util.ResourceUtils.getFirstValue;
import static org.folio.linked.data.util.ResourceUtils.putProperty;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.HashMap;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.folio.linked.data.domain.dto.EanField;
import org.folio.linked.data.domain.dto.EanFieldResponse;
import org.folio.linked.data.domain.dto.EanResponse;
import org.folio.linked.data.domain.dto.IdentifierWithQualifierRequest;
import org.folio.linked.data.domain.dto.InstanceResponse;
import org.folio.linked.data.mapper.dto.common.CoreMapper;
import org.folio.linked.data.mapper.dto.common.MapperUnit;
import org.folio.linked.data.mapper.dto.monograph.instance.sub.InstanceSubResourceMapperUnit;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.service.resource.hash.HashService;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@MapperUnit(type = ID_EAN, predicate = MAP, requestDto = EanField.class)
public class EanMapperUnit implements InstanceSubResourceMapperUnit {

  private final CoreMapper coreMapper;
  private final HashService hashService;

  @Override
  public <P> P toDto(Resource resourceToConvert, P parentDto, ResourceMappingContext context) {
    if (parentDto instanceof InstanceResponse instance) {
      var ean = coreMapper.toDtoWithEdges(resourceToConvert, EanResponse.class, false);
      ean.setId(String.valueOf(resourceToConvert.getId()));
      instance.addMapItem(new EanFieldResponse().ean(ean));
    }
    return parentDto;
  }

  @Override
  public Resource toEntity(Object dto, Resource parentEntity) {
    var ean = ((EanField) dto).getEan();
    var resource = new Resource();
    resource.setLabel(getFirstValue(ean::getValue));
    resource.addTypes(IDENTIFIER, ID_EAN);
    resource.setDoc(getDoc(ean));
    resource.setId(hashService.hash(resource));
    return resource;
  }

  private JsonNode getDoc(IdentifierWithQualifierRequest dto) {
    var map = new HashMap<String, List<String>>();
    putProperty(map, EAN_VALUE, dto.getValue());
    putProperty(map, QUALIFIER, dto.getQualifier());
    return map.isEmpty() ? null : coreMapper.toJson(map);
  }

}
