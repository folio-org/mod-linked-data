package org.folio.linked.data.mapper.dto.resource.common.authority;

import static org.folio.ld.dictionary.PropertyDictionary.AFFILIATION;
import static org.folio.ld.dictionary.PropertyDictionary.ATTRIBUTION;
import static org.folio.ld.dictionary.PropertyDictionary.DATE;
import static org.folio.ld.dictionary.PropertyDictionary.MISC_INFO;
import static org.folio.ld.dictionary.PropertyDictionary.NAME;
import static org.folio.ld.dictionary.PropertyDictionary.NAME_ALTERNATIVE;
import static org.folio.ld.dictionary.PropertyDictionary.NUMBER_OF_PARTS;
import static org.folio.ld.dictionary.PropertyDictionary.NUMERATION;
import static org.folio.ld.dictionary.PropertyDictionary.TITLES;
import static org.folio.ld.dictionary.ResourceTypeDictionary.FAMILY;
import static org.folio.ld.dictionary.ResourceTypeDictionary.PERSON;
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
@MapperUnit(type = {PERSON, FAMILY}, requestDto = AuthorityField.class)
public class PersonAndFamilyAuthorityMapperUnit extends AuthorityMapperUnit {

  public PersonAndFamilyAuthorityMapperUnit(CoreMapper coreMapper, HashService hashService,
                                            ResourceEntityLabelService labelService,
                                            ResourceProfileLinkingService resourceProfileService,
                                            ProfileService profileService) {
    super(coreMapper, hashService, labelService, resourceProfileService, profileService);
  }

  @Override
  protected JsonNode getDoc(AuthorityRequest dto) {
    var map = new HashMap<String, List<String>>();
    putProperty(map, NAME, dto.getName());
    putProperty(map, NUMERATION, dto.getNumeration());
    putProperty(map, TITLES, dto.getTitles());
    putProperty(map, DATE, dto.getDate());
    putProperty(map, MISC_INFO, dto.getMiscInfo());
    putProperty(map, ATTRIBUTION, dto.getAttribution());
    putProperty(map, NAME_ALTERNATIVE, dto.getNameAlternative());
    putProperty(map, AFFILIATION, dto.getAffiliation());
    putProperty(map, NUMBER_OF_PARTS, dto.getNumberOfParts());
    return coreMapper.toJson(map);
  }
}
