package org.folio.linked.data.mapper.dto.resource.common.authority;

import lombok.RequiredArgsConstructor;
import org.folio.linked.data.domain.dto.AuthorityField;
import org.folio.linked.data.domain.dto.AuthorityRequest;
import org.folio.linked.data.domain.dto.AuthorityResponse;
import org.folio.linked.data.domain.dto.AuthorityResponseField;
import org.folio.linked.data.domain.dto.ResourceResponseDto;
import org.folio.linked.data.mapper.dto.resource.base.CoreMapper;
import org.folio.linked.data.mapper.dto.resource.base.MapperUnit;
import org.folio.linked.data.mapper.dto.resource.common.TopResourceMapperUnit;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.service.label.ResourceEntityLabelService;
import org.folio.linked.data.service.profile.ResourceProfileLinkingService;
import org.folio.linked.data.service.resource.hash.HashService;
import tools.jackson.databind.JsonNode;

@RequiredArgsConstructor
public abstract class AuthorityMapperUnit extends TopResourceMapperUnit {

  protected final CoreMapper coreMapper;
  private final HashService hashService;
  private final ResourceEntityLabelService labelService;
  private final ResourceProfileLinkingService resourceProfileService;

  @Override
  public <P> P toDto(Resource resourceToConvert, P parentDto, ResourceMappingContext context) {
    if (parentDto instanceof ResourceResponseDto resourceDto) {
      var authority = coreMapper.toDtoWithEdges(resourceToConvert, AuthorityResponse.class, false);
      authority.setId(String.valueOf(resourceToConvert.getId()));
      authority.setProfileId(resourceProfileService.resolveProfileId(resourceToConvert));
      resourceDto.setResource(new AuthorityResponseField().authority(authority));
    }
    return parentDto;
  }

  @Override
  public Resource toEntity(Object dto, Resource parentEntity) {
    var authorityDto = ((AuthorityField) dto).getAuthority();
    var type = this.getClass().getAnnotation(MapperUnit.class).type();
    var authority = new Resource().addTypes(type);
    authority.setDoc(getDoc(authorityDto));
    labelService.assignLabelToResource(authority);
    authority.setIdAndRefreshEdges(hashService.hash(authority));
    return authority;
  }

  protected abstract JsonNode getDoc(AuthorityRequest dto);
}
