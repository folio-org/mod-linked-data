package org.folio.linked.data.mapper.dto.common;

import static java.util.Arrays.stream;
import static java.util.Comparator.comparing;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.Optional.ofNullable;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.folio.linked.data.mapper.dto.common.SingleResourceMapperUnit.ResourceMappingContext;
import static org.folio.linked.data.util.Constants.AND;
import static org.folio.linked.data.util.Constants.IS_NOT_SUPPORTED_FOR;
import static org.folio.linked.data.util.Constants.PREDICATE;
import static org.folio.linked.data.util.Constants.RIGHT_SQUARE_BRACKET;
import static org.folio.linked.data.util.ResourceUtils.getTypeUris;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Optional;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.folio.ld.dictionary.model.Predicate;
import org.folio.linked.data.exception.NotSupportedException;
import org.folio.linked.data.exception.RequestProcessingException;
import org.folio.linked.data.exception.RequestProcessingExceptionBuilder;
import org.folio.linked.data.model.entity.Resource;
import org.springframework.stereotype.Service;

@Log4j2
@Service
@RequiredArgsConstructor
public class SingleResourceMapperImpl implements SingleResourceMapper {

  private final ObjectMapper objectMapper;
  private final List<SingleResourceMapperUnit> mapperUnits;
  private final RequestProcessingExceptionBuilder exceptionBuilder;

  @SneakyThrows
  @Override
  public <P> Resource toEntity(@NonNull Object dto, @NonNull Class<P> parentRequestDto, Predicate predicate,
                               Resource parentEntity) {
    try {
      return getMapperUnit(null, predicate, parentRequestDto, dto.getClass())
        .map(mapper -> mapper.toEntity(dto, parentEntity))
        .orElseThrow(() -> new NotSupportedException("Dto [" + dto.getClass().getSimpleName() + IS_NOT_SUPPORTED_FOR
          + (nonNull(predicate) ? PREDICATE + predicate.getUri() + RIGHT_SQUARE_BRACKET + AND : EMPTY)
          + "parentDto [" + parentRequestDto.getSimpleName() + RIGHT_SQUARE_BRACKET)
        );
    } catch (RequestProcessingException rpe) {
      throw rpe;
    } catch (Exception e) {
      log.warn("Exception during toEntity mapping", e);
      throw exceptionBuilder.mappingException(dto.getClass().getSimpleName()
        + ofNullable(predicate).map(p -> " under Predicate: " + p.getUri()).orElse(""),
        objectMapper.writeValueAsString(dto));
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
      .map(mapper -> mapper.toDto(source, parentDto, new ResourceMappingContext(parentResource, predicate)))
      .orElseGet(() -> {
        var types = String.join(", ", getTypeUris(source));
        log.debug(
          "Resource with types [" + types + IS_NOT_SUPPORTED_FOR
            + (nonNull(predicate) ? PREDICATE + predicate.getUri() + RIGHT_SQUARE_BRACKET + AND : EMPTY)
            + "parent [" + parentDto.getClass().getSimpleName() + ".class]");
        return null;
      });
  }

  private Optional<SingleResourceMapperUnit> getMapperUnit(String typeUri, Predicate pred, Class<?> parentResponseDto,
                                                          Class<?> requestDto) {
    return mapperUnits.stream()
      .filter(m -> isNull(parentResponseDto) || m.supportedParents().contains(parentResponseDto))
      .filter(m -> {
        var annotation = m.getClass().getAnnotation(MapperUnit.class);
        var typeMatches = isNull(typeUri) || typeUri.equals(annotation.type().getUri());
        var predicateMatches = isNull(pred) || stream(annotation.predicate())
          .anyMatch(dict -> pred.getHash().equals(dict.getHash()));
        var requestDtoMatches = isNull(requestDto) || requestDto.equals(annotation.requestDto());
        return typeMatches && predicateMatches && requestDtoMatches;
      })
      .min(comparing(o -> o.getClass().getSimpleName()));
  }

}
