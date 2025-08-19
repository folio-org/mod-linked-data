package org.folio.linked.data.mapper.dto.resource.common.instance.sub;

import static java.util.Objects.nonNull;
import static java.util.Optional.ofNullable;
import static org.folio.ld.dictionary.PredicateDictionary.PE_DISTRIBUTION;
import static org.folio.ld.dictionary.PredicateDictionary.PE_MANUFACTURE;
import static org.folio.ld.dictionary.PredicateDictionary.PE_PRODUCTION;
import static org.folio.ld.dictionary.PredicateDictionary.PE_PUBLICATION;
import static org.folio.ld.dictionary.PredicateDictionary.PROVIDER_PLACE;
import static org.folio.ld.dictionary.PropertyDictionary.DATE;
import static org.folio.ld.dictionary.PropertyDictionary.NAME;
import static org.folio.ld.dictionary.PropertyDictionary.PROVIDER_DATE;
import static org.folio.ld.dictionary.PropertyDictionary.SIMPLE_PLACE;
import static org.folio.ld.dictionary.ResourceTypeDictionary.PROVIDER_EVENT;
import static org.folio.linked.data.util.ResourceUtils.getFirstValue;
import static org.folio.linked.data.util.ResourceUtils.putProperty;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.folio.linked.data.domain.dto.InstanceResponse;
import org.folio.linked.data.domain.dto.ProviderEventRequest;
import org.folio.linked.data.domain.dto.ProviderEventResponse;
import org.folio.linked.data.mapper.dto.resource.base.CoreMapper;
import org.folio.linked.data.mapper.dto.resource.base.MapperUnit;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.service.resource.hash.HashService;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@MapperUnit(
  type = PROVIDER_EVENT,
  predicate = {PE_DISTRIBUTION, PE_MANUFACTURE, PE_PUBLICATION, PE_PRODUCTION},
  requestDto = ProviderEventRequest.class
)
@Component
public class ProviderEventMapperUnit implements InstanceSubResourceMapperUnit {

  private final CoreMapper coreMapper;
  private final HashService hashService;

  @Override
  public <P> P toDto(Resource resourceToConvert, P parentDto, ResourceMappingContext context) {
    if (parentDto instanceof InstanceResponse instance) {
      var providerEvent = coreMapper.toDtoWithEdges(resourceToConvert, ProviderEventResponse.class, false);
      var predicateUri = context.predicate().getUri();
      if (PE_DISTRIBUTION.getUri().equals(predicateUri)) {
        instance.addDistributionItem(providerEvent);
      } else if (PE_MANUFACTURE.getUri().equals(predicateUri)) {
        instance.addManufactureItem(providerEvent);
      } else if (PE_PRODUCTION.getUri().equals(predicateUri)) {
        instance.addProductionItem(providerEvent);
      } else if (PE_PUBLICATION.getUri().equals(predicateUri)) {
        instance.addPublicationItem(providerEvent);
      }
    }
    return parentDto;
  }

  @Override
  public Resource toEntity(Object dto, Resource parentEntity) {
    var providerEvent = (ProviderEventRequest) dto;
    var resource = new Resource();
    resource.setLabel(getFirstValue(() -> getPossibleLabels(providerEvent)));
    resource.addTypes(PROVIDER_EVENT);
    resource.setDoc(getDoc(providerEvent));
    coreMapper.addOutgoingEdges(resource, ProviderEventRequest.class, providerEvent.getProviderPlace(), PROVIDER_PLACE);
    resource.setId(hashService.hash(resource));
    return resource;
  }

  private List<String> getPossibleLabels(ProviderEventRequest providerEvent) {
    var result = new ArrayList<String>();
    ofNullable(providerEvent.getName()).ifPresent(result::addAll);
    ofNullable(providerEvent.getSimplePlace()).ifPresent(result::addAll);
    ofNullable(providerEvent.getProviderPlace()).ifPresent(
      pp -> result.addAll(pp.stream().filter(p -> nonNull(p.getLabel())).flatMap(p -> p.getLabel().stream()).toList()));
    return result;
  }

  private JsonNode getDoc(ProviderEventRequest dto) {
    var map = new HashMap<String, List<String>>();
    putProperty(map, DATE, dto.getDate());
    putProperty(map, NAME, dto.getName());
    putProperty(map, PROVIDER_DATE, dto.getProviderDate());
    putProperty(map, SIMPLE_PLACE, dto.getSimplePlace());
    return map.isEmpty() ? null : coreMapper.toJson(map);
  }

}
