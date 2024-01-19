package org.folio.linked.data.mapper.resource.monograph.work.sub;

import static org.folio.linked.data.util.Constants.RELATION_PREDICATE_PREFIX;

import java.util.List;
import org.folio.linked.data.domain.dto.Agent;
import org.folio.linked.data.domain.dto.AgentContainer;
import org.folio.linked.data.domain.dto.FamilyField;
import org.folio.linked.data.domain.dto.MeetingField;
import org.folio.linked.data.domain.dto.OrganizationField;
import org.folio.linked.data.domain.dto.PersonField;
import org.folio.linked.data.exception.NotSupportedException;
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
  public void assignRoles(AgentContainer agentInner, Resource workResource) {
    Agent agent = getAgent(agentInner);

    List<String> roles = workResource
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

  public Agent getAgent(AgentContainer agentContainer) {
    if (agentContainer instanceof FamilyField familyField) {
      return familyField.getFamily();
    }
    if (agentContainer instanceof PersonField personField) {
      return personField.getPerson();
    }
    if (agentContainer instanceof OrganizationField organizationField) {
      return organizationField.getOrganization();
    }
    if (agentContainer instanceof MeetingField meetingField) {
      return meetingField.getMeeting();
    }
    // should not be here
    throw new NotSupportedException("Unknown agent type: " + agentContainer.getClass().getSimpleName());
  }
}
