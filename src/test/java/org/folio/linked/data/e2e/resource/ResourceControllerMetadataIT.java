package org.folio.linked.data.e2e.resource;

import static org.folio.ld.dictionary.PropertyDictionary.LINK;
import static org.folio.ld.dictionary.PropertyDictionary.TERM;
import static org.folio.ld.dictionary.ResourceTypeDictionary.WORK;
import static org.folio.linked.data.e2e.resource.ResourceControllerITBase.INSTANCE_ID_PLACEHOLDER;
import static org.folio.linked.data.e2e.resource.ResourceControllerITBase.RESOURCE_URL;
import static org.folio.linked.data.test.MonographTestUtil.getSampleInstanceResource;
import static org.folio.linked.data.test.TestUtil.OBJECT_MAPPER;
import static org.folio.linked.data.test.TestUtil.WORK_WITH_INSTANCE_REF_SAMPLE;
import static org.folio.linked.data.test.TestUtil.assertResourceMetadata;
import static org.folio.linked.data.test.TestUtil.defaultHeadersWithUserId;
import static org.folio.linked.data.test.TestUtil.getSampleWorkDtoMap;
import static org.folio.linked.data.test.resource.ResourceUtils.setExistingResourcesIds;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.folio.ld.dictionary.PropertyDictionary;
import org.folio.linked.data.domain.dto.ResourceResponseDto;
import org.folio.linked.data.domain.dto.WorkResponseField;
import org.folio.linked.data.e2e.base.IntegrationTest;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.test.kafka.KafkaProducerTestConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@IntegrationTest
@SpringBootTest(classes = {KafkaProducerTestConfiguration.class})
public class ResourceControllerMetadataIT extends AbstractResourceControllerIT {

  private static final UUID USER_ID = UUID.randomUUID();

  @Test
  void createWorkWithInstanceRef_shouldSaveEntityCorrectly() throws Exception {
    // given
    var instanceForReference = getSampleInstanceResource(null, null);
    setExistingResourcesIds(instanceForReference, hashService);
    resourceTestService.saveGraph(instanceForReference);
    var requestBuilder = post(RESOURCE_URL)
      .contentType(APPLICATION_JSON)
      .headers(defaultHeadersWithUserId(env, USER_ID.toString()))
      .content(
        WORK_WITH_INSTANCE_REF_SAMPLE.replaceAll(INSTANCE_ID_PLACEHOLDER, instanceForReference.getId().toString())
      );

    // when
    var resultActions = mockMvc.perform(requestBuilder);

    // then
    var response = resultActions
      .andExpect(status().isOk())
      .andExpect(content().contentType(APPLICATION_JSON))
      .andReturn().getResponse().getContentAsString();

    var resourceResponse = OBJECT_MAPPER.readValue(response, ResourceResponseDto.class);
    var id = ((WorkResponseField) resourceResponse.getResource()).getWork().getId();
    var workResource = resourceTestService.getResourceById(id, 4);
    assertResourceMetadata(workResource, USER_ID, null);
  }

  @Test
  void update_shouldReturnCorrectlyUpdateMetadataFields() throws Exception {
    // given
    var instanceForReference = getSampleInstanceResource(null, null);
    setExistingResourcesIds(instanceForReference, hashService);
    resourceTestService.saveGraph(instanceForReference);
    var requestBuilder = post(RESOURCE_URL)
      .contentType(APPLICATION_JSON)
      .headers(defaultHeadersWithUserId(env, USER_ID.toString()))
      .content(
        WORK_WITH_INSTANCE_REF_SAMPLE.replaceAll(INSTANCE_ID_PLACEHOLDER, instanceForReference.getId().toString())
      );

    var response = mockMvc.perform(requestBuilder)
      .andExpect(status().isOk())
      .andReturn().getResponse().getContentAsString();
    var resourceResponse = OBJECT_MAPPER.readValue(response, ResourceResponseDto.class);
    var originalWorkId = ((WorkResponseField) resourceResponse.getResource()).getWork().getId();
    var originalWorkResource = resourceTestService.getResourceById(originalWorkId, 4);


    var updateDto = getSampleWorkDtoMap();
    var workMap = (LinkedHashMap) ((LinkedHashMap) updateDto.get("resource")).get(WORK.getUri());
    workMap.put(PropertyDictionary.LANGUAGE.getValue(),
      Map.of(
        LINK.getValue(), List.of("http://id.loc.gov/vocabulary/languages/eng"),
        TERM.getValue(), List.of("English")
      ));
    var updatedById = UUID.randomUUID();

    // when
    var updateRequest = put(RESOURCE_URL + "/" + originalWorkId)
      .contentType(APPLICATION_JSON)
      .headers(defaultHeadersWithUserId(env, updatedById.toString()))
      .content(OBJECT_MAPPER.writeValueAsString(updateDto)
        .replaceAll(INSTANCE_ID_PLACEHOLDER, instanceForReference.getId().toString())
      );

    // then
    var updatedResponse = mockMvc.perform(updateRequest).andReturn().getResponse().getContentAsString();
    var updatedResourceResponse = OBJECT_MAPPER.readValue(updatedResponse, ResourceResponseDto.class);
    var updatedWorkId = ((WorkResponseField) updatedResourceResponse.getResource()).getWork().getId();
    var updatedWorkResource = resourceTestService.getResourceById(updatedWorkId, 4);
    compareResourceMetadataOfOriginalAndUpdated(originalWorkResource, updatedWorkResource, updatedById);
  }

  private void compareResourceMetadataOfOriginalAndUpdated(Resource original, Resource updated, UUID updatedById) {
    assertEquals(USER_ID, updated.getCreatedBy());
    assertEquals(updatedById, updated.getUpdatedBy());
    assertTrue(updated.getUpdatedDate().after(original.getUpdatedDate()));
    assertEquals(original.getCreatedDate(), updated.getCreatedDate());
    assertEquals(original.getCreatedBy(), updated.getCreatedBy());
    assertNull(original.getUpdatedBy());
    assertEquals(1, updated.getVersion() - original.getVersion());
  }
}
