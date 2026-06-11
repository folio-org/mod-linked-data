package org.folio.linked.data.mapper.dto.resource.common.authority;

import static org.folio.ld.dictionary.ResourceTypeDictionary.MEETING;

import org.folio.linked.data.domain.dto.AuthorityField;
import org.folio.linked.data.mapper.dto.resource.base.CoreMapper;
import org.folio.linked.data.mapper.dto.resource.base.MapperUnit;
import org.folio.linked.data.service.label.ResourceEntityLabelService;
import org.folio.linked.data.service.profile.ResourceProfileLinkingService;
import org.folio.linked.data.service.resource.hash.HashService;
import org.springframework.stereotype.Component;

@Component
@MapperUnit(type = MEETING, requestDto = AuthorityField.class)
public class MeetingAuthorityMapperUnit extends AuthorityMapperUnit {

  public MeetingAuthorityMapperUnit(CoreMapper coreMapper, HashService hashService,
                                    ResourceEntityLabelService labelService,
                                    ResourceProfileLinkingService resourceProfileService) {
    super(coreMapper, hashService, labelService, resourceProfileService);
  }
}
