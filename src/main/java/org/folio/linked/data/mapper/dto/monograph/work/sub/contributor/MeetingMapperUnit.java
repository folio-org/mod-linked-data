package org.folio.linked.data.mapper.dto.monograph.work.sub.contributor;

import static org.folio.ld.dictionary.PredicateDictionary.CONTRIBUTOR;
import static org.folio.ld.dictionary.ResourceTypeDictionary.MEETING;

import org.folio.linked.data.domain.dto.MeetingField;
import org.folio.linked.data.mapper.dto.common.CoreMapper;
import org.folio.linked.data.mapper.dto.common.MapperUnit;
import org.folio.linked.data.mapper.dto.monograph.work.sub.AgentRoleAssigner;
import org.folio.linked.data.service.HashService;
import org.springframework.stereotype.Component;

@Component("ContributorMeetingMapperUnit")
@MapperUnit(type = MEETING, dtoClass = MeetingField.class, predicate = CONTRIBUTOR)
public class MeetingMapperUnit extends ContributorMapperUnit {

  public MeetingMapperUnit(CoreMapper coreMapper, HashService hashService, AgentRoleAssigner agentRoleAssigner) {
    super(coreMapper, hashService, MEETING_TO_FIELD_CONVERTER, FIELD_TO_MEETING_CONVERTER, agentRoleAssigner, MEETING);
  }

}
