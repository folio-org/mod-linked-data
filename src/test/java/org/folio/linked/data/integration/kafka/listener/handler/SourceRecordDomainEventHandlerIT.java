package org.folio.linked.data.integration.kafka.listener.handler;

import static java.util.UUID.randomUUID;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.ld.dictionary.ResourceTypeDictionary.CONCEPT;
import static org.folio.ld.dictionary.ResourceTypeDictionary.INSTANCE;
import static org.folio.ld.dictionary.ResourceTypeDictionary.PERSON;
import static org.folio.linked.data.domain.dto.ResourceIndexEventType.UPDATE;
import static org.folio.linked.data.domain.dto.SourceRecordDomainEvent.EventTypeEnum.CREATED;
import static org.folio.linked.data.domain.dto.SourceRecordDomainEvent.EventTypeEnum.UPDATED;
import static org.folio.linked.data.domain.dto.SourceRecordType.MARC_AUTHORITY;
import static org.folio.linked.data.domain.dto.SourceRecordType.MARC_BIB;
import static org.folio.linked.data.model.entity.ResourceSource.MARC;
import static org.folio.linked.data.test.TestUtil.FOLIO_TEST_PROFILE;
import static org.folio.linked.data.test.TestUtil.OBJECT_MAPPER;
import static org.folio.linked.data.test.TestUtil.TENANT_ID;
import static org.folio.linked.data.test.TestUtil.assertAuthority;
import static org.folio.linked.data.test.TestUtil.awaitAndAssert;
import static org.folio.linked.data.test.TestUtil.loadResourceAsString;
import static org.folio.linked.data.test.kafka.KafkaEventsTestDataFixture.getSrsDomainEventProducerRecord;
import static org.folio.linked.data.util.Constants.FOLIO_PROFILE;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.Set;
import java.util.UUID;
import org.folio.linked.data.domain.dto.InstanceIngressEvent;
import org.folio.linked.data.e2e.base.IntegrationTest;
import org.folio.linked.data.integration.ResourceModificationEventListener;
import org.folio.linked.data.mapper.ResourceModelMapper;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceEdge;
import org.folio.linked.data.repo.ResourceEdgeRepository;
import org.folio.linked.data.service.resource.ResourceMarcAuthorityService;
import org.folio.linked.data.service.tenant.TenantScopedExecutionService;
import org.folio.linked.data.test.ResourceTestRepository;
import org.folio.linked.data.test.kafka.KafkaSearchWorkIndexTopicListener;
import org.folio.marc4ld.service.marc2ld.bib.MarcBib2ldMapper;
import org.folio.spring.tools.kafka.FolioMessageProducer;
import org.folio.spring.tools.kafka.KafkaAdminService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ActiveProfiles;

@IntegrationTest
@ActiveProfiles({FOLIO_PROFILE, FOLIO_TEST_PROFILE})
class SourceRecordDomainEventHandlerIT {

  @Autowired
  private ResourceEdgeRepository resourceEdgeRepository;
  @Autowired
  private KafkaTemplate<String, String> eventKafkaTemplate;
  @Autowired
  private TenantScopedExecutionService tenantScopedExecutionService;
  @Autowired
  private KafkaSearchWorkIndexTopicListener kafkaSearchWorkIndexTopicListener;
  @MockBean
  private FolioMessageProducer<InstanceIngressEvent> instanceIngressMessageProducer;
  @Autowired
  private MarcBib2ldMapper marc2BibframeMapper;
  @Autowired
  private ResourceModelMapper resourceModelMapper;
  @Autowired
  private ResourceTestRepository resourceTestRepository;
  @SpyBean
  @Autowired
  private ResourceMarcAuthorityService resourceMarcService;
  @SpyBean
  @Autowired
  private ResourceModificationEventListener eventListener;

  @BeforeAll
  static void beforeAll(@Autowired KafkaAdminService kafkaAdminService) {
    kafkaAdminService.createTopics(TENANT_ID);
  }

  @BeforeEach
  public void clean() {
    tenantScopedExecutionService.execute(TENANT_ID,
      () -> {
        resourceEdgeRepository.deleteAll();
        resourceTestRepository.deleteAll();
        kafkaSearchWorkIndexTopicListener.getMessages().clear();
      }
    );
  }

  @Disabled("Handling MARC BIB records is disabled temporarily")
  @ParameterizedTest
  @CsvSource({
    "samples/marc2ld/marc_non_monograph_leader.jsonl, 0",
    "samples/marc2ld/marc_monograph_leader.jsonl, 1"
  })
  void shouldNotProcessEventForNullableResource(String resource, int interactions) {
    // given
    var marc = loadResourceAsString(resource);
    var eventProducerRecord = getSrsDomainEventProducerRecord(randomUUID().toString(), marc, CREATED, MARC_BIB);

    // when
    eventKafkaTemplate.send(eventProducerRecord);

    // then
    awaitAndAssert(() -> verify(resourceMarcService, times(interactions))
      .saveMarcResource(any(org.folio.ld.dictionary.model.Resource.class)));
  }

