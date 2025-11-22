package org.folio.linked.data.e2e.rdf;

import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.ld.dictionary.PropertyDictionary.CONTROL_NUMBER;
import static org.folio.ld.dictionary.PropertyDictionary.CREATED_DATE;
import static org.folio.ld.dictionary.PropertyDictionary.FOLIO_INVENTORY_ID;
import static org.folio.ld.dictionary.PropertyDictionary.MAIN_TITLE;
import static org.folio.ld.dictionary.ResourceTypeDictionary.INSTANCE;
import static org.folio.linked.data.test.TestUtil.STANDALONE_TEST_PROFILE;
import static org.folio.linked.data.test.TestUtil.cleanResourceTables;
import static org.folio.linked.data.test.TestUtil.readTree;
import static org.folio.linked.data.util.Constants.STANDALONE_PROFILE;

import java.util.Set;
import java.util.UUID;
import org.folio.ld.dictionary.PredicateDictionary;
import org.folio.ld.dictionary.ResourceTypeDictionary;
import org.folio.linked.data.e2e.base.IntegrationTest;
import org.folio.linked.data.mapper.dto.ResourceSubgraphViewMapper;
import org.folio.linked.data.model.entity.FolioMetadata;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceEdge;
import org.folio.linked.data.repo.ResourceSubgraphViewRepository;
import org.folio.linked.data.service.resource.hash.HashService;
import org.folio.linked.data.test.resource.ResourceTestService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

@IntegrationTest
@ActiveProfiles({STANDALONE_PROFILE, STANDALONE_TEST_PROFILE})
class ResourceSubgraphViewIT {
  @Autowired
  private ResourceSubgraphViewRepository resourceSubgraphViewRepository;
  @Autowired
  private ResourceTestService resourceTestService;
  @Autowired
  protected HashService hashService;
  @Autowired
  private JdbcTemplate jdbcTemplate;

  private ResourceSubgraphViewMapper mapper;

  @BeforeEach
  void beforeEach() {
    cleanResourceTables(jdbcTemplate);
    mapper = new ResourceSubgraphViewMapper();
  }

  // Check the overall set of Postgres functions related to exporting a resource subgraph.
  @Test
  void exportedResourceContainsInventoryId() {
    // given
    var titleStr = "Title";
    var hrid = "in0001234";
    var inventoryId = createInventoriedInstance(titleStr, hrid);

    // when
    var exported = resourceSubgraphViewRepository.findByInventoryIdIn(Set.of(inventoryId));

    // then
    assertThat(exported).isNotEmpty();
    var instance = mapper.fromJson(exported.iterator().next().getResourceSubgraph());
    assertThat(instance).isPresent();
    assertThat(instance.get().getLabel()).isEqualTo(titleStr);
    assertThat(instance.get().getOutgoingEdges()).hasSize(2);
    var amEdge = instance.get().getOutgoingEdges()
      .stream()
      .filter(edge -> edge.getPredicate().equals(PredicateDictionary.ADMIN_METADATA))
      .findFirst();
    assertThat(amEdge).isPresent();
    var props = amEdge.get().getTarget().getDoc();
    assertThat(props).isNotNull();
    assertThat(props.has(FOLIO_INVENTORY_ID.getValue())).isTrue();
    assertThat(props.get(FOLIO_INVENTORY_ID.getValue()).get(0).textValue()).isEqualTo(inventoryId);
    assertThat(props.has(CONTROL_NUMBER.getValue())).isTrue();
    assertThat(props.get(CONTROL_NUMBER.getValue()).get(0).textValue()).isEqualTo(hrid);
  }

  private String createInventoriedInstance(String titleStr, String hrid) {
    var titleDoc = """
      {
        "%mainTitle%": ["%TITLE%"]
      }
      """
      .replace("%mainTitle%", MAIN_TITLE.getValue())
      .replace("%TITLE%", titleStr);
    var title = new Resource()
      .addTypes(ResourceTypeDictionary.TITLE)
      .setDoc(readTree(titleDoc))
      .setLabel(titleStr);
    
    var adminMetadataDoc = """
        {
          "%controlNumber%": ["%HRID%"],
          "%createdDate%": ["2025-11-21"]
        }
        """
        .replace("%controlNumber%", CONTROL_NUMBER.getValue())
        .replace("%HRID%", hrid)
        .replace("%createdDate%", CREATED_DATE.getValue());
    var adminMetadata = new Resource()
      .addTypes(ResourceTypeDictionary.ANNOTATION)
      .setDoc(readTree(adminMetadataDoc))
      .setLabel(hrid);

    var inventoryId = UUID.randomUUID().toString();
    var resource = new Resource()
      .addTypes(INSTANCE)
      .setDoc(readTree("{}"))
      .setLabel(titleStr)
      .setIdAndRefreshEdges(456L);
    resource.setFolioMetadata(new FolioMetadata(resource).setInventoryId(inventoryId));
    resource.addOutgoingEdge(new ResourceEdge(resource, title, PredicateDictionary.TITLE));
    resource.addOutgoingEdge(new ResourceEdge(resource, adminMetadata, PredicateDictionary.ADMIN_METADATA));
    title.setIdAndRefreshEdges(hashService.hash(title));
    adminMetadata.setIdAndRefreshEdges(hashService.hash(adminMetadata));
    resourceTestService.saveGraph(resource);

    return inventoryId;
  }
}
