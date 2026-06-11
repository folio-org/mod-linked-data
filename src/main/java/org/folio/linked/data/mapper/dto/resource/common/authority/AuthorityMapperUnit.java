package org.folio.linked.data.mapper.dto.resource.common.authority;

import static org.folio.ld.dictionary.PropertyDictionary.AFFILIATION;
import static org.folio.ld.dictionary.PropertyDictionary.ATTRIBUTION;
import static org.folio.ld.dictionary.PropertyDictionary.DATE;
import static org.folio.ld.dictionary.PropertyDictionary.GEOGRAPHIC_COVERAGE;
import static org.folio.ld.dictionary.PropertyDictionary.MISC_INFO;
import static org.folio.ld.dictionary.PropertyDictionary.NAME;
import static org.folio.ld.dictionary.PropertyDictionary.NAME_ALTERNATIVE;
import static org.folio.ld.dictionary.PropertyDictionary.NUMERATION;
import static org.folio.ld.dictionary.PropertyDictionary.PLACE;
import static org.folio.ld.dictionary.PropertyDictionary.SUBORDINATE_UNIT;
import static org.folio.ld.dictionary.PropertyDictionary.TITLES;
import static org.folio.linked.data.util.ResourceUtils.putProperty;

import java.util.HashMap;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.folio.linked.data.domain.dto.AuthorityField;
import org.folio.linked.data.domain.dto.AuthorityRequest;
import org.folio.linked.data.domain.dto.AuthorityResponse;
import org.folio.linked.data.domain.dto.AuthorityResponseField;
import org.folio.linked.data.domain.dto.ResourceResponseDto;
import org.folio.linked.data.mapper.dto.resource.base.CoreMapper;
import org.folio.linked.data.mapper.dto.resource.common.TopResourceMapperUnit;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.service.label.ResourceEntityLabelService;
import org.folio.linked.data.service.profile.ResourceProfileLinkingService;
import org.folio.linked.data.service.resource.hash.HashService;
import tools.jackson.databind.JsonNode;

@RequiredArgsConstructor
public class AuthorityMapperUnit extends TopResourceMapperUnit {

  private final CoreMapper coreMapper;
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
    var authority = new Resource().addTypes(resourceProfileService.resolveResourceType(authorityDto.getProfileId()));
    authority.setDoc(getDoc(authorityDto));
    labelService.assignLabelToResource(authority);
    authority.setIdAndRefreshEdges(hashService.hash(authority));
    return authority;
  }

  private JsonNode getDoc(AuthorityRequest dto) {
    var map = new HashMap<String, List<String>>();
    putProperty(map, NAME, dto.getName());
    putProperty(map, NUMERATION, dto.getNumeration());
    putProperty(map, TITLES, dto.getTitles());
    putProperty(map, DATE, dto.getDate());
    putProperty(map, MISC_INFO, dto.getMiscInfo());
    putProperty(map, ATTRIBUTION, dto.getAttribution());
    putProperty(map, NAME_ALTERNATIVE, dto.getNameAlternative());
    putProperty(map, AFFILIATION, dto.getAffiliation());
    putProperty(map, SUBORDINATE_UNIT, dto.getSubordinateUnit());
    putProperty(map, PLACE, dto.getPlace());
    putProperty(map, GEOGRAPHIC_COVERAGE, dto.getGeographicCoverage());
    return coreMapper.toJson(map);
  }
}
