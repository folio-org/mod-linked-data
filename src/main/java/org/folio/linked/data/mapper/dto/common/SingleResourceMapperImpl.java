package org.folio.linked.data.mapper.dto.common;

import static java.util.Comparator.comparing;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.joining;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.folio.linked.data.util.Constants.AND;
import static org.folio.linked.data.util.Constants.IS_NOT_SUPPORTED_FOR;
import static org.folio.linked.data.util.Constants.PREDICATE;
import static org.folio.linked.data.util.Constants.RIGHT_SQUARE_BRACKET;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Optional;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.folio.ld.dictionary.model.Predicate;
import org.folio.linked.data.exception.BaseLinkedDataException;
import org.folio.linked.data.exception.NotSupportedException;
import org.folio.linked.data.exception.ValidationException;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceTypeEntity;
import org.springframework.stereotype.Service;

@Log4j2
@Service
@RequiredArgsConstructor
public class SingleResourceMapperImpl implements SingleResourceMapper {

  private final ObjectMapper objectMapper;
  private final List<SingleResourceMapperUnit> mapperUnits;

  @SneakyThrows
  @Override
  public <P> Resource toEntity(@NonNull Object dto, @NonNull Class<P> parentDtoClass, Predicate predicate,
                               Resource parentEntity) {
    try {
      return getMapperUnit(null, predicate, parentDtoClass, dto.getClass())
        .map(mapper -> mapper.toEntity(dto, parentEntity))
        .orElseThrow(() -> new NotSupportedException("Dto [" + dto.getClass().getSimpleName() + IS_NOT_SUPPORTED_FOR
          + (nonNull(predicate) ? PREDICATE + predicate.getUri() + RIGHT_SQUARE_BRACKET + AND : EMPTY)
          + "parentDto [" + parentDtoClass.getSimpleName() + RIGHT_SQUARE_BRACKET)
        );
    } catch (BaseLinkedDataException blde) {
      throw blde;
    } catch (Exception e) {
      log.warn("Exception during toEntity mapping", e);
      throw new ValidationException(predicate == null ? "-" : predicate.getUri(), objectMapper.writeValueAsString(dto));
    }
  }

  @Override
  public <D> D toDto(@NonNull Resource source, @NonNull D parentDto, Resource parentResource, Predicate predicate) {
    // Of all the types of the resource, take the first one that has a mapper
    var resourceMapper = source.getTypes()
      .stream()
      .map(type -> getMapperUnit(type.getUri(), predicate, parentDto.getClass(), null))
      .flatMap(Optional::stream)
      .findFirst();

    return resourceMapper
      .map(mapper -> mapper.toDto(source, parentDto, parentResource))
      .orElseGet(() -> {
        var types = source.getTypes().stream().map(ResourceTypeEntity::getUri).collect(joining(", "));
        log.info(
          "Resource with types [" + types + IS_NOT_SUPPORTED_FOR
            + (nonNull(predicate) ? PREDICATE + predicate.getUri() + RIGHT_SQUARE_BRACKET + AND : EMPTY)
            + "parent [" + parentDto.getClass().getSimpleName() + ".class]");
        return null;
      });
  }


  @Override
  public Optional<SingleResourceMapperUnit> getMapperUnit(String typeUri, Predicate pred, Class<?> parentDto,
                                                          Class<?> dtoClass) {
    return mapperUnits.stream()
      .filter(m -> isNull(parentDto) || m.supportedParents().contains(parentDto))
      .filter(m -> {
        var annotation = m.getClass().getAnnotation(MapperUnit.class);
        return (isNull(typeUri) || typeUri.equals(annotation.type().getUri()))
          && (isNull(pred) || pred.getHash().equals(annotation.predicate().getHash()))
          && (isNull(dtoClass) || dtoClass.equals(annotation.dtoClass()));
      })
      .min(comparing(o -> o.getClass().getSimpleName()));
  }

}
