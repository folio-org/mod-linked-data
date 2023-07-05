package org.folio.linked.data.mapper.resource.common.inner;

import static org.folio.linked.data.util.Constants.IS_NOT_SUPPORTED;
import static org.folio.linked.data.util.Constants.RESOURCE_TYPE;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.folio.linked.data.domain.dto.BibframeResponse;
import org.folio.linked.data.exception.NotSupportedException;
import org.folio.linked.data.mapper.resource.common.MapperUnit;
import org.folio.linked.data.model.entity.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class InnerResourceMapperImpl implements InnerResourceMapper {

  private final Map<String, InnerResourceMapperUnit> mapperUnits = new HashMap<>();

  @Autowired
  public InnerResourceMapperImpl(List<InnerResourceMapperUnit> mapperUnits) {
    mapperUnits.forEach(mapperUnit -> {
      var annotation = mapperUnit.getClass().getAnnotation(MapperUnit.class);
      this.mapperUnits.put(annotation.type(), mapperUnit);
    });
  }

  @Override
  public BibframeResponse toDto(Resource source, BibframeResponse destination) {
    return getMapperUnit(source.getType().getSimpleLabel()).toDto(source, destination);
  }

  @Override
  public Resource toEntity(Object dto, String resourceType) {
    return getMapperUnit(resourceType).toEntity(dto);
  }

  private InnerResourceMapperUnit getMapperUnit(String type) {
    return mapperUnits.computeIfAbsent(type, k -> {
      throw new NotSupportedException(RESOURCE_TYPE + k + IS_NOT_SUPPORTED);
    });
  }
}
