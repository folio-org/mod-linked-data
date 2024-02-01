package org.folio.linked.data.mapper.resource.monograph.work.sub.contributor;

import static org.folio.ld.dictionary.PredicateDictionary.CONTRIBUTOR;
import static org.folio.ld.dictionary.ResourceTypeDictionary.MEETING;

import org.folio.linked.data.domain.dto.MeetingField;
import org.folio.linked.data.mapper.resource.common.CoreMapper;
import org.folio.linked.data.mapper.resource.common.MapperUnit;
import org.folio.linked.data.mapper.resource.monograph.work.sub.AgentRoleAssigner;
import org.springframework.stereotype.Component;

@Component("ContributorMeetingMapperUnit")
@MapperUnit(type = MEETING, dtoClass = MeetingField.class, predicate = CONTRIBUTOR)
public class MeetingMapperUnit extends ContributorMapperUnit {

  public MeetingMapperUnit(CoreMapper coreMapper, AgentRoleAssigner agentRoleAssigner) {
    super(coreMapper, MEETING_TO_FIELD_CONVERTER, FIELD_TO_MEETING_CONVERTER, agentRoleAssigner, MEETING);
  }

}
