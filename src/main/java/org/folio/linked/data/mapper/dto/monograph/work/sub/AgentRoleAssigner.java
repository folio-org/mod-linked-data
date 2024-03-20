package org.folio.linked.data.mapper.dto.monograph.work.sub;

import static org.folio.linked.data.util.Constants.RELATION_PREDICATE_PREFIX;

import org.folio.linked.data.domain.dto.Agent;
import org.folio.linked.data.model.entity.Resource;
import org.springframework.stereotype.Component;

@Component
public class AgentRoleAssigner {

  public void assignRoles(Agent agent, Resource workResource) {
    var roles = workResource
      .getOutgoingEdges()
      .stream()
      .filter(re -> re.getPredicate().getUri().startsWith(RELATION_PREDICATE_PREFIX))
      .filter(re -> String.valueOf(re.getTarget().getResourceHash()).equals(agent.getId()))
      .map(re -> re.getPredicate().getUri())
      .toList();

    if (!roles.isEmpty()) {
      agent.setRoles(roles);
    }
  }
}
