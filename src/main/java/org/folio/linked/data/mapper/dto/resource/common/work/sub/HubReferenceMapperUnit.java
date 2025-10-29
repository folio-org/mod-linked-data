package org.folio.linked.data.mapper.dto.resource.common.work.sub;

import static org.folio.ld.dictionary.PredicateDictionary.EXPRESSION_OF;
import static org.folio.ld.dictionary.PredicateDictionary.RELATED_TO;
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
import org.folio.linked.data.domain.dto.HubReference;
import org.folio.linked.data.domain.dto.HubReferenceWithType;
import org.folio.linked.data.domain.dto.WorkResponse;
import org.folio.linked.data.mapper.dto.resource.base.CoreMapper;
import org.folio.linked.data.mapper.dto.resource.base.MapperUnit;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.service.resource.hash.HashService;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@MapperUnit(type = HUB, predicate = {EXPRESSION_OF, RELATED_TO}, requestDto = HubReference.class)
public class HubReferenceMapperUnit implements WorkSubResourceMapperUnit {
  private final CoreMapper coreMapper;
  private final HashService hashService;

  @Override
  public <P> P toDto(Resource resourceToConvert, P parentDto, ResourceMappingContext context) {
    if (parentDto instanceof WorkResponse workDto && resourceToConvert.getDoc() != null) {
      var doc = resourceToConvert.getDoc();
      var hub = new HubReference()
        .addLabelItem(doc.get(PropertyDictionary.LABEL.getValue()).get(0).asText())
        .id(String.valueOf(resourceToConvert.getId()));
      var linkNode = doc.get(PropertyDictionary.LINK.getValue());
      if (linkNode != null && !linkNode.isEmpty()) {
        hub.addLinkItem(linkNode.get(0).asText());
      }
      workDto.addHubsItem(
        new HubReferenceWithType()
          .hub(hub)
          .relation(context.predicate().getUri())
      );
    }
    return parentDto;
  }

  @Override
  public Resource toEntity(Object dto, Resource parentEntity) {
    var hub = (HubReference) dto;
    var resource = new Resource();
    resource.addTypes(ResourceTypeDictionary.HUB);
    resource.setDoc(getDoc(hub));
    resource.setLabel(hub.getLabel().getFirst());
    resource.setIdAndRefreshEdges(hashService.hash(resource));
    return resource;
  }

  private JsonNode getDoc(HubReference dto) {
    var map = new HashMap<String, List<String>>();
    putProperty(map, LABEL, dto.getLabel());
    putProperty(map, LINK, dto.getLink());
    return map.isEmpty() ? null : coreMapper.toJson(map);
  }
}
