package org.folio.linked.data.e2e.batch;

import static java.util.Set.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.ld.dictionary.PredicateDictionary.INSTANTIATES;
import static org.folio.ld.dictionary.ResourceTypeDictionary.ABBREVIATED_TITLE;
import static org.folio.ld.dictionary.ResourceTypeDictionary.CATEGORY;
import static org.folio.ld.dictionary.ResourceTypeDictionary.PERSON;
import static org.folio.linked.data.test.MonographTestUtil.createResource;
import static org.folio.linked.data.test.MonographTestUtil.getSampleHub;
import static org.folio.linked.data.test.MonographTestUtil.getSampleInstanceResource;
import static org.folio.linked.data.test.TestUtil.TENANT_ID;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;
import lombok.extern.log4j.Log4j2;
import org.folio.linked.data.configuration.batch.graph.reader.GraphCleaningReader;
import org.folio.linked.data.e2e.base.ITBase;
import org.folio.linked.data.e2e.base.IntegrationTest;
import org.folio.linked.data.model.entity.FolioMetadata;
import org.folio.linked.data.model.entity.ResourceEdge;
import org.junit.jupiter.api.Test;
import org.springframework.batch.infrastructure.item.ExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;

@Log4j2
@IntegrationTest
class GraphCleaningReaderIT extends ITBase {

  @Autowired
  private DataSource dataSource;

  @Test
  void read_shouldReturnOnlyOrphanResourcesWithoutExcludedTypesAndWithoutFolioMetadata() {
    // given
    var orphanIds = tenantScopedExecutionService.execute(TENANT_ID, () -> {
      // An Instance and a Work -> ignored
      resourceTestService.saveGraph(getSampleInstanceResource());

      // A Hub -> ignored
      resourceTestService.saveGraph(getSampleHub());

      // A resource with FolioMetadata -> ignored
      var resourceWithFolioMetadata = createResource(
        Map.of(),
        of(PERSON),
        Map.of()
      ).setLabel("resourceWithFolioMetadata");
      var folioMetadata = new FolioMetadata(resourceWithFolioMetadata).setInventoryId("some-inventory-id");
      resourceWithFolioMetadata.setFolioMetadata(folioMetadata);
      resourceTestService.saveGraph(resourceWithFolioMetadata);

      // A resource with incoming edge -> ignored
      var resourceWithIncomingEdge = createResource(
        Map.of(),
        of(CATEGORY),
        Map.of()
      ).setLabel("resourceWithIncomingEdge");
      var edge = new ResourceEdge(resourceWithFolioMetadata, resourceWithIncomingEdge, INSTANTIATES);
      resourceWithIncomingEdge.addIncomingEdge(edge);
      resourceWithFolioMetadata.addOutgoingEdge(edge);
      resourceTestService.saveGraph(resourceWithIncomingEdge);

      var orphanResource1 = resourceTestService.saveGraph(
        createResource(Map.of(), of(ABBREVIATED_TITLE), Map.of()).setLabel("orphanResource1")
      );

      var orphanResource2 = resourceTestService.saveGraph(
        createResource(Map.of(), of(CATEGORY), Map.of()).setLabel("orphanResource2")
      );

      return List.of(orphanResource1.getId(), orphanResource2.getId());
    });

    // when
    var result = tenantScopedExecutionService.execute(TENANT_ID, () -> {
      var reader = new GraphCleaningReader(dataSource, 100);
      var ids = new ArrayList<Long>();
      reader.open(new ExecutionContext());
      Long id;
      while ((id = reader.read()) != null) {
        ids.add(id);
      }
      reader.close();
      return ids;
    });

    // then
    assertThat(result).isEqualTo(orphanIds);
  }

}




