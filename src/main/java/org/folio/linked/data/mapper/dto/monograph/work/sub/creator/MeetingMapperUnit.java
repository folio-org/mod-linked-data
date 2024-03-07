package org.folio.linked.data.mapper.dto.monograph.work.sub.creator;

import static org.folio.ld.dictionary.PredicateDictionary.CREATOR;
import static org.folio.ld.dictionary.ResourceTypeDictionary.MEETING;

import org.folio.linked.data.domain.dto.MeetingField;
import org.folio.linked.data.mapper.dto.common.CoreMapper;
import org.folio.linked.data.mapper.dto.common.MapperUnit;
import org.folio.linked.data.mapper.dto.monograph.work.sub.AgentRoleAssigner;
import org.folio.linked.data.service.HashService;
import org.springframework.stereotype.Component;

@Component("CreatorMeetingMapperUnit")
@MapperUnit(type = MEETING, dtoClass = MeetingField.class, predicate = CREATOR)
public class MeetingMapperUnit extends CreatorMapperUnit {

  public MeetingMapperUnit(CoreMapper coreMapper, HashService hashService, AgentRoleAssigner agentRoleAssigner) {
    super(coreMapper, hashService, MEETING_TO_FIELD_CONVERTER, FIELD_TO_MEETING_CONVERTER, agentRoleAssigner, MEETING);
  }

}
