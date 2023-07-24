package org.folio.linked.data.mapper.resource.common.inner.sub;

import static java.util.Objects.isNull;
import static org.folio.linked.data.util.Constants.AND;
import static org.folio.linked.data.util.Constants.IS_NOT_SUPPORTED_FOR_PREDICATE;
import static org.folio.linked.data.util.Constants.RESOURCE_TYPE;
import static org.folio.linked.data.util.Constants.RIGHT_SQUARE_BRACKET;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Optional;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.folio.linked.data.exception.BaseLinkedDataException;
import org.folio.linked.data.exception.NotSupportedException;
import org.folio.linked.data.exception.ValidationException;
import org.folio.linked.data.mapper.resource.common.MapperUnit;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceEdge;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SubResourceMapperImpl implements SubResourceMapper {

  private final ObjectMapper objectMapper;
  private final List<SubResourceMapperUnit<?>> mapperUnits;

  @SneakyThrows
  @Override
  public <P> Resource toEntity(@NonNull Object dto, @NonNull String predicate, @NonNull Class<P> parentDtoClass) {
    try {
      return getMapperUnit(null, predicate, parentDtoClass, dto.getClass())
        .map(mapper -> mapper.toEntity(dto, predicate, this))
        .orElseThrow(() -> new NotSupportedException(RESOURCE_TYPE + dto.getClass().getSimpleName()
          + IS_NOT_SUPPORTED_FOR_PREDICATE + predicate + RIGHT_SQUARE_BRACKET)
        );
    } catch (BaseLinkedDataException blde) {
      throw blde;
    } catch (Exception e) {
      throw new ValidationException(predicate, objectMapper.writeValueAsString(dto));
    }
  }

  @Override
  @SuppressWarnings("java:S2201")
  public <T> void toDto(@NonNull ResourceEdge source, @NonNull T destination) {
    getMapperUnit(source.getTarget().getType().getSimpleLabel(), source.getPredicate().getLabel(),
      destination.getClass(), null)
      .map(mapper -> ((SubResourceMapperUnit<T>) mapper).toDto(source.getTarget(), destination))
      .orElseThrow(() -> new NotSupportedException(RESOURCE_TYPE + source.getTarget().getType().getSimpleLabel()
        + IS_NOT_SUPPORTED_FOR_PREDICATE + source.getPredicate().getLabel() + RIGHT_SQUARE_BRACKET + AND
        + destination.getClass().getSimpleName()));
  }

  private Optional<SubResourceMapperUnit<?>> getMapperUnit(String type, String pred, Class<?> parentDto, Class<?> dto) {
    return mapperUnits.stream()
      .filter(m -> isNull(parentDto) || m.getParentDto().contains(parentDto))
      .filter(m -> {
        var annotation = m.getClass().getAnnotation(MapperUnit.class);
        return (isNull(type) || type.equals(annotation.type()))
          && (isNull(pred) || pred.equals(annotation.predicate()))
          && (isNull(dto) || dto.equals(annotation.dtoClass()));
      })
      .findFirst();
  }

}