  @Disabled("Handling MARC BIB records is disabled temporarily")
  @Test
  void shouldProcessMarcBibSourceRecordDomainEvent() {
    // given
    var marc = loadResourceAsString("samples/marc2ld/full_marc_sample.jsonl");
    var eventProducerRecord = getSrsDomainEventProducerRecord(randomUUID().toString(), marc, CREATED, MARC_BIB);

    // when
    eventKafkaTemplate.send(eventProducerRecord);

    // then
    awaitAndAssert(() -> verify(resourceMarcService)
      .saveMarcResource(any(org.folio.ld.dictionary.model.Resource.class)));

    var found = tenantScopedExecutionService.execute(
      TENANT_ID,
      () -> resourceTestRepository.findAllByTypeWithEdgesLoaded(Set.of(INSTANCE.getUri()), 1, Pageable.ofSize(1))
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
    var folioMetadata = result.getFolioMetadata();
    assertThat(folioMetadata.getSource()).isEqualTo(MARC);
    assertThat(folioMetadata.getInventoryId()).hasToString("2165ef4b-001f-46b3-a60e-52bcdeb3d5a1");
    assertThat(folioMetadata.getSrsId()).hasToString("43d58061-decf-4d74-9747-0e1c368e861b");

    assertWorkIsIndexed(result);
    verifyNoInteractions(instanceIngressMessageProducer);
  }

  @Test
  void shouldProcessAuthoritySourceRecordDomainCreateEvent() {
    // given
    var marc = loadResourceAsString("samples/marc2ld/authority_100.jsonl")
      .replace("aValue", "aaValue")
      .replace("1125d50a-adea-4eaa-a418-6b3a0e6fa6ae", UUID.randomUUID().toString())
      .replace("6dcb9a08-9884-4a15-b990-89c879a8e988", UUID.randomUUID().toString());
    var eventProducerRecord = getSrsDomainEventProducerRecord(randomUUID().toString(), marc, CREATED, MARC_AUTHORITY);

    // when
    eventKafkaTemplate.send(eventProducerRecord);

    // then
    awaitAndAssert(() -> verify(resourceMarcService)
      .saveMarcResource(any(org.folio.ld.dictionary.model.Resource.class)));

    var found = tenantScopedExecutionService.execute(
      TENANT_ID,
      () -> resourceTestRepository.findAllByTypeWithEdgesLoaded(Set.of(CONCEPT.getUri(), PERSON.getUri()), 2,
          Pageable.ofSize(10))
        .stream()
        .findFirst()
    );

    assertThat(found).isPresent();
    var expectedLabel = "bValue, aaValue, cValue, qValue, dValue -- vValue -- xValue -- yValue -- zValue";
    assertAuthority(found.get(), expectedLabel, true, true, null);
  }

  @Test
  void shouldProcessAuthoritySourceRecordDomainUpdateEvent() {
    // given
    var marcCreate = loadResourceAsString("samples/marc2ld/authority_100.jsonl")
      .replace("1125d50a-adea-4eaa-a418-6b3a0e6fa6ae", UUID.randomUUID().toString())
      .replace("6dcb9a08-9884-4a15-b990-89c879a8e988", UUID.randomUUID().toString());
    var eventProducerRecordCreate =
      getSrsDomainEventProducerRecord(randomUUID().toString(), marcCreate, CREATED, MARC_AUTHORITY);
    eventKafkaTemplate.send(eventProducerRecordCreate);
    awaitAndAssert(() -> verify(resourceMarcService)
      .saveMarcResource(any(org.folio.ld.dictionary.model.Resource.class)));
    var marcUpdate = marcCreate.replace("aValue", "newAValue");
    var eventProducerRecordUpdate =
      getSrsDomainEventProducerRecord(randomUUID().toString(), marcUpdate, UPDATED, MARC_AUTHORITY);

    // when
    eventKafkaTemplate.send(eventProducerRecordUpdate);

    // then
    awaitAndAssert(() -> verify(resourceMarcService, times(2))
      .saveMarcResource(any(org.folio.ld.dictionary.model.Resource.class)));

    var found = tenantScopedExecutionService.execute(
      TENANT_ID,
      () -> resourceTestRepository.findAllByTypeWithEdgesLoaded(Set.of(CONCEPT.getUri(), PERSON.getUri()), 2,
          Pageable.ofSize(10))
        .stream()
        .toList()
    );

    assertThat(found).hasSize(2);
    var createdResource = found.get(1);
    var updatedResource = found.get(0);
    var expectedLabelCreated = "bValue, aValue, cValue, qValue, dValue -- vValue -- xValue -- yValue -- zValue";
    var expectedLabelUpdated = expectedLabelCreated.replace("aValue", "newAValue");
    assertAuthority(createdResource, expectedLabelCreated, false, false, updatedResource);
    assertAuthority(updatedResource, expectedLabelUpdated, true, true, null);
  }

  @Disabled("Handling MARC BIB records is disabled temporarily")
  @Test
  void marcBibSourceRecordDomainEvent_shouldSendToIndexWorkWithTwoInstances() {
    // given
    var firstInstanceMarc = loadResourceAsString("samples/marc2ld/full_marc_sample.jsonl");
    mapAndSave(firstInstanceMarc);
    var secondInstanceMarc = firstInstanceMarc.replace("  2019493854", "  2019493855")
      .replace("code", "another code")
      .replace("item number", "another item number")
      .replace("2165ef4b-001f-46b3-a60e-52bcdeb3d5a1", UUID.randomUUID().toString())
      .replace("43d58061-decf-4d74-9747-0e1c368e861b", UUID.randomUUID().toString());
    var expectedMessage = loadResourceAsString("samples/marc2ld/expected_message.json");
    var id = randomUUID().toString();
    var eventProducerRecord = getSrsDomainEventProducerRecord(id, secondInstanceMarc, CREATED, MARC_BIB);

    // when
    eventKafkaTemplate.send(eventProducerRecord);

    //then
    awaitAndAssert(() -> {
      assertTrue(isNotEmpty(kafkaSearchWorkIndexTopicListener.getMessages()));
      kafkaSearchWorkIndexTopicListener.getMessages()
        .stream()
        .findFirst()
        .ifPresent(message -> {
          try {
            assertThat(OBJECT_MAPPER.readValue(message, Object.class))
              .usingRecursiveComparison()
              .ignoringFields("id", "ts")
              .isEqualTo(OBJECT_MAPPER.readValue(expectedMessage, Object.class));
          } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
          }
        });
    });
  }

