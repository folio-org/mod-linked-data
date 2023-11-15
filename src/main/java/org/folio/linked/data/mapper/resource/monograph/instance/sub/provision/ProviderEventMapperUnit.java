package org.folio.linked.data.mapper.resource.monograph.instance.sub.provision;

import static org.folio.ld.dictionary.PredicateDictionary.PROVIDER_PLACE;
import static org.folio.ld.dictionary.PropertyDictionary.DATE;
import static org.folio.ld.dictionary.PropertyDictionary.NAME;
import static org.folio.ld.dictionary.PropertyDictionary.PROVIDER_DATE;
import static org.folio.ld.dictionary.PropertyDictionary.SIMPLE_PLACE;
import static org.folio.ld.dictionary.ResourceTypeDictionary.PROVIDER_EVENT;
import static org.folio.linked.data.util.BibframeUtils.getFirstValue;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.BiFunction;
import lombok.RequiredArgsConstructor;
import org.folio.linked.data.domain.dto.Instance;
import org.folio.linked.data.domain.dto.ProviderEvent;
import org.folio.linked.data.mapper.resource.common.CoreMapper;
import org.folio.linked.data.mapper.resource.monograph.common.PlaceMapperUnit;
import org.folio.linked.data.mapper.resource.monograph.instance.sub.InstanceSubResourceMapperUnit;
import org.folio.linked.data.model.entity.Resource;

@RequiredArgsConstructor
public abstract class ProviderEventMapperUnit implements InstanceSubResourceMapperUnit {

  private final CoreMapper coreMapper;
  private final PlaceMapperUnit<ProviderEvent> placeMapper;
  private final BiFunction<ProviderEvent, Instance, Instance> providerEventConsumer;

  @Override
  public Instance toDto(Resource source, Instance destination) {
    var providerEvent = coreMapper.readResourceDoc(source, ProviderEvent.class);
    providerEvent.setId(String.valueOf(source.getResourceHash()));
    coreMapper.addMappedResources(placeMapper, source, PROVIDER_PLACE, providerEvent);
    return providerEventConsumer.apply(providerEvent, destination);
  }

  @Override
  public Resource toEntity(Object dto) {
    var providerEvent = (ProviderEvent) dto;
    Resource resource = new Resource();
    resource.setLabel(getFirstValue(() -> getPossibleLabels(providerEvent)));
    resource.addType(PROVIDER_EVENT);
    resource.setDoc(toDoc(providerEvent));
    coreMapper.mapSubEdges(providerEvent.getProviderPlace(), resource, PROVIDER_PLACE, placeMapper::toEntity);
    resource.setResourceHash(coreMapper.hash(resource));
    return resource;
  }

  private List<String> getPossibleLabels(ProviderEvent providerEvent) {
    List<String> result = new ArrayList<>(providerEvent.getName());
    result.addAll(providerEvent.getSimplePlace());
    result.addAll(providerEvent.getProviderPlace().stream().flatMap(p -> p.getName().stream()).toList());
    return result;
  }

  private JsonNode toDoc(ProviderEvent providerEvent) {
    var map = new HashMap<String, List<String>>();
    map.put(DATE.getValue(), providerEvent.getDate());
    map.put(NAME.getValue(), providerEvent.getName());
    map.put(PROVIDER_DATE.getValue(), providerEvent.getProviderDate());
    map.put(SIMPLE_PLACE.getValue(), providerEvent.getSimplePlace());
    return coreMapper.toJson(map);
  }
}
