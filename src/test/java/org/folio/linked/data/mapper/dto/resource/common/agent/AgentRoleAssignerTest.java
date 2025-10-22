package org.folio.linked.data.mapper.dto.resource.common.agent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;
import org.folio.linked.data.domain.dto.Agent;
import org.folio.linked.data.model.entity.PredicateEntity;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceEdge;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

@UnitTest
class AgentRoleAssignerTest {

  private static AgentRoleAssigner agentRoleAssigner;

  @BeforeAll
  static void setup() {
    agentRoleAssigner = new AgentRoleAssigner();
  }

  @Test
  void shouldAssignRolesToAgent() {
    var agent = new Agent().id("1");
    testAssignRoles(agent);
    testAssignRoles(agent);
    testAssignRoles(agent);
    testAssignRoles(agent);
  }

  private void testAssignRoles(Agent agent) {
    // given
    var role1Predicate = "http://bibfra.me/vocab/relation/role1";
    var role2Predicate = "http://bibfra.me/vocab/relation/role2";
    var nonRolePredicate = "http://bibfra.me/vocab/some/other/predicate";

    var workResource = new Resource();
    var sameAgentResource = new Resource().setIdAndRefreshEdges(Long.parseLong(agent.getId()));
    var anotherAgentResource = new Resource().setIdAndRefreshEdges(Long.parseLong(agent.getId()) + 1);

    workResource.setOutgoingEdges(Set.of(
      // "relation" edges pointing to the agent
      new ResourceEdge().setSource(workResource).setTarget(sameAgentResource)
        .setPredicate(new PredicateEntity().setUri(role1Predicate).setHash(1L)),
      new ResourceEdge().setSource(workResource).setTarget(sameAgentResource)
        .setPredicate(new PredicateEntity().setUri(role2Predicate).setHash(2L)),

      // "relation" edge pointing to another agent
      new ResourceEdge().setSource(workResource).setTarget(anotherAgentResource)
        .setPredicate(new PredicateEntity().setUri(role1Predicate).setHash(3L)),

      // "non-relation" edge pointing to the agent
      new ResourceEdge().setSource(workResource).setTarget(sameAgentResource)
        .setPredicate(new PredicateEntity().setUri(nonRolePredicate).setHash(4L))
    ));

    // when
    agentRoleAssigner.assignRoles(agent, workResource);

    // then
    var roles = agent.getRoles();
    assertEquals(2, roles.size());
    assertTrue(roles.contains(role1Predicate));
    assertTrue(roles.contains(role2Predicate));
  }
}
