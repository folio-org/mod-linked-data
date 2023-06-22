package org.folio.linked.data.mapper.resource.inner.sub;

import static java.util.Objects.nonNull;
import static org.folio.linked.data.util.Constants.IS_NOT_SUPPORTED;
import static org.folio.linked.data.util.Constants.IS_NOT_SUPPORTED_FOR;
import static org.folio.linked.data.util.Constants.RESOURCE_TYPE;
import static org.folio.linked.data.util.Constants.RIGHT_SQUARE_BRACKET;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.folio.linked.data.exception.NotSupportedException;
import org.folio.linked.data.mapper.resource.ResourceMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SubResourceMapperResolverImpl implements SubResourceMapperResolver {

  private final Map<String, Map<Class, SubResourceMapper>> mappers = new HashMap<>();

  @Autowired
  public SubResourceMapperResolverImpl(List<SubResourceMapper> mappers) {
    mappers.stream()
      .filter(m -> nonNull(m.getClass().getAnnotation(ResourceMapper.class)))
      .forEach(mapper -> {
        var annotation = mapper.getClass().getAnnotation(ResourceMapper.class);
        this.mappers.computeIfAbsent(annotation.value(), k -> new HashMap<>())
          .put(mapper.destinationDtoClass(), mapper);
      });
  }

  @Override
  public <T> SubResourceMapper<T> getMapper(String type, Class<T> destinationDtoClass, boolean strict) {
    return mappers
      .computeIfAbsent(type, t -> emptyMapOrStrictException(t, strict))
      .computeIfAbsent(destinationDtoClass, c -> nullOrStrictException(type, c, strict));
  }

  private Map<Class, SubResourceMapper> emptyMapOrStrictException(String type, boolean strict) {
    if (strict) {
      throw new NotSupportedException(RESOURCE_TYPE + type + IS_NOT_SUPPORTED);
    } else {
      return new HashMap<>();
    }
  }

  private <T> SubResourceMapper<T> nullOrStrictException(String type, Class<T> clazz, boolean strict) {
    if (strict) {
      throw new NotSupportedException(RESOURCE_TYPE + type + IS_NOT_SUPPORTED_FOR + clazz.getSimpleName()
        + RIGHT_SQUARE_BRACKET);
    } else {
      return null;
    }
  }
}
