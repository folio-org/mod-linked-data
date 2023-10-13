package org.folio.linked.data.mapper.resource.monograph.inner.work;

import static org.folio.linked.data.util.BibframeConstants.RELATION_PREDICATE_PREFIX;

import java.util.List;
import org.folio.linked.data.domain.dto.Agent;
import org.folio.linked.data.domain.dto.AgentTypeInner;
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
  public void assignRoles(AgentTypeInner agentInner, Resource workResource) {
    Agent agent = getAgent(agentInner);

    List<String> roles = workResource
      .getOutgoingEdges()
      .stream()
      .filter(re -> re.getPredicate().getLabel().startsWith(RELATION_PREDICATE_PREFIX))
      .filter(re -> String.valueOf(re.getTarget().getResourceHash()).equals(agent.getId()))
      .map(re -> re.getPredicate().getLabel())
      .toList();

    if (!roles.isEmpty()) {
      agent.setRoles(roles);
    }
  }

  private Agent getAgent(AgentTypeInner agentInner) {
    if (agentInner instanceof FamilyField familyField) {
      return familyField.getFamily();
    }
    if (agentInner instanceof PersonField personField) {
      return personField.getPerson();
    }
    if (agentInner instanceof OrganizationField organizationField) {
      return organizationField.getOrganization();
    }
    if (agentInner instanceof MeetingField meetingField) {
      return meetingField.getMeeting();
    }
    // should not be here
    throw new NotSupportedException("Unknown agent type: " + agentInner.getClass().getSimpleName());
  }
}