  @Disabled("Handling MARC BIB records is disabled temporarily")
  @Test
  void marcBibSourceRecordDomainEvent_shouldKeepExistedEdgesAndPropertiesAndFolioMetadata_inCaseOfUpdate() {
    // given
    var firstInstanceMarc = loadResourceAsString("samples/marc2ld/small_instance.jsonl");
    mapAndSave(firstInstanceMarc);
    var secondInstanceMarc = loadResourceAsString("samples/marc2ld/small_instance_upd.jsonl");
    var eventId = randomUUID().toString();
    var eventProducerRecord = getSrsDomainEventProducerRecord(eventId, secondInstanceMarc, CREATED, MARC_BIB);

    // when
    eventKafkaTemplate.send(eventProducerRecord);

    // then
    awaitAndAssert(() -> verify(resourceMarcService)
      .saveMarcResource(any(org.folio.ld.dictionary.model.Resource.class)));
    verify(eventListener).afterUpdate(any());
    var allInstances = tenantScopedExecutionService.execute(TENANT_ID,
      () -> resourceTestRepository.findAllByTypeWithEdgesLoaded(Set.of(INSTANCE.getUri()), 1, Pageable.ofSize(1))
        .stream()
        .toList()
    );
    assertThat(allInstances).hasSize(1);
    var instance = allInstances.get(0);
    assertThat(instance.getDoc()).hasToString("{\"http://bibfra.me/vocab/marc/statementOfResponsibility\":["
      + "\"Statement Of Responsibility\"]}");
    assertThat(instance.getOutgoingEdges()).hasSize(5);
    assertThat(instance.getFolioMetadata())
      .hasFieldOrPropertyWithValue("inventoryId", "2165ef4b-001f-46b3-a60e-52bcdeb3d5a1")
      .hasFieldOrPropertyWithValue("srsId", "43d58061-decf-4d74-9747-0e1c368e861b")
      .hasFieldOrPropertyWithValue("source", MARC);
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

  private void mapAndSave(String marc) {
    tenantScopedExecutionService.execute(TENANT_ID,
      () -> marc2BibframeMapper.fromMarcJson(marc)
        .map(resourceModelMapper::toEntity)
        .map(resourceTestRepository::save)
        .map(Resource::getOutgoingEdges)
        .stream()
        .flatMap(Set::stream)
        .forEach(this::saveEdge)
    );
  }

  private void saveEdge(ResourceEdge resourceEdge) {
    resourceEdge.computeId();
    var target = resourceEdge.getTarget();
    resourceTestRepository.save(target);
    resourceEdgeRepository.save(resourceEdge);
    target.getOutgoingEdges()
      .forEach(this::saveEdge);
  }

}
