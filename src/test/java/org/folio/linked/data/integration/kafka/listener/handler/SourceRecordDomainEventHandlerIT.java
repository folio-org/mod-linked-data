package org.folio.linked.data.integration.kafka.listener.handler;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.ld.dictionary.PredicateDictionary.REPLACED_BY;
import static org.folio.ld.dictionary.PropertyDictionary.RESOURCE_PREFERRED;
import static org.folio.ld.dictionary.ResourceTypeDictionary.CONCEPT;
import static org.folio.ld.dictionary.ResourceTypeDictionary.PERSON;
import static org.folio.linked.data.domain.dto.ResourceIndexEventType.UPDATE;
import static org.folio.linked.data.domain.dto.SourceRecordDomainEvent.EventTypeEnum.CREATED;
import static org.folio.linked.data.domain.dto.SourceRecordDomainEvent.EventTypeEnum.UPDATED;
import static org.folio.linked.data.domain.dto.SourceRecordType.MARC_AUTHORITY;
import static org.folio.linked.data.test.TestUtil.FOLIO_TEST_PROFILE;
import static org.folio.linked.data.test.TestUtil.TENANT_ID;
import static org.folio.linked.data.test.TestUtil.awaitAndAssert;
import static org.folio.linked.data.test.TestUtil.loadResourceAsString;
import static org.folio.linked.data.test.kafka.KafkaEventsTestDataFixture.getSrsDomainEventProducerRecord;
import static org.folio.linked.data.util.Constants.FOLIO_PROFILE;
import static org.folio.spring.tools.config.properties.FolioEnvironment.getFolioEnvName;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import org.folio.linked.data.domain.dto.InstanceIngressEvent;
import org.folio.linked.data.e2e.base.IntegrationTest;
import org.folio.linked.data.integration.ResourceModificationEventListener;
import org.folio.linked.data.mapper.ResourceModelMapper;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceEdge;
import org.folio.linked.data.repo.ResourceEdgeRepository;
import org.folio.linked.data.service.resource.ResourceMarcService;
import org.folio.linked.data.service.tenant.TenantScopedExecutionService;
import org.folio.linked.data.test.ResourceTestRepository;
import org.folio.linked.data.test.kafka.KafkaSearchAuthorityAuthorityTopicListener;
import org.folio.linked.data.test.kafka.KafkaSearchWorkIndexTopicListener;
import org.folio.marc4ld.service.marc2ld.bib.MarcBib2ldMapper;
import org.folio.spring.tools.kafka.FolioMessageProducer;
import org.folio.spring.tools.kafka.KafkaAdminService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

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
  private KafkaSearchAuthorityAuthorityTopicListener kafkaSearchAuthorityAuthorityTopicListener;
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
  private ResourceMarcService resourceMarcService;
  @SpyBean
  @Autowired
  private ResourceModificationEventListener eventListener;

  @BeforeAll
  static void beforeAll(@Autowired KafkaAdminService kafkaAdminService) {
    kafkaAdminService.createTopics(TENANT_ID);
  }

  private static String getTopicName(String tenantId, String topic) {
    return String.format("%s.%s.%s", getFolioEnvName(), tenantId, topic);
  }

  @BeforeEach
  public void clean() {
    tenantScopedExecutionService.execute(TENANT_ID,
      () -> {
        resourceEdgeRepository.deleteAll();
        resourceTestRepository.deleteAll();
        kafkaSearchAuthorityAuthorityTopicListener.getMessages().clear();
        kafkaSearchWorkIndexTopicListener.getMessages().clear();
      }
    );
  }

  @Transactional
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
      () -> resourceTestRepository.findAllByTypes(Set.of(CONCEPT.getUri(), PERSON.getUri()), 2, Pageable.ofSize(10))
        .stream()
        .findFirst()
    );

    assertThat(found).isPresent();
    var expectedLabel = "bValue, aaValue, cValue, qValue, dValue -- vValue -- xValue -- yValue -- zValue";
    assertAuthority(found.get(), expectedLabel, true, true, null);

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

  @Transactional
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
      () -> resourceTestRepository.findAllByTypes(Set.of(CONCEPT.getUri(), PERSON.getUri()), 2, Pageable.ofSize(10))
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

    awaitAndAssert(() ->
      assertTrue(kafkaSearchAuthorityAuthorityTopicListener.getMessages()
        .stream()
        .filter(m -> m.contains("\"type\":\"CREATE\""))
        .filter(m -> m.contains("\"tenant\":\"test_tenant\""))
        .filter(m -> m.contains("\"resourceName\":\"linked-data-authority\""))
        .anyMatch(m -> m.contains(expectedLabelCreated))
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

  private void assertAuthority(Resource resource,
                               String label,
                               boolean isActive,
                               boolean isPreferred,
                               Resource replacedBy) {
    assertThat(resource)
      .hasFieldOrPropertyWithValue("label", label)
      .hasFieldOrPropertyWithValue("active", isActive)
      .satisfies(r -> assertThat(r.getDoc()).isNotEmpty())
      .satisfies(r ->
        assertThat(resource.getDoc().get(RESOURCE_PREFERRED.getValue()).get(0).asBoolean()).isEqualTo(isPreferred)
      )
      .satisfies(r -> assertThat(r.getOutgoingEdges()).isNotEmpty())
      .extracting(Resource::getOutgoingEdges)
      .satisfies(resourceEdges -> assertThat(resourceEdges)
        .isNotEmpty()
        .allMatch(edge -> Objects.equals(edge.getSource(), resource))
        .allMatch(edge -> nonNull(edge.getTarget()))
        .allMatch(edge -> nonNull(edge.getPredicate()))
        .anyMatch(edge -> isNull(replacedBy) || edge.getPredicate().getUri().equals(REPLACED_BY.getUri())
          && edge.getTarget().equals(replacedBy))
      );
  }
}
