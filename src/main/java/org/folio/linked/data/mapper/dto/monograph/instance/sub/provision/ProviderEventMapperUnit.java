package org.folio.linked.data.mapper.dto.monograph.instance.sub.provision;

import static java.util.Objects.nonNull;
import static java.util.Optional.ofNullable;
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
import java.util.function.BiFunction;
import lombok.RequiredArgsConstructor;
import org.folio.linked.data.domain.dto.InstanceResponse;
import org.folio.linked.data.domain.dto.ProviderEventRequest;
import org.folio.linked.data.domain.dto.ProviderEventResponse;
import org.folio.linked.data.mapper.dto.common.CoreMapper;
import org.folio.linked.data.mapper.dto.monograph.instance.sub.InstanceSubResourceMapperUnit;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.service.resource.hash.HashService;

@RequiredArgsConstructor
public abstract class ProviderEventMapperUnit implements InstanceSubResourceMapperUnit {

  private final CoreMapper coreMapper;
  private final HashService hashService;
  private final BiFunction<ProviderEventResponse, InstanceResponse, InstanceResponse> providerEventConsumer;

  @Override
  public <P> P toDto(Resource source, P parentDto, Resource parentResource) {
    if (parentDto instanceof InstanceResponse instance) {
      var providerEvent = coreMapper.toDtoWithEdges(source, ProviderEventResponse.class, false);
      providerEventConsumer.apply(providerEvent, instance);
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
