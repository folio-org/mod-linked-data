package org.folio.linked.data.mapper.dto.monograph.work.sub;

import static java.util.Optional.ofNullable;
import static org.folio.ld.dictionary.PropertyDictionary.LCNAF_ID;
import static org.folio.ld.dictionary.PropertyDictionary.NAME;
import static org.folio.linked.data.util.BibframeUtils.putProperty;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.HashMap;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import org.folio.ld.dictionary.PredicateDictionary;
import org.folio.ld.dictionary.ResourceTypeDictionary;
import org.folio.linked.data.domain.dto.Agent;
import org.folio.linked.data.domain.dto.AgentContainer;
import org.folio.linked.data.domain.dto.FamilyField;
import org.folio.linked.data.domain.dto.MeetingField;
import org.folio.linked.data.domain.dto.OrganizationField;
import org.folio.linked.data.domain.dto.PersonField;
import org.folio.linked.data.domain.dto.Work;
import org.folio.linked.data.domain.dto.WorkReference;
import org.folio.linked.data.mapper.dto.common.CoreMapper;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceEdge;
import org.folio.linked.data.service.HashService;

@RequiredArgsConstructor
public abstract class AgentMapperUnit implements WorkSubResourceMapperUnit {
  protected static final Function<Agent, AgentContainer> FAMILY_TO_FIELD_CONVERTER = f -> new FamilyField().family(f);
  protected static final Function<Agent, AgentContainer> PERSON_TO_FIELD_CONVERTER = p -> new PersonField().person(p);
  protected static final Function<Agent, AgentContainer> ORG_TO_FIELD_CONVERTER =
    o -> new OrganizationField().organization(o);
  protected static final Function<Agent, AgentContainer> MEETING_TO_FIELD_CONVERTER =
    m -> new MeetingField().meeting(m);

  protected static final Function<Object, Agent> FIELD_TO_FAMILY_CONVERTER = o -> ((FamilyField) o).getFamily();
  protected static final Function<Object, Agent> FIELD_TO_PERSON_CONVERTER = o -> ((PersonField) o).getPerson();
  protected static final Function<Object, Agent> FIELD_TO_ORG_CONVERTER =
    o -> ((OrganizationField) o).getOrganization();
  protected static final Function<Object, Agent> FIELD_TO_MEETING_CONVERTER = o -> ((MeetingField) o).getMeeting();

  private final CoreMapper coreMapper;
  private final HashService hashService;
  private final BiConsumer<Object, Agent> agentConsumer;
  private final Function<Object, Agent> agentProvider;
  private final AgentRoleAssigner agentRoleAssigner;
  private final ResourceTypeDictionary type;

  @Override
  public <P> P toDto(Resource source, P parentDto, Resource parentResource) {
    var agent = coreMapper.toDtoWithEdges(source, Agent.class, false);
    agent.setId(String.valueOf(source.getResourceHash()));
    if (parentDto instanceof Work work) {
      agentConsumer.accept(work, agent);
      assignRoles(work.getCreator(), parentResource);
      assignRoles(work.getContributor(), parentResource);
    }
    if (parentDto instanceof WorkReference work) {
      agentConsumer.accept(work, agent);
      assignRoles(work.getCreator(), parentResource);
      assignRoles(work.getContributor(), parentResource);
    }
    return parentDto;
  }

  private void assignRoles(List<AgentContainer> agentContainers, Resource source) {
    ofNullable(agentContainers).ifPresent(acs -> acs.forEach(ac -> agentRoleAssigner.assignRoles(ac, source)));
  }

  @Override
  public Resource toEntity(Object dto, Resource parentEntity) {
    var agent = agentProvider.apply(dto);
    var resource = new Resource();
    resource.addType(type);
    resource.setDoc(getDoc(agent));
    resource.setResourceHash(hashService.hash(resource));
    ofNullable(agentRoleAssigner.getAgent((AgentContainer) dto).getRoles())
      .ifPresent(roles -> roles.forEach(role -> PredicateDictionary.fromUri(role)
        .ifPresent(p -> parentEntity.getOutgoingEdges().add(new ResourceEdge(parentEntity, resource, p)))));
    return resource;
  }

  private JsonNode getDoc(Agent dto) {
    var map = new HashMap<String, List<String>>();
    putProperty(map, NAME, dto.getName());
    putProperty(map, LCNAF_ID, dto.getLcnafId());
    return map.isEmpty() ? null : coreMapper.toJson(map);
  }

}
