package org.folio.linked.data.mapper.resource.monograph.inner.work.sub.contributor;

import static org.folio.linked.data.util.BibframeConstants.CONTRIBUTOR_PRED;
import static org.folio.linked.data.util.BibframeConstants.MEETING;

import org.folio.linked.data.domain.dto.Agent;
import org.folio.linked.data.mapper.resource.common.CoreMapper;
import org.folio.linked.data.mapper.resource.common.MapperUnit;
import org.springframework.stereotype.Component;

@Component("ContributorMeetingMapperUnit")
@MapperUnit(type = MEETING, dtoClass = Agent.class, predicate = CONTRIBUTOR_PRED)
public class MeetingMapperUnit extends ContributorMapperUnit {
  public MeetingMapperUnit(CoreMapper coreMapper) {
    super(coreMapper, MEETING_CONVERTER);
  }
}
