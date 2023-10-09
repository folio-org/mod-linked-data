package org.folio.linked.data.mapper.resource.monograph.inner.work.sub;

import java.util.function.BiConsumer;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.NotImplementedException;
import org.folio.linked.data.domain.dto.Agent;
import org.folio.linked.data.domain.dto.AgentTypeInner;
import org.folio.linked.data.domain.dto.FamilyField;
import org.folio.linked.data.domain.dto.MeetingField;
import org.folio.linked.data.domain.dto.OrganizationField;
import org.folio.linked.data.domain.dto.PersonField;
import org.folio.linked.data.domain.dto.Work;
import org.folio.linked.data.mapper.resource.common.CoreMapper;
import org.folio.linked.data.mapper.resource.common.inner.sub.SubResourceMapper;
import org.folio.linked.data.model.entity.Resource;

@RequiredArgsConstructor
public abstract class AgentMapperUnit implements WorkSubResourceMapperUnit {
  protected static final Function<Agent, AgentTypeInner> FAMILY_CONVERTER = f -> new FamilyField().family(f);
  protected static final Function<Agent, AgentTypeInner> PERSON_CONVERTER = p -> new PersonField().person(p);
  protected static final Function<Agent, AgentTypeInner> ORG_CONVERTER = o -> new OrganizationField().organization(o);
  protected static final Function<Agent, AgentTypeInner> MEETING_CONVERTER = m -> new MeetingField().meeting(m);

  private final CoreMapper coreMapper;
  private final BiConsumer<Work, Agent> agentConsumer;

  @Override
  public Work toDto(Resource source, Work destination) {
    var person = coreMapper.readResourceDoc(source, Agent.class);
    person.setId(String.valueOf(source.getResourceHash()));
    agentConsumer.accept(destination, person);
    return destination;
  }

  @Override
  public Resource toEntity(Object dto, String predicate, SubResourceMapper subResourceMapper) {
    // Not implemented yet as we don't support PUT / POST APIs for Work
    throw new NotImplementedException();
  }
}
