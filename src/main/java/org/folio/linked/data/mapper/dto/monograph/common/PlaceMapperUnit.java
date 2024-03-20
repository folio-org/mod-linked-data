package org.folio.linked.data.mapper.dto.monograph.common;

import static org.folio.ld.dictionary.PredicateDictionary.PROVIDER_PLACE;
import static org.folio.ld.dictionary.PropertyDictionary.CODE;
import static org.folio.ld.dictionary.PropertyDictionary.LABEL;
import static org.folio.ld.dictionary.PropertyDictionary.LINK;
import static org.folio.ld.dictionary.ResourceTypeDictionary.PLACE;
import static org.folio.linked.data.util.BibframeUtils.getFirstValue;
import static org.folio.linked.data.util.BibframeUtils.putProperty;
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
import org.folio.linked.data.mapper.dto.common.CoreMapper;
import org.folio.linked.data.mapper.dto.common.MapperUnit;
import org.folio.linked.data.mapper.dto.common.SingleResourceMapperUnit;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.service.HashService;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@MapperUnit(type = PLACE, predicate = PROVIDER_PLACE, dtoClass = Place.class)
public class PlaceMapperUnit implements SingleResourceMapperUnit {

  private static final Set<Class<?>> SUPPORTED_PARENTS = Set.of(ProviderEvent.class);
  private final CoreMapper coreMapper;
  private final HashService hashService;

  @Override
  public <P> P toDto(Resource source, P parentDto, Resource parentResource) {
    if (parentDto instanceof ProviderEvent providerEvent) {
      var place = coreMapper.toDtoWithEdges(source, Place.class, false);
      place.setId(String.valueOf(source.getResourceHash()));
      providerEvent.addProviderPlaceItem(place);
    } else {
      throw new NotSupportedException(RESOURCE_TYPE + parentDto.getClass().getSimpleName()
        + IS_NOT_SUPPORTED_FOR_PREDICATE + PROVIDER_PLACE.getUri() + RIGHT_SQUARE_BRACKET);
    }
    return parentDto;
  }

  @Override
  public Set<Class<?>> supportedParents() {
    return SUPPORTED_PARENTS;
  }

  @Override
  public Resource toEntity(Object dto, Resource parentEntity) {
    var place = (Place) dto;
    var resource = new Resource(true);
    resource.setLabel(getFirstValue(place::getLabel));
    resource.addType(PLACE);
    resource.setDoc(getDoc(place));
    resource.setResourceHash(hashService.hash(resource));
    return resource;
  }

  private JsonNode getDoc(Place dto) {
    var map = new HashMap<String, List<String>>();
    putProperty(map, CODE, dto.getCode());
    putProperty(map, LABEL, dto.getLabel());
    putProperty(map, LINK, dto.getLink());
    return map.isEmpty() ? null : coreMapper.toJson(map);
  }

}
