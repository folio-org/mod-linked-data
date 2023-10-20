package org.folio.linked.data.mapper.resource.monograph.inner.common;

import static org.folio.ld.dictionary.PredicateDictionary.PROVIDER_PLACE;
import static org.folio.ld.dictionary.Property.LINK;
import static org.folio.ld.dictionary.Property.NAME;
import static org.folio.ld.dictionary.ResourceTypeDictionary.PLACE;
import static org.folio.linked.data.util.BibframeUtils.getFirstValue;
import static org.folio.linked.data.util.Constants.IS_NOT_SUPPORTED_FOR_PREDICATE;
import static org.folio.linked.data.util.Constants.RESOURCE_TYPE;
import static org.folio.linked.data.util.Constants.RIGHT_SQUARE_BRACKET;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.folio.linked.data.domain.dto.Place;
import org.folio.linked.data.domain.dto.ProviderEvent;
import org.folio.linked.data.exception.NotSupportedException;
import org.folio.linked.data.mapper.resource.common.CoreMapper;
import org.folio.linked.data.mapper.resource.common.MapperUnit;
import org.folio.linked.data.mapper.resource.common.inner.sub.SubResourceMapperUnit;
import org.folio.linked.data.model.entity.Resource;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@MapperUnit(type = PLACE, predicate = PROVIDER_PLACE, dtoClass = Place.class)
public class PlaceMapperUnit<T> implements SubResourceMapperUnit<T> {

  private static final Set<Class> SUPPORTED_PARENTS = Set.of(ProviderEvent.class);
  private final CoreMapper coreMapper;

  @Override
  public T toDto(Resource source, T destination) {
    var place = coreMapper.readResourceDoc(source, Place.class);
    place.setId(String.valueOf(source.getResourceHash()));
    if (destination instanceof ProviderEvent providerEvent) {
      providerEvent.addProviderPlaceItem(place);
    } else {
      throw new NotSupportedException(RESOURCE_TYPE + destination.getClass().getSimpleName()
        + IS_NOT_SUPPORTED_FOR_PREDICATE + PROVIDER_PLACE.getUri() + RIGHT_SQUARE_BRACKET);
    }
    return destination;
  }

  @Override
  public Set<Class> getParentDto() {
    return SUPPORTED_PARENTS;
  }

  @Override
  public Resource toEntity(Object dto) {
    var place = (Place) dto;
    var resource = new Resource();
    resource.setLabel(getFirstValue(place::getName));
    resource.addType(PLACE);
    resource.setDoc(getDoc(place));
    resource.setResourceHash(coreMapper.hash(resource));
    return resource;
  }

  private JsonNode getDoc(Place place) {
    var map = new HashMap<String, List<String>>();
    map.put(NAME, place.getName());
    map.put(LINK, place.getLink());
    return coreMapper.toJson(map);
  }

}
