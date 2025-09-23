package org.folio.linked.data.mapper.dto.resource.common.work.sub;

import static org.folio.ld.dictionary.PredicateDictionary.EXPRESSION_OF;
import static org.folio.ld.dictionary.PropertyDictionary.LABEL;
import static org.folio.ld.dictionary.PropertyDictionary.LINK;
import static org.folio.ld.dictionary.ResourceTypeDictionary.HUB;
import static org.folio.linked.data.util.ResourceUtils.putProperty;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.HashMap;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.folio.ld.dictionary.PropertyDictionary;
import org.folio.ld.dictionary.ResourceTypeDictionary;
import org.folio.linked.data.domain.dto.Hub;
import org.folio.linked.data.domain.dto.HubAllOfHub;
import org.folio.linked.data.domain.dto.HubResponse;
import org.folio.linked.data.domain.dto.WorkResponse;
import org.folio.linked.data.mapper.dto.resource.base.CoreMapper;
import org.folio.linked.data.mapper.dto.resource.base.MapperUnit;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.service.resource.hash.HashService;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@MapperUnit(type = HUB, predicate = EXPRESSION_OF, requestDto = Hub.class)
public class HubMapperUnit implements WorkSubResourceMapperUnit {
  private final CoreMapper coreMapper;
  private final HashService hashService;

  @Override
  public <P> P toDto(Resource resourceToConvert, P parentDto, ResourceMappingContext context) {
    if (parentDto instanceof WorkResponse workDto) {
      var hub = new HubResponse();
      var doc = resourceToConvert.getDoc();
      if (doc != null) {
        hub.setHub(new HubAllOfHub()
          .addLabelItem(doc.get(PropertyDictionary.LABEL.getValue()).get(0).asText())
          .addLinkItem(doc.get(PropertyDictionary.LINK.getValue()).get(0).asText()));
        hub.setId(String.valueOf(resourceToConvert.getId()));
        hub.setRelation(context.predicate().getUri());
        workDto.addHubsItem(hub);
      }
    }
    return parentDto;
  }

  @Override
  public Resource toEntity(Object dto, Resource parentEntity) {
    var hub = (Hub) dto;
    var resource = new Resource();
    resource.addTypes(ResourceTypeDictionary.HUB);
    resource.setDoc(getDoc(hub));
    resource.setLabel(hub.getHub().getLabel().getFirst());
    resource.setId(hashService.hash(resource));
    return resource;
  }

  private JsonNode getDoc(Hub dto) {
    var map = new HashMap<String, List<String>>();
    putProperty(map, LABEL, dto.getHub().getLabel());
    putProperty(map, LINK, dto.getHub().getLink());
    return map.isEmpty() ? null : coreMapper.toJson(map);
  }
}
