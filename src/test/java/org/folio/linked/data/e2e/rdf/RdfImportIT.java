package org.folio.linked.data.e2e.rdf;

import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.ld.dictionary.ResourceTypeDictionary.CONCEPT;
import static org.folio.ld.dictionary.ResourceTypeDictionary.FAMILY;
import static org.folio.ld.dictionary.ResourceTypeDictionary.FORM;
import static org.folio.ld.dictionary.ResourceTypeDictionary.ID_LCNAF;
import static org.folio.ld.dictionary.ResourceTypeDictionary.INSTANCE;
import static org.folio.ld.dictionary.ResourceTypeDictionary.MEETING;
import static org.folio.ld.dictionary.ResourceTypeDictionary.ORGANIZATION;
import static org.folio.ld.dictionary.ResourceTypeDictionary.PERSON;
import static org.folio.ld.dictionary.ResourceTypeDictionary.PLACE;
import static org.folio.ld.dictionary.ResourceTypeDictionary.TITLE;
import static org.folio.ld.dictionary.ResourceTypeDictionary.TOPIC;
import static org.folio.ld.dictionary.ResourceTypeDictionary.WORK;
import static org.folio.linked.data.test.TestUtil.TEST_JSON_MAPPER;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.folio.linked.data.test.TestUtil.awaitAndAssert;
import static org.folio.linked.data.test.TestUtil.defaultHeaders;
import static org.folio.linked.data.test.TestUtil.getJsonNode;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Map;
import org.folio.ld.dictionary.PropertyDictionary;
import org.folio.ld.dictionary.ResourceTypeDictionary;
import org.folio.linked.data.e2e.base.ITBase;
import org.folio.linked.data.e2e.base.IntegrationTest;
import org.folio.linked.data.model.entity.FolioMetadata;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceTypeEntity;
import org.folio.linked.data.model.entity.event.ResourceCreatedEvent;
import org.folio.linked.data.service.ResourceModificationEventListener;
import org.folio.linked.data.test.kafka.KafkaProducerTestConfiguration;
import org.folio.linked.data.test.kafka.KafkaSearchWorkIndexTopicListener;
import org.junit.jupiter.api.BeforeEach;
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
  private static final String EXPORT_ENDPOINT = "/linked-data/resource/{id}/rdf";
  @Autowired
  private KafkaSearchWorkIndexTopicListener workIndexTopicListener;
  @MockitoSpyBean
  private ResourceModificationEventListener eventListener;

  @BeforeEach
  @Override
  public void beforeEach() {
    super.beforeEach();
    workIndexTopicListener.getMessages().clear();
  }

  @Test
  void rdfImport_shouldSaveImportedResourceAndSendEventAndReturnId() throws Exception {
    // given
    var fileName = "instance.json";
    var input = this.getClass().getResourceAsStream("/rdf/" + fileName);
    var multipartFile = new MockMultipartFile("fileName", fileName,
      "application/ld+json", input);
    var requestBuilder = MockMvcRequestBuilders.multipart(IMPORT_ENDPOINT)
      .file(multipartFile)
      .headers(defaultHeaders(env))
      .param("filterType", "");

    // when
    var resultActions = mockMvc.perform(requestBuilder);

    // then
    var response = resultActions
      .andExpect(status().isOk())
      .andReturn().getResponse().getContentAsString();
    var resourceId = TEST_JSON_MAPPER.readTree(response).path("resources").get(0).asLong();
    resultActions
      .andExpect(jsonPath("resources[0]", equalTo(Long.toString(resourceId))))
      .andExpect(jsonPath("log", containsString("ID;TYPE;LABEL;STATUS;FAILURE_REASON")))
      .andExpect(jsonPath("log", containsString(Long.toString(resourceId) + ";Instance;")))
      .andExpect(jsonPath("log", containsString(";Created;")));
    assertThat(resourceTestService.existsById(resourceId)).isTrue();

    var expectedEvents = List.of(
      new ResourceTypeAndLabel(INSTANCE, "Title mainTitle Title subtitle"),
      new ResourceTypeAndLabel(TITLE, "Title mainTitle Title subtitle"),
      new ResourceTypeAndLabel(WORK, "Title mainTitle"),
      new ResourceTypeAndLabel(TITLE, "Title mainTitle")
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
      .headers(defaultHeaders(env))
      .param("filterType", "");
    var expectedId = -6960648989710467939L;
    var existedAuthority = new Resource()
      .setIdAndRefreshEdges(123L)
      .setLabel("n2021004098")
      .setDoc(getJsonNode(Map.of(PropertyDictionary.NAME.getValue(), List.of("name"))))
      .addType(new ResourceTypeEntity(PERSON.getHash(), PERSON.getUri(), ""));
    existedAuthority
      .setFolioMetadata(new FolioMetadata(existedAuthority).setInventoryId("inv_id_00000000"));
    resourceTestService.saveGraph(existedAuthority);

    // when
    var resultActions = mockMvc.perform(requestBuilder);

    // then
    resultActions
      .andExpect(status().isOk())
      .andExpect(jsonPath("resources[0]", equalTo(Long.toString(expectedId))))
      .andExpect(jsonPath("log", containsString(Long.toString(expectedId))));
    assertThat(resourceTestService.existsById(expectedId)).isTrue();

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
      .headers(defaultHeaders(env))
      .param("filterType", "");
    var expectedId = -6960648989710467939L;

    // when
    var resultActions = mockMvc.perform(requestBuilder);

    // then
    resultActions
      .andExpect(status().isOk())
      .andExpect(jsonPath("resources[0]", equalTo(Long.toString(expectedId))))
      .andExpect(jsonPath("log", containsString(Long.toString(expectedId))));
    assertThat(resourceTestService.existsById(expectedId)).isTrue();

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

  @Test
  void rdfImport_shouldIncludeBothInstancesInWorkSearchIndexMessage_whenSecondInstanceAddedToExistingWorkWithInstance()
    throws Exception {
    // given
    var instance1File = new MockMultipartFile(
      "fileName", "instance1_same_work.json", "application/ld+json",
      getClass().getResourceAsStream("/rdf/instance1_same_work.json"));
    var importResponse1 = mockMvc.perform(MockMvcRequestBuilders.multipart(IMPORT_ENDPOINT)
        .file(instance1File)
        .headers(defaultHeaders(env))
        .param("filterType", ""))
      .andExpect(status().isOk())
      .andReturn().getResponse().getContentAsString();
    var instance1Id = TEST_JSON_MAPPER.readTree(importResponse1).path("resources").get(0).asLong();
    workIndexTopicListener.getMessages().clear();

    // when
    var instance2File = new MockMultipartFile(
      "fileName", "instance2_same_work.json", "application/ld+json",
      getClass().getResourceAsStream("/rdf/instance2_same_work.json"));
    var importResponse2 = mockMvc.perform(MockMvcRequestBuilders.multipart(IMPORT_ENDPOINT)
        .file(instance2File)
        .headers(defaultHeaders(env))
        .param("filterType", ""))
      .andExpect(status().isOk())
      .andReturn().getResponse().getContentAsString();
    var instance2Id = TEST_JSON_MAPPER.readTree(importResponse2).path("resources").get(0).asLong();

    // then
    awaitAndAssert(() ->
      assertTrue(
        workIndexTopicListener.getMessages().stream().anyMatch(m ->
          m.contains(String.valueOf(instance1Id))
            && m.contains(String.valueOf(instance2Id))
            && m.contains("\"titles\"")
        )
      )
    );
  }

  @Test
  void rdfImport_shouldBeFailedByConstraint_whenImportSameInstanceWithAnotherWork()
    throws Exception {
    // given
    var instance1File = new MockMultipartFile(
      "fileName", "instance1_work1.json", "application/ld+json",
      getClass().getResourceAsStream("/rdf/instance1_work1.json"));
    mockMvc.perform(MockMvcRequestBuilders.multipart(IMPORT_ENDPOINT)
        .file(instance1File)
        .headers(defaultHeaders(env))
        .param("filterType", ""))
      .andExpect(status().isOk());
    workIndexTopicListener.getMessages().clear();

    // when
    var instance2File = new MockMultipartFile(
      "fileName", "instance1_work1_to_work2.json", "application/ld+json",
      getClass().getResourceAsStream("/rdf/instance1_work1_to_work2.json"));
    var importResponse2 = mockMvc.perform(MockMvcRequestBuilders.multipart(IMPORT_ENDPOINT)
        .file(instance2File)
        .headers(defaultHeaders(env))
        .param("filterType", ""));


    // then
    importResponse2
      .andExpect(status().isOk())
      .andExpect(jsonPath("log", containsString("duplicate key value violates unique constraint")));
  }

  @Test
  void rdfImport_shouldReturnEmptyResourcesAndHeaderOnlyLog_whenEmptyJsonFileSubmitted() throws Exception {
    // given
    var multipartFile = new MockMultipartFile("fileName", "empty.json",
      "application/ld+json", "[]".getBytes());
    var requestBuilder = MockMvcRequestBuilders.multipart(IMPORT_ENDPOINT)
      .file(multipartFile)
      .headers(defaultHeaders(env))
      .param("filterType", "");

    // when
    var resultActions = mockMvc.perform(requestBuilder);

    // then
    resultActions
      .andExpect(status().isOk())
      .andExpect(jsonPath("resources").doesNotExist())
      .andExpect(jsonPath("log", equalTo("ID;TYPE;LABEL;STATUS;FAILURE_REASON\n")));
    verify(eventListener, times(0)).afterCreate(any());
  }

  @Test
  void rdfImport_shouldSaveAllInstancesAndReturnCsvLog_whenFileContainsWorkWithMultipleInstances() throws Exception {
    // given
    var fileName = "work_with_multiple_instances.json";
    var input = this.getClass().getResourceAsStream("/rdf/" + fileName);
    var multipartFile = new MockMultipartFile("fileName", fileName, "application/ld+json", input);
    var requestBuilder = MockMvcRequestBuilders.multipart(IMPORT_ENDPOINT)
      .file(multipartFile)
      .headers(defaultHeaders(env))
      .param("filterType", "");

    // when
    var resultActions = mockMvc.perform(requestBuilder);

    // then
    var response = resultActions
      .andExpect(status().isOk())
      .andReturn().getResponse().getContentAsString();
    var resourcesNode = TEST_JSON_MAPPER.readTree(response).path("resources");
    assertThat(resourcesNode.size()).isEqualTo(2);
    var instance1Id = resourcesNode.get(0).asLong();
    var instance2Id = resourcesNode.get(1).asLong();

    resultActions
      .andExpect(jsonPath("resources", hasSize(2)))
      .andExpect(jsonPath("log", containsString("ID;TYPE;LABEL;STATUS;FAILURE_REASON")))
      .andExpect(jsonPath("log", containsString(";Instance;Instance 1 Main Title;Created;")))
      .andExpect(jsonPath("log", containsString(";Instance;Instance 2 Main Title;Created;")));
    assertThat(resourceTestService.existsById(instance1Id)).isTrue();
    assertThat(resourceTestService.existsById(instance2Id)).isTrue();

    var expectedEvents = List.of(
      new ResourceTypeAndLabel(INSTANCE, "Instance 1 Main Title"),
      new ResourceTypeAndLabel(TITLE, "Instance 1 Main Title"),
      new ResourceTypeAndLabel(INSTANCE, "Instance 2 Main Title"),
      new ResourceTypeAndLabel(TITLE, "Instance 2 Main Title"),
      new ResourceTypeAndLabel(WORK, "Multi Work Main Title"),
      new ResourceTypeAndLabel(TITLE, "Multi Work Main Title")
    );
    assertEvents(expectedEvents);
  }

  @Test
  void rdfImport_shouldMaterializeConceptNodeBetweenWorkAndSubjectAuthority() throws Exception {
    // given
    var fileName = "instance_work_subjects.json";
    var input = this.getClass().getResourceAsStream("/rdf/" + fileName);
    var multipartFile = new MockMultipartFile("fileName", fileName, "application/ld+json", input);
    var requestBuilder = MockMvcRequestBuilders.multipart(IMPORT_ENDPOINT)
      .file(multipartFile)
      .headers(defaultHeaders(env))
      .param("filterType", "");

    // when
    var resultActions = mockMvc.perform(requestBuilder);

    // then
    var response = resultActions
      .andExpect(status().isOk())
      .andReturn().getResponse().getContentAsString();
    var resourceId = TEST_JSON_MAPPER.readTree(response).path("resources").get(0).asLong();
    assertThat(resourceTestService.existsById(resourceId)).isTrue();

    var expectedEvents = List.of(
      new ResourceTypeAndLabel(INSTANCE, "Subject Instance Title"),
      new ResourceTypeAndLabel(TITLE, "Subject Instance Title"),
      new ResourceTypeAndLabel(WORK, "Subject Work Title"),
      new ResourceTypeAndLabel(TITLE, "Subject Work Title"),
      new ResourceTypeAndLabel(CONCEPT, "Subject Person"),
      new ResourceTypeAndLabel(PERSON, "Subject Person"),
      new ResourceTypeAndLabel(CONCEPT, "Subject Topic"),
      new ResourceTypeAndLabel(TOPIC, "Subject Topic"),
      new ResourceTypeAndLabel(CONCEPT, "Subject Family"),
      new ResourceTypeAndLabel(FAMILY, "Subject Family"),
      new ResourceTypeAndLabel(CONCEPT, "Subject Organization"),
      new ResourceTypeAndLabel(ORGANIZATION, "Subject Organization"),
      new ResourceTypeAndLabel(CONCEPT, "Subject Meeting"),
      new ResourceTypeAndLabel(MEETING, "Subject Meeting"),
      new ResourceTypeAndLabel(CONCEPT, "Subject Place"),
      new ResourceTypeAndLabel(PLACE, "Subject Place"),
      new ResourceTypeAndLabel(CONCEPT, "Subject Form"),
      new ResourceTypeAndLabel(FORM, "Subject Form")
    );
    assertEvents(expectedEvents);
  }

  @Test
  void rdfImport_shouldSaveSerialInstanceAndExportWithSerialWorkType() throws Exception {
    // given - import phase
    var fileName = "instance_serial.json";
    var input = this.getClass().getResourceAsStream("/rdf/" + fileName);
    var multipartFile = new MockMultipartFile("fileName", fileName,
      "application/ld+json", input);
    var importRequestBuilder = MockMvcRequestBuilders.multipart(IMPORT_ENDPOINT)
      .file(multipartFile)
      .headers(defaultHeaders(env))
      .param("filterType", "");

    // when - import
    var importResultActions = mockMvc.perform(importRequestBuilder);

    // then - verify import
    var importResponse = importResultActions
      .andExpect(status().isOk())
      .andReturn().getResponse().getContentAsString();
    var resourceId = TEST_JSON_MAPPER.readTree(importResponse).path("resources").get(0).asLong();
    importResultActions
      .andExpect(jsonPath("resources[0]", equalTo(Long.toString(resourceId))))
      .andExpect(jsonPath("log", containsString(Long.toString(resourceId) + ";Instance;")))
      .andExpect(jsonPath("log", containsString(";Created;")));
    assertThat(resourceTestService.existsById(resourceId)).isTrue();

    var expectedEvents = List.of(
      new ResourceTypeAndLabel(INSTANCE, "Serial mainTitle Serial subtitle"),
      new ResourceTypeAndLabel(TITLE, "Serial mainTitle Serial subtitle"),
      new ResourceTypeAndLabel(WORK, "Serial mainTitle"),
      new ResourceTypeAndLabel(TITLE, "Serial mainTitle")
    );
    assertEvents(expectedEvents);

    // given - export phase (round-trip)
    var exportRequestBuilder = get(EXPORT_ENDPOINT.replace("{id}", Long.toString(resourceId)))
      .contentType(APPLICATION_JSON)
      .headers(defaultHeaders(env));

    // when - export
    var exportResultActions = mockMvc.perform(exportRequestBuilder);

    // then - verify Serial type survives round-trip
    var exportResponse = exportResultActions
      .andExpect(status().isOk())
      .andExpect(content().contentType(APPLICATION_JSON))
      .andReturn().getResponse().getContentAsString();
    assertThat(exportResponse).contains("http://id.loc.gov/ontologies/bibframe/Serial");
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
