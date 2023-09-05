package org.folio.linked.data.mapper.resource.common.inner;

import static org.folio.linked.data.util.Constants.IS_NOT_SUPPORTED;
import static org.folio.linked.data.util.Constants.RESOURCE_TYPE;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.folio.linked.data.domain.dto.Bibframe2Response;
import org.folio.linked.data.domain.dto.BibframeResponse;
import org.folio.linked.data.exception.BaseLinkedDataException;
import org.folio.linked.data.exception.NotSupportedException;
import org.folio.linked.data.exception.ValidationException;
import org.folio.linked.data.mapper.resource.common.MapperUnit;
import org.folio.linked.data.model.entity.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Log4j2
@Service
public class InnerResourceMapperImpl implements InnerResourceMapper {

  private final ObjectMapper objectMapper;
  private final Map<String, InnerResourceMapperUnit> mapperUnits = new HashMap<>();

  @Autowired
  public InnerResourceMapperImpl(List<InnerResourceMapperUnit> mapperUnits, ObjectMapper objectMapper) {
    mapperUnits.forEach(mapperUnit -> {
      var annotation = mapperUnit.getClass().getAnnotation(MapperUnit.class);
      this.mapperUnits.put(annotation.type(), mapperUnit);
    });
    this.objectMapper = objectMapper;
  }

  @Override
  public BibframeResponse toDto(@NonNull Resource source, @NonNull BibframeResponse destination) {
    return getMapperUnit(source.getType().getTypeUri()).toDto(source, destination);
  }

  @Override
  public Bibframe2Response toDto(@NonNull Resource source, @NonNull Bibframe2Response destination) {
    return getMapperUnit(source.getType().getTypeUri()).toDto(source, destination);
  }

  @SneakyThrows
  @Override
  public Resource toEntity(@NonNull Object dto, @NonNull String resourceType) {
    try {
      return getMapperUnit(resourceType).toEntity(dto);
    } catch (BaseLinkedDataException blde) {
      throw blde;
    } catch (Exception e) {
      log.warn("Exception during toEntity mapping", e);
      throw new ValidationException(dto.getClass().getSimpleName(), objectMapper.writeValueAsString(dto));
    }
  }

  private InnerResourceMapperUnit getMapperUnit(String type) {
    return mapperUnits.computeIfAbsent(type, k -> {
      throw new NotSupportedException(RESOURCE_TYPE + k + IS_NOT_SUPPORTED);
    });
  }
}
