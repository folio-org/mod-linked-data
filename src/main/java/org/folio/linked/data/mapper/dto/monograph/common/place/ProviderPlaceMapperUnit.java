package org.folio.linked.data.mapper.dto.monograph.common.place;

import static org.folio.ld.dictionary.PredicateDictionary.PROVIDER_PLACE;
import static org.folio.ld.dictionary.ResourceTypeDictionary.PLACE;
import static org.folio.linked.data.util.Constants.IS_NOT_SUPPORTED_FOR_PREDICATE;
import static org.folio.linked.data.util.Constants.RESOURCE_TYPE;
import static org.folio.linked.data.util.Constants.RIGHT_SQUARE_BRACKET;
import static org.folio.linked.data.util.ResourceUtils.getFirstValue;

import org.folio.linked.data.domain.dto.Place;
import org.folio.linked.data.domain.dto.PlaceResponse;
import org.folio.linked.data.domain.dto.ProviderEventResponse;
import org.folio.linked.data.exception.NotSupportedException;
import org.folio.linked.data.mapper.dto.common.CoreMapper;
import org.folio.linked.data.mapper.dto.common.MapperUnit;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.service.resource.hash.HashService;
import org.springframework.stereotype.Component;

@Component
@MapperUnit(type = PLACE, predicate = PROVIDER_PLACE, requestDto = Place.class)
public class ProviderPlaceMapperUnit extends PlaceMapperUnit {

  public ProviderPlaceMapperUnit(CoreMapper coreMapper, HashService hashService) {
    super(coreMapper, hashService);
  }

  @Override
  public <P> P toDto(Resource resourceToConvert, P parentDto, ResourceMappingContext context) {
    if (parentDto instanceof ProviderEventResponse providerEvent) {
      var place = coreMapper.toDtoWithEdges(resourceToConvert, PlaceResponse.class, false);
      place.setId(String.valueOf(resourceToConvert.getId()));
      providerEvent.addProviderPlaceItem(place);
    } else {
      throw new NotSupportedException(RESOURCE_TYPE + parentDto.getClass().getSimpleName()
        + IS_NOT_SUPPORTED_FOR_PREDICATE + PROVIDER_PLACE.getUri() + RIGHT_SQUARE_BRACKET);
    }
    return parentDto;
  }

  @Override
  protected String getLabel(Place place) {
    return getFirstValue(place::getLabel);
  }
}
