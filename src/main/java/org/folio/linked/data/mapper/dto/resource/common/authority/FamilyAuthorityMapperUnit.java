package org.folio.linked.data.mapper.dto.resource.common.authority;

import static org.folio.ld.dictionary.PropertyDictionary.AFFILIATION;
import static org.folio.ld.dictionary.PropertyDictionary.ATTRIBUTION;
import static org.folio.ld.dictionary.PropertyDictionary.DATE;
import static org.folio.ld.dictionary.PropertyDictionary.MISC_INFO;
import static org.folio.ld.dictionary.PropertyDictionary.NAME;
import static org.folio.ld.dictionary.PropertyDictionary.NAME_ALTERNATIVE;
import static org.folio.ld.dictionary.PropertyDictionary.NUMERATION;
import static org.folio.ld.dictionary.PropertyDictionary.TITLES;
import static org.folio.ld.dictionary.ResourceTypeDictionary.FAMILY;
import static org.folio.linked.data.util.ResourceUtils.putProperty;

import java.util.HashMap;
import java.util.List;
import org.folio.linked.data.domain.dto.AuthorityField;
import org.folio.linked.data.domain.dto.AuthorityRequest;
import org.folio.linked.data.mapper.dto.resource.base.CoreMapper;
import org.folio.linked.data.mapper.dto.resource.base.MapperUnit;
import org.folio.linked.data.service.label.ResourceEntityLabelService;
import org.folio.linked.data.service.profile.ResourceProfileLinkingService;
import org.folio.linked.data.service.resource.hash.HashService;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;

@Component
@MapperUnit(type = FAMILY, requestDto = AuthorityField.class)
public class FamilyAuthorityMapperUnit extends AuthorityMapperUnit {

  public FamilyAuthorityMapperUnit(CoreMapper coreMapper, HashService hashService,
                                   ResourceEntityLabelService labelService,
                                   ResourceProfileLinkingService resourceProfileService) {
    super(coreMapper, hashService, labelService, resourceProfileService);
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
    return coreMapper.toJson(map);
  }
}
