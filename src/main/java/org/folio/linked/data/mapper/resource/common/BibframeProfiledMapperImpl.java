package org.folio.linked.data.mapper.resource.common;

import static org.folio.linked.data.util.Constants.IS_NOT_BIBFRAME_ROOT;
import static org.folio.linked.data.util.Constants.RESOURCE_TYPE;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.folio.linked.data.domain.dto.BibframeCreateRequest;
import org.folio.linked.data.domain.dto.BibframeResponse;
import org.folio.linked.data.exception.NotSupportedException;
import org.folio.linked.data.model.entity.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BibframeProfiledMapperImpl implements BibframeProfiledMapper {

  private final Map<String, BibframeProfiledMapperUnit> mappers = new HashMap<>();

  public BibframeProfiledMapperImpl(@Autowired List<BibframeProfiledMapperUnit> mappers) {
    mappers.forEach(mapper -> {
      var resourceMapper = mapper.getClass().getAnnotation(ResourceMapper.class);
      this.mappers.put(resourceMapper.type(), mapper);
    });
  }

  @Override
  public Resource toResource(BibframeCreateRequest dto) {
    return getMapper(dto.getProfile()).toResource(dto);
  }

  @Override
  public BibframeResponse toResponseDto(Resource resource) {
    return getMapper(resource.getType().getSimpleLabel()).toResponseDto(resource);
  }

  private BibframeProfiledMapperUnit getMapper(String profile) {
    return mappers.computeIfAbsent(profile, k -> {
      throw new NotSupportedException(RESOURCE_TYPE + k + IS_NOT_BIBFRAME_ROOT);
    });
  }

}
