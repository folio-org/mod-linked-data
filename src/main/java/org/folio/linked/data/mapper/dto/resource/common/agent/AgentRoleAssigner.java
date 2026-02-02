package org.folio.linked.data.mapper.dto.resource.common.agent;

import static org.folio.linked.data.util.Constants.RELATION_PREDICATE_PREFIX;

import org.folio.linked.data.domain.dto.AgentResponse;
import org.folio.linked.data.model.entity.Resource;
import org.springframework.stereotype.Component;

/**
 * Class responsible for assigning roles to Agents.
 */
@Component
public class AgentRoleAssigner {

  /**
   * Assigns roles to the given agent. Roles can be found in the work resource as outgoing edges with a predicate that
   * starts with "http://bibfra.me/vocab/relation/" and has the agent's id as target.
   */
  public void assignRoles(AgentResponse agent, Resource workResource) {
    var roles = workResource
      .getOutgoingEdges()
      .stream()
      .filter(re -> re.getPredicate().getUri().startsWith(RELATION_PREDICATE_PREFIX))
      .filter(re -> String.valueOf(re.getTarget().getId()).equals(agent.getId()))
      .map(re -> re.getPredicate().getUri())
      .toList();

    if (!roles.isEmpty()) {
      agent.setRoles(roles);
    }
  }
}
