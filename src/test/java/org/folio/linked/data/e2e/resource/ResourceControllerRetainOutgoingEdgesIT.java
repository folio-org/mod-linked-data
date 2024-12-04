package org.folio.linked.data.e2e.resource;

import static org.folio.ld.dictionary.ResourceTypeDictionary.ANNOTATION;
import static org.folio.ld.dictionary.ResourceTypeDictionary.INSTANCE;
import static org.folio.linked.data.model.entity.ResourceSource.LINKED_DATA;
import static org.folio.linked.data.test.MonographTestUtil.createPrimaryTitle;
import static org.folio.linked.data.test.MonographTestUtil.createResource;
import static org.folio.linked.data.test.MonographTestUtil.getSampleWork;
import static org.folio.linked.data.test.TestUtil.INSTANCE_WITH_WORK_REF_SAMPLE;
import static org.folio.linked.data.test.TestUtil.OBJECT_MAPPER;
import static org.folio.linked.data.test.TestUtil.defaultHeaders;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.folio.ld.dictionary.PredicateDictionary;
import org.folio.linked.data.domain.dto.InstanceResponseField;
import org.folio.linked.data.domain.dto.ResourceResponseDto;
import org.folio.linked.data.e2e.base.IntegrationTest;
import org.folio.linked.data.model.entity.FolioMetadata;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceEdge;
import org.folio.linked.data.service.resource.hash.HashService;
import org.folio.linked.data.test.ResourceTestService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.test.web.servlet.MockMvc;

@IntegrationTest
class ResourceControllerRetainOutgoingEdgesIT {
  private static final String RESOURCE_URL = "/linked-data/resource";
  private static final String WORK_ID_PLACEHOLDER = "%WORK_ID%";

  @Autowired
  private ResourceTestService resourceTestService;
  @Autowired
  private Environment env;
  @Autowired
  private MockMvc mockMvc;
  @Autowired
  private HashService hashService;

  @Test
  void shouldRetainAdminMetadataOfInstanceAfterUpdate() throws Exception {
    // given
    var work = getSampleWork(null);
    setResourceIds(work);
    resourceTestService.saveGraph(work);

    var annotation = createResource(Map.of(), Set.of(ANNOTATION), Map.of());
    var instance = createResource(Map.of(), Set.of(INSTANCE),
      Map.of(
        PredicateDictionary.TITLE, List.of(createPrimaryTitle(1L)),
        PredicateDictionary.ADMIN_METADATA, List.of(annotation),
        PredicateDictionary.INSTANTIATES, List.of(work)
      )
    );
    var folioMetadata = new FolioMetadata(instance)
      .setSource(LINKED_DATA)
      .setInventoryId(UUID.randomUUID().toString());
    instance.setFolioMetadata(folioMetadata);
    setResourceIds(instance);
    resourceTestService.saveGraph(instance);

    // when
    var updatedResource = updateResource(instance.getId(),
      INSTANCE_WITH_WORK_REF_SAMPLE.replaceAll(WORK_ID_PLACEHOLDER, work.getId().toString()));
    var newInstanceId = ((InstanceResponseField) updatedResource.getResource()).getInstance().getId();

    // then
    assertAdminMetadataEdgeRetained(newInstanceId);
  }

  private void assertAdminMetadataEdgeRetained(String id) throws Exception {
    var resourceGraphUrl = String.format("%s/%s/graph", RESOURCE_URL, id);
    var requestBuilder = get(resourceGraphUrl)
      .contentType(APPLICATION_JSON)
      .headers(defaultHeaders(env));

    mockMvc.perform(requestBuilder)
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.outgoingEdges.edges['http://bibfra.me/vocab/marc/adminMetadata']").isArray())
      .andExpect(jsonPath("$.outgoingEdges.edges['http://bibfra.me/vocab/marc/adminMetadata'].length()").value(1));
  }


  private ResourceResponseDto updateResource(Long id, String payload) throws Exception {
    var updateRequest = put(RESOURCE_URL + "/" + id)
      .contentType(APPLICATION_JSON)
      .headers(defaultHeaders(env))
      .content(payload);
    var responseString = mockMvc.perform(updateRequest)
      .andExpect(status().isOk())
      .andReturn()
      .getResponse().getContentAsString();
    return OBJECT_MAPPER.readValue(responseString, ResourceResponseDto.class);
  }

  private void setResourceIds(Resource resource) {
    resource.setId(hashService.hash(resource));
    resource.getOutgoingEdges()
      .stream()
      .map(ResourceEdge::getTarget)
      .forEach(this::setResourceIds);
  }
}
