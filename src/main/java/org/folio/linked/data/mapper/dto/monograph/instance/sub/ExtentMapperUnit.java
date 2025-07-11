package org.folio.linked.data.mapper.dto.monograph.instance.sub;

import static org.folio.ld.dictionary.ResourceTypeDictionary.EXTENT;
import static org.folio.linked.data.util.ResourceUtils.getFirstValue;
import static org.folio.linked.data.util.ResourceUtils.putProperty;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.HashMap;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.folio.ld.dictionary.PredicateDictionary;
import org.folio.ld.dictionary.PropertyDictionary;
import org.folio.linked.data.domain.dto.Extent;
import org.folio.linked.data.domain.dto.ExtentResponse;
import org.folio.linked.data.domain.dto.InstanceResponse;
import org.folio.linked.data.mapper.dto.common.CoreMapper;
import org.folio.linked.data.mapper.dto.common.MapperUnit;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.service.resource.hash.HashService;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@MapperUnit(type = EXTENT, predicate = PredicateDictionary.EXTENT, requestDto = Extent.class)
public class ExtentMapperUnit implements InstanceSubResourceMapperUnit {

  private final CoreMapper coreMapper;
  private final HashService hashService;

  @Override
  public <P> P toDto(Resource source, P parentDto, Resource parentResource) {
    if (parentDto instanceof InstanceResponse instance) {
      var extent = coreMapper.toDtoWithEdges(source, ExtentResponse.class);
      extent.setId(String.valueOf(source.getId()));
      instance.addExtentV2Item(extent);
    }
    return parentDto;
  }

  @Override
  public Resource toEntity(Object dto, Resource parentEntity) {
    var extent = (Extent) dto;
    var resource = new Resource()
      .setLabel(getFirstValue(extent::getLabel))
      .addTypes(EXTENT)
      .setDoc(getDoc(extent));
    resource.setId(hashService.hash(resource));
    return resource;
  }

  private JsonNode getDoc(Extent dto) {
    var map = new HashMap<String, List<String>>();
    putProperty(map, PropertyDictionary.LABEL, dto.getLabel());
    putProperty(map, PropertyDictionary.MATERIALS_SPECIFIED, dto.getMaterialsSpec());
    putProperty(map, PropertyDictionary.NOTE, dto.getNote());
    return map.isEmpty() ? null : coreMapper.toJson(map);
  }
}
