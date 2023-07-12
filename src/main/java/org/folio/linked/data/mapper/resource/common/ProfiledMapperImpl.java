package org.folio.linked.data.mapper.resource.common;

import static org.folio.linked.data.util.Constants.IS_NOT_BIBFRAME_ROOT;
import static org.folio.linked.data.util.Constants.RESOURCE_TYPE;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.folio.linked.data.domain.dto.BibframeRequest;
import org.folio.linked.data.domain.dto.BibframeResponse;
import org.folio.linked.data.exception.NotSupportedException;
import org.folio.linked.data.model.entity.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ProfiledMapperImpl implements ProfiledMapper {

  private final Map<String, ProfiledMapperUnit> mappers = new HashMap<>();

  public ProfiledMapperImpl(@Autowired List<ProfiledMapperUnit> mapperUnits) {
    mapperUnits.forEach(mapperUnit -> {
      var annotation = mapperUnit.getClass().getAnnotation(MapperUnit.class);
      this.mappers.put(annotation.type(), mapperUnit);
    });
  }

  @Override
  public Resource toEntity(BibframeRequest dto) {
    return getMapperUnit(dto.getProfile()).toEntity(dto);
  }

  @Override
  public BibframeResponse toDto(Resource resource) {
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
