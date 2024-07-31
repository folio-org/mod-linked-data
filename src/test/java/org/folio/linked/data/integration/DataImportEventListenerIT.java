package org.folio.linked.data.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.ld.dictionary.PredicateDictionary.INSTANTIATES;
import static org.folio.ld.dictionary.ResourceTypeDictionary.CONCEPT;
import static org.folio.ld.dictionary.ResourceTypeDictionary.INSTANCE;
import static org.folio.ld.dictionary.ResourceTypeDictionary.PERSON;
import static org.folio.linked.data.model.entity.ResourceSource.MARC;
import static org.folio.linked.data.test.TestUtil.FOLIO_TEST_PROFILE;
import static org.folio.linked.data.test.TestUtil.OBJECT_MAPPER;
import static org.folio.linked.data.test.TestUtil.TENANT_ID;
import static org.folio.linked.data.test.TestUtil.awaitAndAssert;
import static org.folio.linked.data.test.TestUtil.defaultKafkaHeaders;
import static org.folio.linked.data.test.TestUtil.loadResourceAsString;
import static org.folio.linked.data.test.kafka.KafkaEventsTestDataFixture.authorityEvent;
import static org.folio.linked.data.test.kafka.KafkaEventsTestDataFixture.instanceCreatedEvent;
import static org.folio.linked.data.util.Constants.FOLIO_PROFILE;
import static org.folio.search.domain.dto.ResourceIndexEventType.UPDATE;
import static org.folio.spring.tools.config.properties.FolioEnvironment.getFolioEnvName;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.Objects;
import java.util.Set;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.folio.linked.data.e2e.base.IntegrationTest;
import org.folio.linked.data.integration.kafka.consumer.DataImportEventHandler;
import org.folio.linked.data.mapper.ResourceModelMapper;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceEdge;
import org.folio.linked.data.repo.ResourceEdgeRepository;
import org.folio.linked.data.repo.ResourceRepository;
import org.folio.linked.data.service.ResourceService;
import org.folio.linked.data.service.impl.tenant.TenantScopedExecutionService;
import org.folio.linked.data.test.kafka.KafkaSearchAuthorityAuthorityTopicListener;
import org.folio.linked.data.test.kafka.KafkaSearchWorkIndexTopicListener;
import org.folio.marc4ld.service.marc2ld.bib.MarcBib2ldMapper;
import org.folio.search.domain.dto.DataImportEvent;
import org.folio.search.domain.dto.InstanceIngressEvent;
import org.folio.spring.tools.kafka.FolioMessageProducer;
import org.folio.spring.tools.kafka.KafkaAdminService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@IntegrationTest
@ActiveProfiles({FOLIO_PROFILE, FOLIO_TEST_PROFILE})
class DataImportEventListenerIT {

  private static final String DI_COMPLETED_TOPIC = "DI_COMPLETED";
  private static final String EVENT_ID_01 = "event_id_01";

  @Autowired
  private ResourceRepository resourceRepo;
  @Autowired
  private ResourceEdgeRepository resourceEdgeRepository;
  @Autowired
  private KafkaTemplate<String, String> eventKafkaTemplate;
  @SpyBean
  @Autowired
  private DataImportEventHandler dataImportEventHandler;
  @Autowired
  private TenantScopedExecutionService tenantScopedExecutionService;
  @Autowired
  private KafkaSearchAuthorityAuthorityTopicListener kafkaSearchAuthorityAuthorityTopicListener;
  @Autowired
  private KafkaSearchWorkIndexTopicListener kafkaSearchWorkIndexTopicListener;
  @SpyBean
  @Autowired
  private ResourceService resourceService;
  @MockBean
  private FolioMessageProducer<InstanceIngressEvent> instanceIngressMessageProducer;
  @Autowired
  private MarcBib2ldMapper marc2BibframeMapper;
  @Autowired
  private ResourceModelMapper resourceModelMapper;

  @BeforeAll
  static void beforeAll(@Autowired KafkaAdminService kafkaAdminService) {
    kafkaAdminService.createTopics(TENANT_ID);
  }

