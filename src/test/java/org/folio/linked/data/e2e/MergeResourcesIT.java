package org.folio.linked.data.e2e;

import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.linked.data.test.TestUtil.FOLIO_TEST_PROFILE;
import static org.folio.linked.data.util.Constants.FOLIO_PROFILE;

import java.util.List;
import java.util.Map;
import java.util.Set;
import org.folio.ld.dictionary.PredicateDictionary;
import org.folio.ld.dictionary.PropertyDictionary;
import org.folio.ld.dictionary.ResourceTypeDictionary;
import org.folio.linked.data.e2e.base.IntegrationTest;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.repo.ResourceRepository;
import org.folio.linked.data.test.MonographTestUtil;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;

@IntegrationTest
@ActiveProfiles({FOLIO_PROFILE, FOLIO_TEST_PROFILE})
class MergeResourcesIT {

  @Autowired
  private ResourceRepository resourceRepository;


  @Test
  @Disabled("Failing test - to be fixed by MODLD-261")
  // TOOO - add assertions for contents of graph. Currently only validating the count of outgoing edges.
  void testMergeResources() {

    Resource graph1 = createFirstGraph();
    resourceRepository.save(graph1);
    var fp1Resource = resourceRepository.findById(1L).get();
    // Should be: 1 -> [2]
    assertThat(fp1Resource.getOutgoingEdges()).hasSize(1);

    Resource graph2 = createSecondGraph();
    resourceRepository.save(graph2);
    fp1Resource = resourceRepository.findById(1L).get();
    var fp3Resource = resourceRepository.findById(3L).get();
    // fp1Resource should be: 1 -> [2, 5]
    assertThat(fp1Resource.getOutgoingEdges()).hasSize(2);
    // fp3Resource should be: 3 -> [4, (1 -> [2, 5])]
    assertThat(fp3Resource.getOutgoingEdges()).hasSize(2);

    Resource graph3 = createThirdGraph();
    resourceRepository.save(graph3);
    fp1Resource = resourceRepository.findById(1L).get();
    fp3Resource = resourceRepository.findById(3L).get();
    var fp6Resource = resourceRepository.findById(6L).get();
    // fp1Resource should be: 1 -> [2, 5]
    assertThat(fp1Resource.getOutgoingEdges()).hasSize(2);
    // fp3Resource should be: 3 -> [(1 -> [2, 5]), (4 -> 5)]
    assertThat(fp3Resource.getOutgoingEdges()).hasSize(2);
    // fp6Resource should be: 6 -> [(1 -> [2, 5]), (4 -> 5)]
    assertThat(fp6Resource.getOutgoingEdges()).hasSize(2);
  }

  private static Resource createFirstGraph() {
    // 1 -> [2]
    Resource fp2Resource = createResource(2L, Map.of());
    Resource fp1Resource = createResource(1L, Map.of(PredicateDictionary.MAP, List.of(fp2Resource)));
    return fp1Resource;
  }

  private static Resource createSecondGraph() {
    // 3 -> [(1 -> 5), 4]
    Resource fp5Resource = createResource(5L, Map.of());
    Resource fp1Resource = createResource(1L, Map.of(PredicateDictionary.MAP, List.of(fp5Resource)));
    Resource fp4Resource = createResource(4L, Map.of());
    Resource fp3Resource = createResource(3L, Map.of(PredicateDictionary.MAP, List.of(fp1Resource, fp4Resource)));

    return fp3Resource;
  }

  private Resource createThirdGraph() {
    // 6 -> [1, (4 -> 5)]
    Resource fp1Resource = createResource(1L, Map.of());
    Resource fp5Resource = createResource(5L, Map.of());
    Resource fp4Resource = createResource(4L, Map.of(PredicateDictionary.MAP, List.of(fp5Resource)));
    Resource fp6Resource = createResource(6L, Map.of(PredicateDictionary.MAP, List.of(fp1Resource, fp4Resource)));

    return fp6Resource;
  }

  private static Resource createResource(Long hash, Map<PredicateDictionary, List<Resource>> pred2OutgoingResources) {
    return MonographTestUtil.createResource(
      Map.of(PropertyDictionary.NAME, List.of("John Doe")),
      Set.of(ResourceTypeDictionary.IDENTIFIER),
      pred2OutgoingResources
    ).setResourceHash(hash);
  }
}
