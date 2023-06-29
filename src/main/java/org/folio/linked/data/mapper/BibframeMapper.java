package org.folio.linked.data.mapper;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

import org.folio.linked.data.domain.dto.BibframeCreateRequest;
import org.folio.linked.data.domain.dto.BibframeResponse;
import org.folio.linked.data.domain.dto.BibframeShort;
import org.folio.linked.data.domain.dto.BibframeShortInfoPage;
import org.folio.linked.data.mapper.resource.common.BibframeProfiledMapper;
import org.folio.linked.data.model.ResourceHashAndLabel;
import org.folio.linked.data.model.entity.Resource;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;

@Mapper(componentModel = SPRING)
public abstract class BibframeMapper {

  @Autowired
  private BibframeProfiledMapper bibframeProfiledMapper;

  @Mapping(target = "id", source = "resourceHash")
  public abstract BibframeShort map(ResourceHashAndLabel resourceHashAndLabel);

  public abstract BibframeShortInfoPage map(Page<BibframeShort> page);

  public Resource map(BibframeCreateRequest dto) {
    return bibframeProfiledMapper.toResource(dto);
  }

  public BibframeResponse map(Resource resource) {
    return bibframeProfiledMapper.toResponseDto(resource);
  }

}