  private static String getTopicName(String tenantId, String topic) {
    return String.format("%s.%s.%s", getFolioEnvName(), tenantId, topic);
  }

  @BeforeEach
  public void clean() {
    resourceEdgeRepository.deleteAll();
    resourceRepo.deleteAll();
    kafkaSearchAuthorityAuthorityTopicListener.getMessages().clear();
    kafkaSearchWorkIndexTopicListener.getMessages().clear();
  }

  @ParameterizedTest
  @CsvSource({
    "samples/marc_non_monograph_leader.jsonl, 0",
    "samples/marc_monograph_leader.jsonl, 1"
  })
  void shouldNotProcessEventForNullableResource(String resource, int interactions) {
    // given
    var marc = loadResourceAsString(resource);
    var emittedEvent = instanceCreatedEvent(EVENT_ID_01, TENANT_ID, marc);
    var expectedEvent = newMarcBibDataImportEvent(marc);

    // when
    eventKafkaTemplate.send(newProducerRecord(emittedEvent));

    // then
    awaitAndAssert(() -> verify(dataImportEventHandler).handle(expectedEvent));
    verify(resourceService, times(interactions)).createResource(any(org.folio.ld.dictionary.model.Resource.class));
  }

  @Transactional
  @Test
  void shouldProcessInstanceCreatedEventFromDataImport() {
    // given
    var marc = loadResourceAsString("samples/full_marc_sample.jsonl");
    var emittedEvent = instanceCreatedEvent(EVENT_ID_01, TENANT_ID, marc);
    var expectedEvent = newMarcBibDataImportEvent(marc);

    // when
    eventKafkaTemplate.send(newProducerRecord(emittedEvent));

    // then
    awaitAndAssert(() -> verify(dataImportEventHandler).handle(expectedEvent));

    var found = tenantScopedExecutionService.execute(
      TENANT_ID,
      () -> resourceRepo.findAllByType(Set.of(INSTANCE.getUri()), Pageable.ofSize(1))
        .stream()
        .findFirst()
    );
    assertThat(found).isPresent();
    var result = found.get();
    assertThat(result.getLabel()).isEqualTo("Instance MainTitle");
    assertThat(result.getTypes().iterator().next().getUri()).isEqualTo(INSTANCE.getUri());
    assertThat(result.getDoc()).isNotEmpty();
    assertThat(result.getOutgoingEdges()).isNotEmpty();
    result.getOutgoingEdges().forEach(edge -> {
      assertThat(edge.getSource()).isEqualTo(result);
      assertThat(edge.getTarget()).isNotNull();
      assertThat(edge.getPredicate()).isNotNull();
    });
    var instanceMetadata = result.getInstanceMetadata();
    assertThat(instanceMetadata.getSource()).isEqualTo(MARC);
    assertThat(instanceMetadata.getInventoryId()).hasToString("2165ef4b-001f-46b3-a60e-52bcdeb3d5a1");
    assertThat(instanceMetadata.getSrsId()).hasToString("43d58061-decf-4d74-9747-0e1c368e861b");

    assertWorkIsIndexed(result);
    verifyNoInteractions(instanceIngressMessageProducer);
  }

