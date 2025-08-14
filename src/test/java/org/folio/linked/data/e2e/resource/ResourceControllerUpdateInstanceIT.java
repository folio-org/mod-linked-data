package org.folio.linked.data.e2e.resource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.linked.data.e2e.resource.ResourceControllerITBase.RESOURCE_URL;
import static org.folio.linked.data.test.MonographTestUtil.getWork;
import static org.folio.linked.data.test.TestUtil.defaultHeaders;
import static org.folio.linked.data.test.TestUtil.readTree;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.folio.ld.dictionary.PredicateDictionary;
import org.folio.ld.dictionary.ResourceTypeDictionary;
import org.folio.linked.data.e2e.ITBase;
import org.folio.linked.data.e2e.base.IntegrationTest;
import org.folio.linked.data.model.entity.FolioMetadata;
import org.folio.linked.data.model.entity.RawMarc;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceEdge;
import org.folio.linked.data.repo.RawMarcRepository;
import org.folio.linked.data.test.kafka.KafkaProducerTestConfiguration;
import org.folio.linked.data.test.resource.ResourceTestService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@IntegrationTest
@SpringBootTest(classes = {KafkaProducerTestConfiguration.class})
class ResourceControllerUpdateInstanceIT extends ITBase {
  @Autowired
  private ResourceTestService resourceTestService;
  @Autowired
  private RawMarcRepository rawMarcRepository;
  @Autowired
  private ObjectMapper objectMapper;

  @Test
  void update_should_reject_if_instance_with_same_id_exists_and_connected_to_different_work() throws Exception {
    var instance1Title = "simple_instance1";
    var instance2Title = "simple_instance2";
    var work1 = getWork("simple_work1", hashService);
    var work2 = getWork("simple_work2", hashService);
    var work1Instance = getInstance(work1, instance1Title);
    var work2Instance = getInstance(work2, instance2Title);
    resourceTestService.saveGraph(work1Instance);
    resourceTestService.saveGraph(work2Instance);

    // when
    // Update Instance1's title to Instance2's title
    var instance1UpdateRequestDto = getInstanceRequestDto(work1.getId(), instance2Title);

    var updateRequest = put(RESOURCE_URL + "/" + work1Instance.getId())
      .contentType(APPLICATION_JSON)
      .headers(defaultHeaders(env))
      .content(instance1UpdateRequestDto);

    // then
    // Assert that the request is rejected
    mockMvc.perform(updateRequest)
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.errors[0].code").value("already_exists"));
  }

  @Test
  void update_should_succeed_if_instance_with_same_id_exists_and_connected_to_same_work() throws Exception {
    var instance1Title = "simple_instance1";
    var instance2Title = "simple_instance2";
    var work = getWork("simple_work1", hashService);
    var instance1 = getInstance(work, instance1Title);
    var instance2 = getInstance(work, instance2Title);
    resourceTestService.saveGraph(instance1);
    resourceTestService.saveGraph(instance2);

    // when
    // Update Instance1's title to Instance2's title
    var instance1UpdateRequestDto = getInstanceRequestDto(work.getId(), instance2Title);

    var updateRequest = put(RESOURCE_URL + "/" + instance1.getId())
      .contentType(APPLICATION_JSON)
      .headers(defaultHeaders(env))
      .content(instance1UpdateRequestDto);

    // then
    // Assert that the request is successful
    mockMvc.perform(updateRequest)
      .andExpect(status().isOk());
  }

  @Test
  void update_should_update_instance_when_fingerprint_changed() throws Exception {
    // given
    var instanceTitle = "simple_instance1";
    var work = getWork("simple_work1", hashService);
    var instance = getInstance(work, instanceTitle);
    resourceTestService.saveGraph(instance);

    // when
    var instance1UpdateRequestDto = getInstanceRequestDto(work.getId(), instanceTitle + "_updated");

    var updateRequest = put(RESOURCE_URL + "/" + instance.getId())
      .contentType(APPLICATION_JSON)
      .headers(defaultHeaders(env))
      .content(instance1UpdateRequestDto);

    // then
    // Assert that the request is successful
    mockMvc.perform(updateRequest)
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.resource['http://bibfra.me/vocab/lite/Instance'].profileId").value(3));
  }

  @Test
  void update_should_retain_unmapped_marc_records() throws Exception {
    // given
    var instanceTitle = "simple_instance1";
    var work = getWork("simple_work1", hashService);
    var instance = getInstance(work, instanceTitle);
    var savedInstance = resourceTestService.saveGraph(instance);
    var unmappedMarc = "{\"800\": \"unmapped_marc_content\"}";
    rawMarcRepository.save(new RawMarc(savedInstance).setContent(unmappedMarc));

    // when
    var updateRequestDto = getInstanceRequestDto(work.getId(), instanceTitle + "_updated");

    var updateRequest = put(RESOURCE_URL + "/" + instance.getId())
      .contentType(APPLICATION_JSON)
      .headers(defaultHeaders(env))
      .content(updateRequestDto);

    var postResponse = mockMvc.perform(updateRequest)
      .andExpect(status().isOk())
      .andReturn()
      .getResponse()
      .getContentAsString();

    var updatedInstanceId = getInstanceId(postResponse);

    // then
    var rawMarcOpt = rawMarcRepository.findById(updatedInstanceId);
    assertThat(rawMarcOpt).isPresent();
    assertThat(rawMarcOpt.get().getContent())
      .isEqualTo(unmappedMarc);
  }

  @SneakyThrows
  private long getInstanceId(String postResponse) {
    JsonNode rootNode = objectMapper.readTree(postResponse);
    return rootNode
      .path("resource")
      .path("http://bibfra.me/vocab/lite/Instance")
      .path("id")
      .asLong();
  }

  private Resource getInstance(Resource work, String titleStr) {
    var titleDoc = """
      {
        "http://bibfra.me/vocab/marc/mainTitle": ["%TITLE%"]
      }
      """
      .replace("%TITLE%", titleStr);
    var title = new Resource()
      .addTypes(ResourceTypeDictionary.TITLE)
      .setDoc(readTree(titleDoc))
      .setLabel(titleStr);
    var instance = new Resource()
      .addTypes(ResourceTypeDictionary.INSTANCE)
      .setDoc(readTree("{}"))
      .setLabel(titleStr);

    instance.addOutgoingEdge(new ResourceEdge(instance, title, PredicateDictionary.TITLE));
    instance.addOutgoingEdge(new ResourceEdge(instance, work, PredicateDictionary.INSTANTIATES));

    title.setId(hashService.hash(title));
    instance.setId(hashService.hash(instance));

    FolioMetadata metadata = new FolioMetadata(instance)
      .setInventoryId(titleStr + "_inventoryId").setSrsId(titleStr + "_srsId");
    return instance.setFolioMetadata(metadata);
  }

  private String getInstanceRequestDto(Long workId, String title) {
    return """
      {
        "resource": {
          "http://bibfra.me/vocab/lite/Instance": {
            "profileId":  3,
            "http://bibfra.me/vocab/marc/title": [
                {
                  "http://bibfra.me/vocab/marc/Title": {
                      "http://bibfra.me/vocab/marc/mainTitle": [ "%TITLE%" ]
                  }
                }
            ],
            "http://bibfra.me/vocab/marc/summary": ["new summary"],
            "_workReference": [ { "id": "%WORK_ID%"} ]
          }
        }
      }
      """
      .replace("%WORK_ID%", workId.toString())
      .replace("%TITLE%", title);
  }
}
