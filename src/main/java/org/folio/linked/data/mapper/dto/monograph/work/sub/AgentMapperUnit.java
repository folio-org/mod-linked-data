package org.folio.linked.data.mapper.dto.monograph.work.sub;

import static java.util.Optional.ofNullable;
import static org.folio.linked.data.util.ResourceUtils.ensureLatestReplaced;
import static org.folio.linked.data.util.ResourceUtils.isPreferred;

import java.util.function.BiConsumer;
import lombok.RequiredArgsConstructor;
import org.folio.ld.dictionary.PredicateDictionary;
import org.folio.linked.data.domain.dto.Agent;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceEdge;
import org.folio.linked.data.service.resource.marc.ResourceMarcAuthorityService;

@RequiredArgsConstructor
public abstract class AgentMapperUnit implements WorkSubResourceMapperUnit {

  private final BiConsumer<Object, Agent> agentConsumer;
  private final AgentRoleAssigner agentRoleAssigner;
  private final ResourceMarcAuthorityService resourceMarcAuthorityService;

  @Override
  public <P> P toDto(Resource source, P parentDto, Resource parentResource) {
    source = ensureLatestReplaced(source);
    var agent = new Agent()
      .id(String.valueOf(source.getId()))
      .label(source.getLabel())
      .type(source.getTypes().iterator().next().getUri())
      .isPreferred(isPreferred(source));
    agentRoleAssigner.assignRoles(agent, parentResource);
    agentConsumer.accept(parentDto, agent);
    return parentDto;
  }

  @Override
  public Resource toEntity(Object dto, Resource parentEntity) {
    var agent = (Agent) dto;
    var resource = resourceMarcAuthorityService.fetchAuthorityOrCreateFromSrsRecord(agent);
    ofNullable(agent.getRoles())
      .ifPresent(roles -> roles.forEach(role -> PredicateDictionary.fromUri(role)
        .ifPresent(p -> parentEntity.addOutgoingEdge(new ResourceEdge(parentEntity, resource, p)))));
    return resource;
  }

}
