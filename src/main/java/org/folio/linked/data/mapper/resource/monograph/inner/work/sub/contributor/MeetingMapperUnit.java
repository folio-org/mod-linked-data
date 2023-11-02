package org.folio.linked.data.mapper.resource.monograph.inner.work.sub.contributor;

import static org.folio.ld.dictionary.PredicateDictionary.CONTRIBUTOR;
import static org.folio.ld.dictionary.ResourceTypeDictionary.MEETING;

import org.folio.linked.data.domain.dto.MeetingField;
import org.folio.linked.data.mapper.resource.common.CoreMapper;
import org.folio.linked.data.mapper.resource.common.MapperUnit;
import org.springframework.stereotype.Component;

@Component("ContributorMeetingMapperUnit")
@MapperUnit(type = MEETING, dtoClass = MeetingField.class, predicate = CONTRIBUTOR)
public class MeetingMapperUnit extends ContributorMapperUnit {
  public MeetingMapperUnit(CoreMapper coreMapper) {
    super(coreMapper, MEETING_TO_FIELD_CONVERTER, FIELD_TO_MEETING_CONVERTER, MEETING);
  }
}
