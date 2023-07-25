package org.folio.linked.data.mapper.resource.monograph.inner.common.contribution.agent;

import static org.folio.linked.data.util.BibframeConstants.AGENT_PRED;
import static org.folio.linked.data.util.BibframeConstants.MEETING;
import static org.folio.linked.data.util.BibframeConstants.MEETING_URL;
import static org.folio.linked.data.util.BibframeConstants.SAME_AS_PRED;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.HashMap;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.folio.linked.data.domain.dto.Agent;
import org.folio.linked.data.domain.dto.Contribution;
import org.folio.linked.data.domain.dto.Lookup;
import org.folio.linked.data.domain.dto.MeetingField;
import org.folio.linked.data.mapper.resource.common.CoreMapper;
import org.folio.linked.data.mapper.resource.common.MapperUnit;
import org.folio.linked.data.mapper.resource.common.inner.sub.SubResourceMapper;
import org.folio.linked.data.mapper.resource.monograph.inner.common.contribution.ContributionSubResourceMapperUnit;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceType;
import org.folio.linked.data.service.dictionary.DictionaryService;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@MapperUnit(type = MEETING, predicate = AGENT_PRED, dtoClass = MeetingField.class)
public class ContributionMeetingMapperUnit implements ContributionSubResourceMapperUnit {

  private final DictionaryService<ResourceType> resourceTypeService;
  private final CoreMapper coreMapper;

  @Override
  public Contribution toDto(Resource source, Contribution destination) {
    var agent = coreMapper.readResourceDoc(source, Agent.class);
    destination.addAgentItem(new MeetingField().meeting(agent));
    return destination;
  }

  @Override
  public Resource toEntity(Object dto, String predicate, SubResourceMapper subResourceMapper) {
    var agent = ((MeetingField) dto).getMeeting();
    var resource = new Resource();
    resource.setLabel(MEETING_URL);
    resource.setType(resourceTypeService.get(MEETING));
    resource.setDoc(getDoc(agent));
    resource.setResourceHash(coreMapper.hash(resource));
    return resource;
  }

  private JsonNode getDoc(Agent dto) {
    var map = new HashMap<String, List<Lookup>>();
    map.put(SAME_AS_PRED, dto.getSameAs());
    return coreMapper.toJson(map);
  }

}
