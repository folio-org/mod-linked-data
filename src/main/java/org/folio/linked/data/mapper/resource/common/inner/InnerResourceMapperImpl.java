package org.folio.linked.data.mapper.resource.common.inner;

import static org.folio.linked.data.util.Constants.IS_NOT_SUPPORTED;
import static org.folio.linked.data.util.Constants.RESOURCE_TYPE;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.folio.linked.data.domain.dto.BibframeResponse;
import org.folio.linked.data.exception.NotSupportedException;
import org.folio.linked.data.mapper.resource.common.ResourceMapper;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceEdge;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class InnerResourceMapperImpl implements InnerResourceMapper {

  private final Map<String, InnerResourceMapperUnit> mappers = new HashMap<>();

  @Autowired
  public InnerResourceMapperImpl(List<InnerResourceMapperUnit> mappers) {
    mappers.forEach(mapper -> {
      var resourceMapper = mapper.getClass().getAnnotation(ResourceMapper.class);
      this.mappers.put(resourceMapper.type(), mapper);
    });
  }

  @Override
  public BibframeResponse toDto(Resource source, BibframeResponse destination) {
    return getMapper(source.getType().getSimpleLabel()).toDto(source, destination);
  }

  @Override
  public ResourceEdge toEntity(Object innerResourceDto, String innerResourceType, Resource destination) {
    return getMapper(innerResourceType).toEntity(innerResourceDto, destination);
  }

  private InnerResourceMapperUnit getMapper(String type) {
    return mappers.computeIfAbsent(type, k -> {
      throw new NotSupportedException(RESOURCE_TYPE + k + IS_NOT_SUPPORTED);
    });
  }
}