  @Transactional
  @Test
  void shouldConsumeAuthorityEventFromDataImport() {
    // given
    var marc = loadResourceAsString("samples/authority_100.jsonl");
    var emittedEvent = authorityEvent(EVENT_ID_01, TENANT_ID, marc);
    var expectedLabel = "bValue, aValue, cValue, qValue, dValue -- vValue -- xValue -- yValue -- zValue";
    var expectedEvent = newAuthorutyDataImportEvent(marc);

    // when
    eventKafkaTemplate.send(newProducerRecord(emittedEvent));

    // then
    awaitAndAssert(() -> verify(dataImportEventHandler).handle(expectedEvent));

    var found = tenantScopedExecutionService.execute(
      TENANT_ID,
      () -> resourceRepo.findAllByType(Set.of(CONCEPT.getUri(), PERSON.getUri()), Pageable.ofSize(1))
        .stream()
        .findFirst()
    );

    assertThat(found)
      .isPresent()
      .get()
      .hasFieldOrPropertyWithValue("label", expectedLabel)
      .satisfies(resource -> assertThat(resource.getDoc()).isNotEmpty())
      .satisfies(resource -> assertThat(resource.getOutgoingEdges()).isNotEmpty())
      .extracting(Resource::getOutgoingEdges)
      .satisfies(resourceEdges -> assertThat(resourceEdges)
        .isNotEmpty()
        .allMatch(edge -> Objects.nonNull(edge.getSource()))
        .allMatch(edge -> Objects.nonNull(edge.getTarget()))
        .allMatch(edge -> Objects.nonNull(edge.getPredicate()))
      );

    awaitAndAssert(() ->
      assertTrue(kafkaSearchAuthorityAuthorityTopicListener.getMessages()
        .stream()
        .filter(m -> m.contains("\"type\":\"CREATE\""))
        .filter(m -> m.contains("\"tenant\":\"test_tenant\""))
        .filter(m -> m.contains("\"resourceName\":\"linked-data-authority\""))
        .anyMatch(m -> m.contains(expectedLabel))
      )
    );
  }

  @Test
  void shouldSendToIndexWorkWithTwoInstances() {
    //given
    var firstInstanceMarc = loadResourceAsString("samples/full_marc_sample.jsonl");
    mapAndSave(firstInstanceMarc);
    var secondInstanceMarc = firstInstanceMarc.replace("  2019493854", "  2019493855");
    var emittedEvent = instanceCreatedEvent(EVENT_ID_01, TENANT_ID, secondInstanceMarc);

    //when
    eventKafkaTemplate.send(newProducerRecord(emittedEvent));

    //then
    awaitAndAssert(() ->
      assertTrue(
        kafkaSearchWorkIndexTopicListener.getMessages()
          .stream()
          .anyMatch(message -> {
            try {
              return OBJECT_MAPPER.readValue(message, JsonNode.class).get("new").get("instances").size() == 2;
            } catch (JsonProcessingException e) {
              throw new RuntimeException(e);
            }
          })
      )
    );
  }

  private void assertWorkIsIndexed(Resource instance) {
    var workIdOptional = instance.getOutgoingEdges()
      .stream()
      .filter(edge -> edge.getPredicate().getUri().equals("http://bibfra.me/vocab/lite/instantiates"))
      .map(ResourceEdge::getTarget)
      .map(Resource::getId)
      .findFirst();
    assertThat(workIdOptional).isPresent();
    awaitAndAssert(() ->
      assertTrue(
        kafkaSearchWorkIndexTopicListener.getMessages()
          .stream()
          .anyMatch(m -> m.contains(workIdOptional.get().toString()) && m.contains(UPDATE.getValue()))
      )
    );
  }

  private DataImportEvent newMarcBibDataImportEvent(String marc) {
    return newDataImportEvent().marcBib(marc);
  }

  private DataImportEvent newAuthorutyDataImportEvent(String marc) {
    return newDataImportEvent().marcAuthority(marc);
  }

  private DataImportEvent newDataImportEvent() {
    return new DataImportEvent()
      .id(EVENT_ID_01)
      .tenant(TENANT_ID)
      .eventType(DI_COMPLETED_TOPIC);
  }

  private ProducerRecord<String, String> newProducerRecord(String emittedEvent) {
    return new ProducerRecord(getTopicName(TENANT_ID, DI_COMPLETED_TOPIC), 0,
      EVENT_ID_01, emittedEvent, defaultKafkaHeaders());
  }

  private void mapAndSave(String marc) {
    marc2BibframeMapper.fromMarcJson(marc)
      .map(resourceModelMapper::toEntity)
      .map(resourceRepo::save)
      .map(Resource::getOutgoingEdges)
      .stream()
      .flatMap(Set::stream)
      .filter(resourceEdge -> INSTANTIATES.getUri().equals(resourceEdge.getPredicate().getUri()))
      .forEach(resourceEdge -> {
        resourceEdge.computeId();
        resourceRepo.save(resourceEdge.getTarget());
        resourceEdgeRepository.save(resourceEdge);
      });
  }
}
