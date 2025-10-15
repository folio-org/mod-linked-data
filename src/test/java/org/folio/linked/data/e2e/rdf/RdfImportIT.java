package org.folio.linked.data.e2e.rdf;

import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.ld.dictionary.ResourceTypeDictionary.INSTANCE;
import static org.folio.ld.dictionary.ResourceTypeDictionary.PERSON;
import static org.folio.ld.dictionary.ResourceTypeDictionary.TITLE;
import static org.folio.ld.dictionary.ResourceTypeDictionary.WORK;
import static org.folio.linked.data.test.TestUtil.defaultHeaders;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Optional;
import org.folio.ld.dictionary.ResourceTypeDictionary;
import org.folio.ld.dictionary.model.Resource;
import org.folio.linked.data.e2e.base.IntegrationTest;
import org.folio.linked.data.integration.ResourceModificationEventListener;
import org.folio.linked.data.model.entity.event.ResourceCreatedEvent;
import org.folio.linked.data.repo.ResourceRepository;
import org.folio.linked.data.service.rdf.LccnResourceProvider;
import org.folio.linked.data.test.kafka.KafkaProducerTestConfiguration;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@IntegrationTest
@SpringBootTest(classes = {KafkaProducerTestConfiguration.class})
class RdfImportIT {
  private static final String IMPORT_ENDPOINT = "/linked-data/import/file";
  @Autowired
  private MockMvc mockMvc;
  @Autowired
  private Environment env;
  @Autowired
  private ResourceRepository resourceRepo;
  @MockitoSpyBean
  private ResourceModificationEventListener eventListener;
  @MockitoBean
  private LccnResourceProvider lccnResourceProvider;

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
  void rdfImport_shouldFetchAuthorityByLccn() throws Exception {
    // given
    var fileName = "instance_work_agents_ids.json";
    var input = this.getClass().getResourceAsStream("/rdf/" + fileName);
    var multipartFile = new MockMultipartFile("fileName", fileName,
      "application/ld+json", input);
    var requestBuilder = MockMvcRequestBuilders.multipart(IMPORT_ENDPOINT)
      .file(multipartFile)
      .headers(defaultHeaders(env));
    var expectedId = -2124728439381748732L;
    var lccn = "n2021004098";
    var existedAuthority = new Resource().setId(123L).setLabel(lccn).addType(PERSON);
    when(lccnResourceProvider.apply(lccn)).thenReturn(Optional.of(existedAuthority));

    // when
    var resultActions = mockMvc.perform(requestBuilder);

    // then
    resultActions
      .andExpect(status().isOk())
      .andExpect(jsonPath("resources[0]", equalTo(Long.toString(expectedId))))
      .andExpect(jsonPath("log", containsString(Long.toString(expectedId))));
    assertThat(resourceRepo.existsById(expectedId)).isTrue();
    assertThat(resourceRepo.existsById(existedAuthority.getId())).isTrue();

    var expectedEvents = List.of(
      new ResourceTypeAndLabel(WORK, "Work Title"),
      new ResourceTypeAndLabel(INSTANCE, "Instance Title"),
      new ResourceTypeAndLabel(TITLE, "Instance Title"),
      new ResourceTypeAndLabel(TITLE, "Work Title"),
      new ResourceTypeAndLabel(PERSON, lccn)
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

  record ResourceTypeAndLabel(ResourceTypeDictionary type, String label) {}
}
