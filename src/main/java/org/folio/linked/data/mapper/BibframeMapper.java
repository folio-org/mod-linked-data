package org.folio.linked.data.mapper;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

import org.folio.linked.data.domain.dto.BibframeRequest;
import org.folio.linked.data.domain.dto.BibframeResponse;
import org.folio.linked.data.domain.dto.BibframeShort;
import org.folio.linked.data.domain.dto.BibframeShortInfoPage;
import org.folio.linked.data.mapper.resource.common.ProfiledMapper;
import org.folio.linked.data.model.ResourceShortInfo;
import org.folio.linked.data.model.entity.Resource;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;

@Mapper(componentModel = SPRING)
public abstract class BibframeMapper {

  @Autowired
  private ProfiledMapper profiledMapper;

  @Mapping(target = "id", source = "resourceHash")
  @Mapping(target = "profile", expression = "java(resourceShortInfo.getType().getSimpleLabel())")
  public abstract BibframeShort map(ResourceShortInfo resourceShortInfo);

  public abstract BibframeShortInfoPage map(Page<BibframeShort> page);

  public Resource map(BibframeRequest dto) {
    Resource resource = profiledMapper.toEntity(dto);
    setEdgesId(resource);
    return resource;
  }

  public BibframeResponse map(Resource resource) {
    return profiledMapper.toDto(resource);
  }

  private void setEdgesId(Resource resource) {
    resource.getOutgoingEdges().forEach(edge -> {
      edge.getId().setSourceHash(edge.getSource().getResourceHash());
      edge.getId().setTargetHash(edge.getTarget().getResourceHash());
      edge.getId().setPredicateHash(edge.getPredicate().getPredicateHash());
      setEdgesId(edge.getTarget());
    });
  }

}
