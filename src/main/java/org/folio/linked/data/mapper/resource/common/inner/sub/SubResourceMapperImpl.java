package org.folio.linked.data.mapper.resource.common.inner.sub;

import static java.util.Objects.isNull;
import static org.folio.linked.data.util.Constants.AND;
import static org.folio.linked.data.util.Constants.IS_NOT_SUPPORTED_FOR;
import static org.folio.linked.data.util.Constants.PREDICATE;
import static org.folio.linked.data.util.Constants.RESOURCE_TYPE;
import static org.folio.linked.data.util.Constants.RIGHT_SQUARE_BRACKET;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.folio.linked.data.exception.NotSupportedException;
import org.folio.linked.data.mapper.resource.common.ResourceMapper;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceEdge;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SubResourceMapperImpl implements SubResourceMapper {

  private final List<SubResourceMapperUnit<?>> mappers;

  @Override
  public Resource toEntity(Object dto, String predicate) {
    return getMapper(null, predicate, null, dto.getClass())
      .map(mapper -> mapper.toEntity(dto, predicate))
      .orElseThrow(() -> new NotSupportedException(RESOURCE_TYPE + dto.getClass().getSimpleName()
        + IS_NOT_SUPPORTED_FOR + PREDICATE + predicate + RIGHT_SQUARE_BRACKET)
      );
  }

  @Override
  public <T> void toDto(ResourceEdge source, T destination) {
    getMapper(source.getTarget().getType().getSimpleLabel(), source.getPredicate().getLabel(),
      destination.getClass(), null)
      .map(mapper -> ((SubResourceMapperUnit<T>) mapper).toDto(source.getTarget(), destination))
      .orElseThrow(() -> new NotSupportedException(RESOURCE_TYPE + source.getTarget().getType().getSimpleLabel()
        + IS_NOT_SUPPORTED_FOR + PREDICATE + source.getPredicate().getLabel() + RIGHT_SQUARE_BRACKET + AND
        + destination.getClass().getSimpleName()));
  }

  private Optional<SubResourceMapperUnit<?>> getMapper(String type, String pred, Class<?> parentDto, Class<?> dto) {
    return mappers.stream()
      .filter(m -> isNull(parentDto) || parentDto.equals(m.getParentDto()))
      .filter(m -> {
        var annotation = m.getClass().getAnnotation(ResourceMapper.class);
        return (isNull(type) || type.equals(annotation.type()))
          && (isNull(pred) || pred.equals(annotation.predicate()))
          && (isNull(dto) || dto.equals(annotation.dtoClass()));
      })
      .findFirst();
  }

}
