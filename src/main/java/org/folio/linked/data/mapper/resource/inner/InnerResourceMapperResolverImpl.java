package org.folio.linked.data.mapper.resource.inner;

import static org.folio.linked.data.util.Constants.IS_NOT_SUPPORTED;
import static org.folio.linked.data.util.Constants.RESOURCE_TYPE;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.folio.linked.data.exception.NotSupportedException;
import org.folio.linked.data.mapper.resource.ResourceMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class InnerResourceMapperResolverImpl implements InnerResourceMapperResolver {
  private final Map<String, InnerResourceMapper> mappers = new HashMap<>();

  @Autowired
  public InnerResourceMapperResolverImpl(List<InnerResourceMapper> mappers) {
    for (var mapper : mappers) {
      var resourceMapper = mapper.getClass().getAnnotation(ResourceMapper.class);
      this.mappers.put(resourceMapper.value(), mapper);
    }
  }

  @Override
  public InnerResourceMapper getMapper(String type) {
    return mappers.computeIfAbsent(type, k -> {
      throw new NotSupportedException(RESOURCE_TYPE + k + IS_NOT_SUPPORTED);
    });
  }
}
