package org.folio.linked.data.mapper.dto.monograph.work.sub;

import static java.util.Optional.ofNullable;
import static org.folio.linked.data.util.Constants.IS_NOT_FOUND;
import static org.folio.linked.data.util.Constants.RESOURCE_WITH_GIVEN_ID;

import java.util.function.BiConsumer;
import lombok.RequiredArgsConstructor;
import org.folio.ld.dictionary.PredicateDictionary;
import org.folio.linked.data.domain.dto.Agent;
import org.folio.linked.data.exception.NotFoundException;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceEdge;
import org.folio.linked.data.repo.ResourceRepository;

@RequiredArgsConstructor
public abstract class AgentMapperUnit implements WorkSubResourceMapperUnit {

  private final BiConsumer<Object, Agent> agentConsumer;
  private final AgentRoleAssigner agentRoleAssigner;
  private final ResourceRepository resourceRepository;

  @Override
  public <P> P toDto(Resource source, P parentDto, Resource parentResource) {
    var agent = new Agent()
      .id(String.valueOf(source.getId()))
      .label(source.getLabel())
      .type(source.getTypes().iterator().next().getUri());
    agentRoleAssigner.assignRoles(agent, parentResource);
    agentConsumer.accept(parentDto, agent);
    return parentDto;
  }

  @Override
  public Resource toEntity(Object dto, Resource parentEntity) {
    var agent = (Agent) dto;
    var resource = resourceRepository.findById(Long.parseLong(agent.getId()))
      .map(Resource::copyWithNoEdges)
      .orElseThrow(() -> new NotFoundException(RESOURCE_WITH_GIVEN_ID + agent.getId() + IS_NOT_FOUND));
    ofNullable(agent.getRoles())
      .ifPresent(roles -> roles.forEach(role -> PredicateDictionary.fromUri(role)
        .ifPresent(p -> parentEntity.addOutgoingEdge(new ResourceEdge(parentEntity, resource, p)))));
    return resource;
  }
}
