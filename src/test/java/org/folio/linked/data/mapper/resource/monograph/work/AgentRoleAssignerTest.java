package org.folio.linked.data.mapper.resource.monograph.work;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Set;
import java.util.function.Function;
import org.folio.linked.data.domain.dto.Agent;
import org.folio.linked.data.domain.dto.AgentTypeInner;
import org.folio.linked.data.domain.dto.FamilyField;
import org.folio.linked.data.domain.dto.MeetingField;
import org.folio.linked.data.domain.dto.OrganizationField;
import org.folio.linked.data.domain.dto.PersonField;
import org.folio.linked.data.mapper.resource.monograph.work.sub.AgentRoleAssigner;
import org.folio.linked.data.model.entity.PredicateEntity;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceEdge;
import org.folio.spring.test.type.UnitTest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

@UnitTest
class AgentRoleAssignerTest {

  private static AgentRoleAssigner agentRoleAssigner;

  @BeforeAll
  public static void setup() {
    agentRoleAssigner = new AgentRoleAssigner();
  }

  @Test
  void shouldAssignRolesToAgent() {
    Agent agent = new Agent().id("1");
    testAssignRoles(agent, a -> new FamilyField().family(a));
    testAssignRoles(agent, a -> new MeetingField().meeting(a));
    testAssignRoles(agent, a -> new OrganizationField().organization(a));
    testAssignRoles(agent, a -> new PersonField().person(a));
  }

  private void testAssignRoles(Agent agent, Function<Agent, AgentTypeInner> agentConverter) {
    // given
    String role1Predicate = "http://bibfra.me/vocab/relation/role1";
    String role2Predicate = "http://bibfra.me/vocab/relation/role2";
    String nonRolePredicate = "http://bibfra.me/vocab/some/other/predicate";

    Resource workResource = new Resource();
    Resource sameAgentResource = new Resource().setResourceHash(Long.parseLong(agent.getId()));
    Resource anotherAgentResource = new Resource().setResourceHash(Long.parseLong(agent.getId()) + 1);

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
    agentRoleAssigner.assignRoles(agentConverter.apply(agent), workResource);

    // then
    List<String> roles = agent.getRoles();
    assertEquals(2, roles.size());
    assertTrue(roles.contains(role1Predicate));
    assertTrue(roles.contains(role2Predicate));
  }
}
