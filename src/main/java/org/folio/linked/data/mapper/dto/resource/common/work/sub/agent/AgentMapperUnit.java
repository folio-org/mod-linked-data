package org.folio.linked.data.mapper.dto.resource.common.work.sub.agent;

import static java.util.Optional.ofNullable;
import static org.folio.ld.dictionary.PredicateDictionary.CONTRIBUTOR;
import static org.folio.ld.dictionary.PredicateDictionary.CREATOR;
import static org.folio.linked.data.util.ResourceUtils.ensureLatestReplaced;
import static org.folio.linked.data.util.ResourceUtils.isPreferred;

import lombok.RequiredArgsConstructor;
import org.folio.ld.dictionary.PredicateDictionary;
import org.folio.linked.data.domain.dto.Agent;
import org.folio.linked.data.domain.dto.WorkResponse;
import org.folio.linked.data.mapper.dto.resource.common.work.sub.WorkSubResourceMapperUnit;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceEdge;
import org.folio.linked.data.service.resource.marc.ResourceMarcAuthorityService;

@RequiredArgsConstructor
public abstract class AgentMapperUnit implements WorkSubResourceMapperUnit {

  private final AgentRoleAssigner agentRoleAssigner;
  private final ResourceMarcAuthorityService resourceMarcAuthorityService;

  @Override
  public <P> P toDto(Resource resourceToConvert, P parentDto, ResourceMappingContext context) {
    if (parentDto instanceof WorkResponse workResponse) {
      resourceToConvert = ensureLatestReplaced(resourceToConvert);
      var agent = new Agent()
        .id(String.valueOf(resourceToConvert.getId()))
        .label(resourceToConvert.getLabel())
        .type(resourceToConvert.getTypes().iterator().next().getUri())
        .isPreferred(isPreferred(resourceToConvert));
      agentRoleAssigner.assignRoles(agent, context.parentResource());

      if (context.predicate().getUri().equals(CREATOR.getUri())) {
        workResponse.addCreatorReferenceItem(agent);
      } else if (context.predicate().getUri().equals(CONTRIBUTOR.getUri())) {
        workResponse.addContributorReferenceItem(agent);
      }

    }

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
