package org.folio.linked.data.mapper.resource.common;

import static org.folio.linked.data.util.Constants.IS_NOT_BIBFRAME_ROOT;
import static org.folio.linked.data.util.Constants.RESOURCE_TYPE;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.NonNull;
import lombok.SneakyThrows;
import org.folio.linked.data.domain.dto.BibframeRequest;
import org.folio.linked.data.domain.dto.BibframeResponse;
import org.folio.linked.data.exception.BaseLinkedDataException;
import org.folio.linked.data.exception.NotSupportedException;
import org.folio.linked.data.exception.ValidationException;
import org.folio.linked.data.model.entity.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ProfiledMapperImpl implements ProfiledMapper {

  private final ObjectMapper objectMapper;
  private final Map<String, ProfiledMapperUnit> mappers = new HashMap<>();

  public ProfiledMapperImpl(@Autowired List<ProfiledMapperUnit> mapperUnits, ObjectMapper objectMapper) {
    mapperUnits.forEach(mapperUnit -> {
      var annotation = mapperUnit.getClass().getAnnotation(MapperUnit.class);
      this.mappers.put(annotation.type(), mapperUnit);
    });
    this.objectMapper = objectMapper;
  }

  @SneakyThrows
  @Override
  public Resource toEntity(@NonNull BibframeRequest dto) {
    try {
      return getMapperUnit(dto.getProfile()).toEntity(dto);
    } catch (BaseLinkedDataException blde) {
      throw blde;
    } catch (Exception e) {
      throw new ValidationException(dto.getClass().getSimpleName(), objectMapper.writeValueAsString(dto));
    }
  }

  @Override
  public BibframeResponse toDto(@NonNull Resource resource) {
    var type = resource.getType().getSimpleLabel();
    var response = getMapperUnit(type).toDto(resource);
    response.setProfile(type);
    return response;
  }

  private ProfiledMapperUnit getMapperUnit(String profile) {
    return mappers.computeIfAbsent(profile, k -> {
      throw new NotSupportedException(RESOURCE_TYPE + k + IS_NOT_BIBFRAME_ROOT);
    });
  }

}
