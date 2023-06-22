package org.folio.linked.data.mapper.resource;

import static org.folio.linked.data.util.Constants.IS_NOT_BIBFRAME_ROOT;
import static org.folio.linked.data.util.Constants.RESOURCE_TYPE;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.folio.linked.data.exception.NotSupportedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BibframeProfiledMapperResolverImpl implements BibframeProfiledMapperResolver {

  private final Map<String, BibframeProfiledMapper> mappers = new HashMap<>();

  public BibframeProfiledMapperResolverImpl(@Autowired List<BibframeProfiledMapper> mappers) {
    for (var mapper : mappers) {
      var resourceMapper = mapper.getClass().getAnnotation(ResourceMapper.class);
      this.mappers.put(resourceMapper.value(), mapper);
    }
  }

  @Override
  public BibframeProfiledMapper getMapper(String profile) {
    return mappers.computeIfAbsent(profile, k -> {
      throw new NotSupportedException(RESOURCE_TYPE + k + IS_NOT_BIBFRAME_ROOT);
    });
  }

}
