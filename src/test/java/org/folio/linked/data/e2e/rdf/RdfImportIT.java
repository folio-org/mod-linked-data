package org.folio.linked.data.e2e.rdf;

import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.ld.dictionary.ResourceTypeDictionary.ID_LCNAF;
import static org.folio.ld.dictionary.ResourceTypeDictionary.INSTANCE;
import static org.folio.ld.dictionary.ResourceTypeDictionary.PERSON;
import static org.folio.ld.dictionary.ResourceTypeDictionary.TITLE;
import static org.folio.ld.dictionary.ResourceTypeDictionary.WORK;
import static org.folio.linked.data.test.TestUtil.defaultHeaders;
import static org.folio.linked.data.test.TestUtil.getJsonNode;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Map;
import org.folio.ld.dictionary.PropertyDictionary;
import org.folio.ld.dictionary.ResourceTypeDictionary;
import org.folio.linked.data.e2e.ITBase;
import org.folio.linked.data.e2e.base.IntegrationTest;
import org.folio.linked.data.model.entity.FolioMetadata;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceTypeEntity;
import org.folio.linked.data.model.entity.event.ResourceCreatedEvent;
import org.folio.linked.data.repo.ResourceRepository;
import org.folio.linked.data.service.ResourceModificationEventListener;
import org.folio.linked.data.test.kafka.KafkaProducerTestConfiguration;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@IntegrationTest
@SpringBootTest(classes = {KafkaProducerTestConfiguration.class})
class RdfImportIT extends ITBase {
  private static final String IMPORT_ENDPOINT = "/linked-data/import/file";
  @Autowired
  private ResourceRepository resourceRepo;
  @MockitoSpyBean
  private ResourceModificationEventListener eventListener;

  @Test
  void rdfImport_shouldSaveImportedResourceAndSendEventAndReturnId() throws Exception {
    // given
    var fileName = "instance.json";
    var input = this.getClass().getResourceAsStream("/rdf/" + fileName);
    var multipartFile = new MockMultipartFile("fileName", fileName,
      "application/ld+json", input);
    var requestBuilder = MockMvcRequestBuilders.multipart(IMPORT_ENDPOINT)
      .file(multipartFile)
      .headers(defaultHeaders(env));
    var expectedId = -6100541528157108314L;

    // when
    var resultActions = mockMvc.perform(requestBuilder);

    // then
    resultActions
      .andExpect(status().isOk())
      .andExpect(jsonPath("resources[0]", equalTo(Long.toString(expectedId))))
      .andExpect(jsonPath("log", containsString(Long.toString(expectedId))));
    assertThat(resourceRepo.existsById(expectedId)).isTrue();

    var expectedEvents = List.of(
      new ResourceTypeAndLabel(INSTANCE, "Title mainTitle, Title subtitle"),
      new ResourceTypeAndLabel(TITLE, "Title mainTitle, Title subtitle")
    );
    assertEvents(expectedEvents);
  }

  @Test
  void rdfImport_shouldFetchAuthorityByLccnFromGraph() throws Exception {
    // given
    var fileName = "instance_work_agents_ids.json";
    var input = this.getClass().getResourceAsStream("/rdf/" + fileName);
    var multipartFile = new MockMultipartFile("fileName", fileName,
      "application/ld+json", input);
    var requestBuilder = MockMvcRequestBuilders.multipart(IMPORT_ENDPOINT)
      .file(multipartFile)
      .headers(defaultHeaders(env));
    var expectedId = -2124728439381748732L;
    var existedAuthority = new Resource()
      .setIdAndRefreshEdges(123L)
      .setLabel("n2021004098")
      .setDoc(getJsonNode(Map.of(PropertyDictionary.NAME.getValue(), List.of("name"))))
      .addType(new ResourceTypeEntity(PERSON.getHash(), PERSON.getUri(), ""));
    existedAuthority
      .setFolioMetadata(new FolioMetadata(existedAuthority).setInventoryId("inv_id_00000000"));
    resourceRepo.save(existedAuthority);

    // when
    var resultActions = mockMvc.perform(requestBuilder);

    // then
    resultActions
      .andExpect(status().isOk())
      .andExpect(jsonPath("resources[0]", equalTo(Long.toString(expectedId))))
      .andExpect(jsonPath("log", containsString(Long.toString(expectedId))));
    assertThat(resourceRepo.existsById(expectedId)).isTrue();

    var expectedEvents = List.of(
      new ResourceTypeAndLabel(WORK, "Work Title"),
      new ResourceTypeAndLabel(INSTANCE, "Instance Title"),
      new ResourceTypeAndLabel(TITLE, "Instance Title"),
      new ResourceTypeAndLabel(TITLE, "Work Title")
    );
    assertEvents(expectedEvents);
  }

  @Test
  void rdfImport_shouldFetchAuthorityByLccnFromSrs() throws Exception {
    // given
    var fileName = "instance_work_agents_ids.json";
    var input = this.getClass().getResourceAsStream("/rdf/" + fileName);
    var multipartFile = new MockMultipartFile("fileName", fileName,
      "application/ld+json", input);
    var requestBuilder = MockMvcRequestBuilders.multipart(IMPORT_ENDPOINT)
      .file(multipartFile)
      .headers(defaultHeaders(env));
    var expectedId = -2124728439381748732L;

    // when
    var resultActions = mockMvc.perform(requestBuilder);

    // then
    resultActions
      .andExpect(status().isOk())
      .andExpect(jsonPath("resources[0]", equalTo(Long.toString(expectedId))))
      .andExpect(jsonPath("log", containsString(Long.toString(expectedId))));
    assertThat(resourceRepo.existsById(expectedId)).isTrue();

    var expectedEvents = List.of(
      new ResourceTypeAndLabel(WORK, "Work Title"),
      new ResourceTypeAndLabel(INSTANCE, "Instance Title"),
      new ResourceTypeAndLabel(TITLE, "Instance Title"),
      new ResourceTypeAndLabel(TITLE, "Work Title"),
      new ResourceTypeAndLabel(PERSON, "Lccn resource fetched from SRS"),
      new ResourceTypeAndLabel(ID_LCNAF, "n2021004098")
    );
    assertEvents(expectedEvents);
  }

  private void assertEvents(List<ResourceTypeAndLabel> expectedEvents) {
    var captor = ArgumentCaptor.forClass(ResourceCreatedEvent.class);
    verify(eventListener, times(expectedEvents.size())).afterCreate(captor.capture());

    var allEventsReceived = captor.getAllValues()
      .stream()
      .allMatch(event ->
        expectedEvents
          .stream()
          .anyMatch(e -> event.resource().isOfType(e.type())
            && event.resource().getLabel().equals(e.label()))
      );
    assertThat(allEventsReceived).isTrue();
  }

  record ResourceTypeAndLabel(ResourceTypeDictionary type, String label) {
  }
}
