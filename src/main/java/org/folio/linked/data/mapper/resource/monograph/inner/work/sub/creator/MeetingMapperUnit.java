package org.folio.linked.data.mapper.resource.monograph.inner.work.sub.creator;

import static org.folio.ld.dictionary.PredicateDictionary.CREATOR;
import static org.folio.ld.dictionary.ResourceTypeDictionary.MEETING;

import org.folio.linked.data.domain.dto.Agent;
import org.folio.linked.data.mapper.resource.common.CoreMapper;
import org.folio.linked.data.mapper.resource.common.MapperUnit;
import org.springframework.stereotype.Component;

@Component("CreatorMeetingMapperUnit")
@MapperUnit(type = MEETING, dtoClass = Agent.class, predicate = CREATOR)
public class MeetingMapperUnit extends CreatorMapperUnit {
  public MeetingMapperUnit(CoreMapper coreMapper) {
    super(coreMapper, MEETING_CONVERTER);
  }
}
