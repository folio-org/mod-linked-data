package org.folio.linked.data.mapper.dto.resource.common.instance.sub;

import static org.folio.ld.dictionary.ResourceTypeDictionary.SUPPLEMENTARY_CONTENT;
import static org.folio.linked.data.util.ResourceUtils.getFirstValue;
import static org.folio.linked.data.util.ResourceUtils.putProperty;

import java.util.HashMap;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.folio.ld.dictionary.PredicateDictionary;
import org.folio.ld.dictionary.PropertyDictionary;
import org.folio.linked.data.domain.dto.InstanceResponse;
import org.folio.linked.data.domain.dto.SupplementaryContent;
import org.folio.linked.data.domain.dto.SupplementaryContentResponse;
import org.folio.linked.data.mapper.dto.resource.base.CoreMapper;
import org.folio.linked.data.mapper.dto.resource.base.MapperUnit;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.service.resource.hash.HashService;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;

@Component
@RequiredArgsConstructor
@MapperUnit(type = SUPPLEMENTARY_CONTENT, predicate = PredicateDictionary.SUPPLEMENTARY_CONTENT,
  requestDto = SupplementaryContent.class)
public class InstanceSupplementaryContentMapperUnit implements InstanceSubResourceMapperUnit {

  private final CoreMapper coreMapper;
  private final HashService hashService;

  @Override
  public <P> P toDto(Resource resource, P parentDto, ResourceMappingContext context) {
    if (parentDto instanceof InstanceResponse instance) {
      var supplementaryContent = coreMapper.toDtoWithEdges(resource, SupplementaryContentResponse.class, false);
      supplementaryContent.setId(String.valueOf(resource.getId()));
      instance.addSupplementaryContentItem(supplementaryContent);
    }
    return parentDto;
  }

  @Override
  public Resource toEntity(Object dto, Resource parentEntity) {
    var supplementaryContent = (SupplementaryContent) dto;
    var resource = new Resource()
      .setLabel(getFirstValue(supplementaryContent::getName))
      .addTypes(SUPPLEMENTARY_CONTENT)
      .setDoc(getDoc(supplementaryContent));
    resource.setIdAndRefreshEdges(hashService.hash(resource));
    return resource;
  }

  private JsonNode getDoc(SupplementaryContent dto) {
    var map = new HashMap<String, List<String>>();
    putProperty(map, PropertyDictionary.LINK, dto.getLink());
    putProperty(map, PropertyDictionary.NAME, dto.getName());
    return map.isEmpty() ? null : coreMapper.toJson(map);
  }
}
