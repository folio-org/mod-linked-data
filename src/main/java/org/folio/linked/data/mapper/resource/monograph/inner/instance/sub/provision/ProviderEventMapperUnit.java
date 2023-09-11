package org.folio.linked.data.mapper.resource.monograph.inner.instance.sub.provision;

import static com.google.common.collect.Iterables.getFirst;
import static org.folio.linked.data.util.BibframeConstants.DATE;
import static org.folio.linked.data.util.BibframeConstants.NAME;
import static org.folio.linked.data.util.BibframeConstants.PLACE;
import static org.folio.linked.data.util.BibframeConstants.PLACE_PRED;
import static org.folio.linked.data.util.BibframeConstants.PROVIDER_EVENT;
import static org.folio.linked.data.util.BibframeConstants.SIMPLE_DATE;
import static org.folio.linked.data.util.BibframeConstants.SIMPLE_PLACE;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.HashMap;
import java.util.List;
import java.util.function.BiFunction;
import lombok.RequiredArgsConstructor;
import org.folio.linked.data.domain.dto.Instance;
import org.folio.linked.data.domain.dto.Place;
import org.folio.linked.data.domain.dto.ProviderEvent;
import org.folio.linked.data.mapper.resource.common.CoreMapper;
import org.folio.linked.data.mapper.resource.common.inner.sub.SubResourceMapper;
import org.folio.linked.data.mapper.resource.monograph.inner.common.PlaceMapperUnit;
import org.folio.linked.data.mapper.resource.monograph.inner.instance.sub.InstanceSubResourceMapperUnit;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.service.dictionary.ResourceTypeService;

@RequiredArgsConstructor
public abstract class ProviderEventMapperUnit implements InstanceSubResourceMapperUnit {

  private final CoreMapper coreMapper;
  private final PlaceMapperUnit<ProviderEvent> placeMapper;
  private final ResourceTypeService resourceTypeService;
  private final BiFunction<ProviderEvent, Instance, Instance> providerEventConsumer;

  @Override
  public Instance toDto(Resource source, Instance destination) {
    var providerEvent = coreMapper.readResourceDoc(source, ProviderEvent.class);
    coreMapper.addMappedResources(placeMapper, source, PLACE_PRED, providerEvent);
    return providerEventConsumer.apply(providerEvent, destination);
  }

  @Override
  public Resource toEntity(Object dto, String predicate, SubResourceMapper subResourceMapper) {
    var production = (ProviderEvent) dto;
    Resource resource = new Resource();
    resource.setLabel(getFirst(production.getSimplePlace(), getPlaceName(production)));
    resource.addType(resourceTypeService.get(PROVIDER_EVENT));
    resource.setDoc(toDoc(production));
    coreMapper.mapResourceEdges(production.getPlace(), resource, PLACE, PLACE_PRED,
      (place, pred) -> placeMapper.toEntity(place, pred, null));
    resource.setResourceHash(coreMapper.hash(resource));
    return resource;
  }

  private String getPlaceName(ProviderEvent providerEvent) {
    return getFirst(getFirst(providerEvent.getPlace(), new Place()).getName(), "");
  }

  private JsonNode toDoc(ProviderEvent providerEvent) {
    var map = new HashMap<String, List<String>>();
    map.put(DATE, providerEvent.getDate());
    map.put(NAME, providerEvent.getName());
    map.put(SIMPLE_DATE, providerEvent.getSimpleDate());
    map.put(SIMPLE_PLACE, providerEvent.getSimplePlace());
    return coreMapper.toJson(map);
  }
}
