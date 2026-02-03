package org.folio.linked.data.mapper.dto.resource.common.agent;

import static java.util.Optional.ofNullable;
import static org.folio.ld.dictionary.PredicateDictionary.CONTRIBUTOR;
import static org.folio.ld.dictionary.PredicateDictionary.CREATOR;
import static org.folio.linked.data.util.ResourceUtils.ensureLatestReplaced;
import static org.folio.linked.data.util.ResourceUtils.isPreferred;

import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.folio.ld.dictionary.PredicateDictionary;
import org.folio.linked.data.domain.dto.Agent;
import org.folio.linked.data.domain.dto.AgentResponse;
import org.folio.linked.data.domain.dto.HubRequest;
import org.folio.linked.data.domain.dto.HubResponse;
import org.folio.linked.data.domain.dto.WorkRequest;
import org.folio.linked.data.domain.dto.WorkResponse;
import org.folio.linked.data.mapper.dto.resource.base.SingleResourceMapperUnit;
import org.folio.linked.data.model.dto.HasCreator;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceEdge;
import org.folio.linked.data.service.reference.ReferenceService;

@RequiredArgsConstructor
public abstract class AgentMapperUnit implements SingleResourceMapperUnit {

  private final AgentRoleAssigner agentRoleAssigner;
  private final ReferenceService referenceService;

  @Override
  public <P> P toDto(Resource resourceToConvert, P parentDto, ResourceMappingContext context) {
    if (parentDto instanceof HasCreator dtoWithCreator) {
      resourceToConvert = ensureLatestReplaced(resourceToConvert);
      var agent = new AgentResponse()
        .id(String.valueOf(resourceToConvert.getId()))
        .label(resourceToConvert.getLabel())
        .type(resourceToConvert.getTypes().iterator().next().getUri())
        .isPreferred(isPreferred(resourceToConvert));
      agentRoleAssigner.assignRoles(agent, context.parentResource());

      if (context.predicate().getUri().equals(CREATOR.getUri())) {
        dtoWithCreator.addCreatorReferenceItem(agent);
      } else if (context.predicate().getUri().equals(CONTRIBUTOR.getUri())) {
        dtoWithCreator.addContributorReferenceItem(agent);
      }
    }

    return parentDto;
  }

  @Override
  public Resource toEntity(Object dto, Resource parentEntity) {
    var agent = (Agent) dto;
    var resource = referenceService.resolveReference(agent);
    ofNullable(agent.getRoles())
      .ifPresent(roles -> roles.forEach(role -> PredicateDictionary.fromUri(role)
        .ifPresent(p -> parentEntity.addOutgoingEdge(new ResourceEdge(parentEntity, resource, p)))));
    return resource;
  }

  @Override
  public Set<Class<?>> supportedParents() {
    return Set.of(WorkRequest.class, WorkResponse.class, HubRequest.class, HubResponse.class);
  }
}
