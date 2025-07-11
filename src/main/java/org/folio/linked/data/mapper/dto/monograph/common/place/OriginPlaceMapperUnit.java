package org.folio.linked.data.mapper.dto.monograph.common.place;

import static org.folio.ld.dictionary.PredicateDictionary.ORIGIN_PLACE;
import static org.folio.ld.dictionary.ResourceTypeDictionary.PLACE;
import static org.folio.linked.data.util.Constants.IS_NOT_SUPPORTED_FOR_PREDICATE;
import static org.folio.linked.data.util.Constants.RESOURCE_TYPE;
import static org.folio.linked.data.util.Constants.RIGHT_SQUARE_BRACKET;
import static org.folio.linked.data.util.ResourceUtils.getFirstValue;

import org.folio.linked.data.domain.dto.Place;
import org.folio.linked.data.domain.dto.PlaceResponse;
import org.folio.linked.data.domain.dto.WorkResponse;
import org.folio.linked.data.exception.NotSupportedException;
import org.folio.linked.data.mapper.dto.common.CoreMapper;
import org.folio.linked.data.mapper.dto.common.MapperUnit;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.service.resource.hash.HashService;
import org.springframework.stereotype.Component;

@Component
@MapperUnit(type = PLACE, predicate = ORIGIN_PLACE, requestDto = Place.class)
public class OriginPlaceMapperUnit extends PlaceMapperUnit {

  public OriginPlaceMapperUnit(CoreMapper coreMapper, HashService hashService) {
    super(coreMapper, hashService);
  }

  @Override
  public <P> P toDto(Resource source, P parentDto, Resource parentResource) {
    if (parentDto instanceof WorkResponse work) {
      work.addOriginPlaceItem(getPlace(source));
    } else {
      throw new NotSupportedException(RESOURCE_TYPE + parentDto.getClass().getSimpleName()
        + IS_NOT_SUPPORTED_FOR_PREDICATE + ORIGIN_PLACE.getUri() + RIGHT_SQUARE_BRACKET);
    }
    return parentDto;
  }

  @Override
  protected String getLabel(Place place) {
    return getFirstValue(place::getName);
  }

  private PlaceResponse getPlace(Resource source) {
    var place = coreMapper.toDtoWithEdges(source, PlaceResponse.class);
    place.setId(String.valueOf(source.getId()));
    return place;
  }
}
