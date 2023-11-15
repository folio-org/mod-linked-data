package org.folio.linked.data.mapper.resource.common.sub;

import static java.util.Objects.isNull;
import static org.folio.linked.data.util.Constants.AND;
import static org.folio.linked.data.util.Constants.IS_NOT_SUPPORTED_FOR_PREDICATE;
import static org.folio.linked.data.util.Constants.RESOURCE_TYPE;
import static org.folio.linked.data.util.Constants.RESOURCE_WITH_GIVEN_ID;
import static org.folio.linked.data.util.Constants.RIGHT_SQUARE_BRACKET;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Optional;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.folio.ld.dictionary.api.Predicate;
import org.folio.linked.data.exception.BaseLinkedDataException;
import org.folio.linked.data.exception.NotSupportedException;
import org.folio.linked.data.exception.ValidationException;
import org.folio.linked.data.mapper.resource.common.MapperUnit;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceEdge;
import org.springframework.stereotype.Service;

@Log4j2
@Service
@RequiredArgsConstructor
public class SubResourceMapperImpl implements SubResourceMapper {

  private final ObjectMapper objectMapper;
  private final List<SubResourceMapperUnit<?>> mapperUnits;

  @SneakyThrows
  @Override
  public <P> Resource toEntity(@NonNull Object dto, Predicate predicate, @NonNull Class<P> parentDtoClass) {
    try {
      return getMapperUnit(null, predicate, parentDtoClass, dto.getClass())
        .map(mapper -> mapper.toEntity(dto))
        .orElseThrow(() -> new NotSupportedException(RESOURCE_TYPE + dto.getClass().getSimpleName()
          + IS_NOT_SUPPORTED_FOR_PREDICATE + predicate.getUri() + RIGHT_SQUARE_BRACKET)
        );
    } catch (BaseLinkedDataException blde) {
      throw blde;
    } catch (Exception e) {
      log.warn("Exception during toEntity mapping", e);
      throw new ValidationException(predicate.getUri(), objectMapper.writeValueAsString(dto));
    }
  }

  @Override
  @SuppressWarnings("java:S2201")
  public <T> void toDto(@NonNull ResourceEdge source, @NonNull T destination) {
    // Of all the types of the resource, take the first one that has a mapper
    var resourceMapper = source.getTarget().getTypes()
      .stream()
      .map(type -> getMapperUnit(type.getUri(), source.getPredicate(), destination.getClass(), null))
      .flatMap(Optional::stream)
      .findFirst();

    resourceMapper
      .map(mapper -> ((SubResourceMapperUnit<T>) mapper).toDto(source.getTarget(), destination))
      .orElseGet(() -> {
        log.warn(RESOURCE_WITH_GIVEN_ID + source.getTarget().getResourceHash() + RIGHT_SQUARE_BRACKET
          + IS_NOT_SUPPORTED_FOR_PREDICATE + source.getPredicate().getUri()
          + RIGHT_SQUARE_BRACKET + AND + destination.getClass().getSimpleName());
        return null;
      });
  }


  @Override
  public Optional<SubResourceMapperUnit<?>> getMapperUnit(String typeUri, Predicate pred, Class<?> parentDto,
                                                          Class<?> dto) {
    return mapperUnits.stream()
      .filter(m -> isNull(parentDto) || m.getParentDto().contains(parentDto))
      .filter(m -> {
        var annotation = m.getClass().getAnnotation(MapperUnit.class);
        return (isNull(typeUri) || typeUri.equals(annotation.type().getUri()))
          && (isNull(pred) || pred.getHash().equals(annotation.predicate().getHash()))
          && (isNull(dto) || dto.equals(annotation.dtoClass()));
      })
      .findFirst();
  }

}
