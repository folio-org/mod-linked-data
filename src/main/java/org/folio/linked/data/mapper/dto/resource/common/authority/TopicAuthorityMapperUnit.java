package org.folio.linked.data.mapper.dto.resource.common.authority;

import static org.folio.ld.dictionary.PropertyDictionary.GEOGRAPHIC_COVERAGE;
import static org.folio.ld.dictionary.PropertyDictionary.MISC_INFO;
import static org.folio.ld.dictionary.PropertyDictionary.NAME;
import static org.folio.ld.dictionary.ResourceTypeDictionary.TOPIC;
import static org.folio.linked.data.util.ResourceUtils.putProperty;

import java.util.HashMap;
import java.util.List;
import org.folio.linked.data.domain.dto.AuthorityField;
import org.folio.linked.data.domain.dto.AuthorityRequest;
import org.folio.linked.data.mapper.dto.resource.base.CoreMapper;
import org.folio.linked.data.mapper.dto.resource.base.MapperUnit;
import org.folio.linked.data.service.label.ResourceEntityLabelService;
import org.folio.linked.data.service.profile.ProfileService;
import org.folio.linked.data.service.profile.ResourceProfileLinkingService;
import org.folio.linked.data.service.resource.hash.HashService;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;

@Component
@MapperUnit(type = TOPIC, requestDto = AuthorityField.class)
public class TopicAuthorityMapperUnit extends AuthorityMapperUnit {

  public TopicAuthorityMapperUnit(CoreMapper coreMapper, HashService hashService,
                                  ResourceEntityLabelService labelService,
                                  ResourceProfileLinkingService resourceProfileService,
                                  ProfileService profileService) {
    super(coreMapper, hashService, labelService, resourceProfileService, profileService);
  }

  @Override
  protected JsonNode getDoc(AuthorityRequest dto) {
    var map = new HashMap<String, List<String>>();
    putProperty(map, NAME, dto.getName());
    putProperty(map, GEOGRAPHIC_COVERAGE, dto.getGeographicCoverage());
    putProperty(map, MISC_INFO, dto.getMiscInfo());
    return coreMapper.toJson(map);
  }
}
