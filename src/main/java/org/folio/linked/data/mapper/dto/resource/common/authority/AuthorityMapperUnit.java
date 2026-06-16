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
import org.folio.ld.dictionary.ResourceTypeDictionary;
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
    var type = resourceProfileService.resolveResourceType(authorityDto.getProfileId());
    var authority = new Resource().addTypes(type);
    authority.setDoc(getDocByType(type, authorityDto));
    labelService.assignLabelToResource(authority);
    authority.setIdAndRefreshEdges(hashService.hash(authority));
    return authority;
  }

  private JsonNode getDocByType(ResourceTypeDictionary type, AuthorityRequest dto) {
    return switch (type) {
      case FAMILY       -> getFamilyDoc(dto);
      case FORM         -> getFormDoc(dto);
      case JURISDICTION -> getJurisdictionDoc(dto);
      case MEETING      -> getMeetingDoc(dto);
      case ORGANIZATION -> getOrganizationDoc(dto);
      case PERSON       -> getPersonDoc(dto);
      case PLACE        -> getPlaceDoc(dto);
      case TEMPORAL     -> getTemporalDoc(dto);
      case TOPIC        -> getTopicDoc(dto);
      default -> throw new IllegalArgumentException("Unsupported authority type: " + type);
    };
  }

  private JsonNode getFamilyDoc(AuthorityRequest dto) {
    var map = new HashMap<String, List<String>>();
    putProperty(map, NAME, dto.getName());
    putProperty(map, NUMERATION, dto.getNumeration());
    putProperty(map, TITLES, dto.getTitles());
    putProperty(map, DATE, dto.getDate());
    putProperty(map, MISC_INFO, dto.getMiscInfo());
    putProperty(map, ATTRIBUTION, dto.getAttribution());
    putProperty(map, NAME_ALTERNATIVE, dto.getNameAlternative());
    putProperty(map, AFFILIATION, dto.getAffiliation());
    return coreMapper.toJson(map);
  }

  private JsonNode getFormDoc(AuthorityRequest dto) {
    var map = new HashMap<String, List<String>>();
    putProperty(map, NAME, dto.getName());
    return coreMapper.toJson(map);
  }

  private JsonNode getJurisdictionDoc(AuthorityRequest dto) {
    var map = new HashMap<String, List<String>>();
    putProperty(map, NAME, dto.getName());
    putProperty(map, SUBORDINATE_UNIT, dto.getSubordinateUnit());
    putProperty(map, PLACE, dto.getPlace());
    putProperty(map, DATE, dto.getDate());
    putProperty(map, MISC_INFO, dto.getMiscInfo());
    putProperty(map, AFFILIATION, dto.getAffiliation());
    return coreMapper.toJson(map);
  }

  private JsonNode getMeetingDoc(AuthorityRequest dto) {
    var map = new HashMap<String, List<String>>();
    putProperty(map, NAME, dto.getName());
    putProperty(map, PLACE, dto.getPlace());
    putProperty(map, DATE, dto.getDate());
    putProperty(map, SUBORDINATE_UNIT, dto.getSubordinateUnit());
    putProperty(map, MISC_INFO, dto.getMiscInfo());
    putProperty(map, AFFILIATION, dto.getAffiliation());
    return coreMapper.toJson(map);
  }

  private JsonNode getOrganizationDoc(AuthorityRequest dto) {
    return getJurisdictionDoc(dto);
  }

  private JsonNode getPersonDoc(AuthorityRequest dto) {
    return getFamilyDoc(dto);
  }

  private JsonNode getPlaceDoc(AuthorityRequest dto) {
    var map = new HashMap<String, List<String>>();
    putProperty(map, NAME, dto.getName());
    putProperty(map, MISC_INFO, dto.getMiscInfo());
    return coreMapper.toJson(map);
  }

  private JsonNode getTemporalDoc(AuthorityRequest dto) {
    return getFormDoc(dto);
  }

  private JsonNode getTopicDoc(AuthorityRequest dto) {
    var map = new HashMap<String, List<String>>();
    putProperty(map, NAME, dto.getName());
    putProperty(map, GEOGRAPHIC_COVERAGE, dto.getGeographicCoverage());
    putProperty(map, MISC_INFO, dto.getMiscInfo());
    return coreMapper.toJson(map);
  }
}
