package org.folio.linked.data.integration.kafka.listener.handler;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.ld.dictionary.PredicateDictionary.ADMIN_METADATA;
import static org.folio.ld.dictionary.PropertyDictionary.CONTROL_NUMBER;
import static org.folio.ld.dictionary.PropertyDictionary.CREATED_DATE;
import static org.folio.ld.dictionary.ResourceTypeDictionary.CONCEPT;
import static org.folio.ld.dictionary.ResourceTypeDictionary.INSTANCE;
import static org.folio.ld.dictionary.ResourceTypeDictionary.PERSON;
import static org.folio.linked.data.domain.dto.SourceRecordDomainEvent.EventTypeEnum.SOURCE_RECORD_CREATED;
import static org.folio.linked.data.domain.dto.SourceRecordDomainEvent.EventTypeEnum.SOURCE_RECORD_UPDATED;
import static org.folio.linked.data.domain.dto.SourceRecordType.MARC_AUTHORITY;
import static org.folio.linked.data.domain.dto.SourceRecordType.MARC_BIB;
import static org.folio.linked.data.test.TestUtil.TENANT_ID;
import static org.folio.linked.data.test.TestUtil.assertAuthority;
import static org.folio.linked.data.test.TestUtil.awaitAndAssert;
import static org.folio.linked.data.test.TestUtil.cleanResourceTables;
import static org.folio.linked.data.test.TestUtil.loadResourceAsString;
import static org.folio.linked.data.test.TestUtil.randomLong;
import static org.folio.linked.data.test.kafka.KafkaEventsTestDataFixture.getSrsDomainEventProducerRecord;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.util.Set;
import java.util.UUID;
import org.folio.ld.dictionary.PredicateDictionary;
import org.folio.linked.data.domain.dto.InstanceIngressEvent;
import org.folio.linked.data.e2e.base.IntegrationTest;
import org.folio.linked.data.integration.ResourceModificationEventListener;
import org.folio.linked.data.mapper.ResourceModelMapper;
import org.folio.linked.data.model.entity.FolioMetadata;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceEdge;
import org.folio.linked.data.repo.ResourceEdgeRepository;
import org.folio.linked.data.service.resource.marc.ResourceMarcAuthorityService;
import org.folio.linked.data.service.resource.marc.ResourceMarcBibService;
import org.folio.linked.data.service.tenant.TenantScopedExecutionService;
import org.folio.linked.data.test.MonographTestUtil;
import org.folio.linked.data.test.kafka.KafkaSearchWorkIndexTopicListener;
import org.folio.linked.data.test.resource.ResourceTestRepository;
import org.folio.marc4ld.service.marc2ld.bib.MarcBib2ldMapper;
import org.folio.spring.tools.kafka.FolioMessageProducer;
import org.folio.spring.tools.kafka.KafkaAdminService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

@IntegrationTest
class SourceRecordDomainEventHandlerIT {

  @Autowired
  private ResourceEdgeRepository resourceEdgeRepository;
  @Autowired
  private KafkaTemplate<String, String> eventKafkaTemplate;
  @Autowired
  private TenantScopedExecutionService tenantScopedExecutionService;
  @Autowired
  private KafkaSearchWorkIndexTopicListener kafkaSearchWorkIndexTopicListener;
  @MockitoSpyBean
  private FolioMessageProducer<InstanceIngressEvent> instanceIngressMessageProducer;
  @Autowired
  private MarcBib2ldMapper marcBib2ldMapper;
  @Autowired
  private ResourceModelMapper resourceModelMapper;
  @Autowired
  private ResourceTestRepository resourceTestRepository;
  @MockitoSpyBean
  @Autowired
  private ResourceMarcAuthorityService resourceMarcAuthorityService;
  @MockitoSpyBean
  @Autowired
  private ResourceMarcBibService resourceMarcBibService;
  @MockitoSpyBean
  @Autowired
  private ResourceModificationEventListener eventListener;
  @Autowired
  private JdbcTemplate jdbcTemplate;

  @BeforeAll
  static void beforeAll(@Autowired KafkaAdminService kafkaAdminService) {
    kafkaAdminService.createTopics(TENANT_ID);
  }

  @BeforeEach
  void clean() {
    tenantScopedExecutionService.execute(TENANT_ID,
      () -> {
        cleanResourceTables(jdbcTemplate);
        kafkaSearchWorkIndexTopicListener.getMessages().clear();
      }
    );
  }

  @ParameterizedTest
  @CsvSource({
    "samples/marc2ld/marc_non_monograph_leader.jsonl, 0",
    "samples/marc2ld/marc_monograph_leader.jsonl, 1"
  })
  void shouldNotProcessEventForNullableResource(String resource, int interactions) {
    // given
    var marc = loadResourceAsString(resource);
    var eventProducerRecord =
      getSrsDomainEventProducerRecord(randomUUID().toString(), marc, SOURCE_RECORD_CREATED, MARC_BIB);

    // when
    eventKafkaTemplate.send(eventProducerRecord);

    // then
    awaitAndAssert(() -> verify(resourceMarcBibService, times(interactions))
      .saveAdminMetadata(any(org.folio.ld.dictionary.model.Resource.class)));
  }

  @Test
  void shouldSaveAdminMetadataOutOfMarcBibSourceRecordDomainEvent() {
    // given
    var existedInstance = new Resource()
      .setIdAndRefreshEdges(6331008328653046125L)
      .addTypes(INSTANCE);
    var folioMetadata = new FolioMetadata(existedInstance).setInventoryId("2165ef4b-001f-46b3-a60e-52bcdeb3d5a1");
    existedInstance.setFolioMetadata(folioMetadata);
    var title = MonographTestUtil.createPrimaryTitle(randomLong());
    existedInstance.addOutgoingEdge(new ResourceEdge(existedInstance, title, PredicateDictionary.TITLE));
    resourceTestRepository.save(existedInstance);
    var marc = loadResourceAsString("samples/marc2ld/full_marc_sample.jsonl");
    var eventProducerRecord =
      getSrsDomainEventProducerRecord(randomUUID().toString(), marc, SOURCE_RECORD_CREATED, MARC_BIB);

    // when
    eventKafkaTemplate.send(eventProducerRecord);

    // then
    awaitAndAssert(() -> verify(resourceMarcBibService)
      .saveAdminMetadata(any(org.folio.ld.dictionary.model.Resource.class)));

    var found = tenantScopedExecutionService.execute(TENANT_ID,
      () -> resourceTestRepository.findByIdWithEdgesLoaded(existedInstance.getId())
    );
    assertThat(found).isPresent();
    var result = found.get();
    assertThat(result.getOutgoingEdges()).isNotEmpty();
    var adminMetadata = result.getOutgoingEdges().stream()
      .filter(re -> re.getPredicate().getUri().equals(ADMIN_METADATA.getUri()))
      .findFirst();
    assertThat(adminMetadata).isPresent();
    var adminMetadataDoc = adminMetadata.get().getTarget().getDoc();
    assertThat(adminMetadataDoc).hasSize(2);
    assertThat(adminMetadataDoc.has(CONTROL_NUMBER.getValue())).isTrue();
    assertThat(adminMetadataDoc.get(CONTROL_NUMBER.getValue())).hasSize(1);
    assertThat(adminMetadataDoc.get(CONTROL_NUMBER.getValue()).get(0).asText()).isEqualTo("#880524405##");
    assertThat(adminMetadataDoc.has(CREATED_DATE.getValue())).isTrue();
    assertThat(adminMetadataDoc.get(CREATED_DATE.getValue())).hasSize(1);
    assertThat(adminMetadataDoc.get(CREATED_DATE.getValue()).get(0).asText()).isEqualTo("2019-06-07");
    verifyNoInteractions(instanceIngressMessageProducer);
  }

  @Test
  void shouldProcessAuthoritySourceRecordDomainCreateEvent() {
    // given
    var marc = loadResourceAsString("samples/marc2ld/authority_100.jsonl")
      .replace("aValue", "aaValue")
      .replace("1125d50a-adea-4eaa-a418-6b3a0e6fa6ae", UUID.randomUUID().toString())
      .replace("6dcb9a08-9884-4a15-b990-89c879a8e988", UUID.randomUUID().toString());
    var eventProducerRecord =
      getSrsDomainEventProducerRecord(randomUUID().toString(), marc, SOURCE_RECORD_CREATED, MARC_AUTHORITY);

    // when
    eventKafkaTemplate.send(eventProducerRecord);

    // then
    awaitAndAssert(() -> verify(resourceMarcAuthorityService)
      .saveMarcAuthority(any(org.folio.ld.dictionary.model.Resource.class)));

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
      getSrsDomainEventProducerRecord(randomUUID().toString(), marcCreate, SOURCE_RECORD_CREATED, MARC_AUTHORITY);
    eventKafkaTemplate.send(eventProducerRecordCreate);
    awaitAndAssert(() -> verify(resourceMarcAuthorityService)
      .saveMarcAuthority(any(org.folio.ld.dictionary.model.Resource.class)));
    var marcUpdate = marcCreate.replace("aValue", "newAValue");
    var eventProducerRecordUpdate =
      getSrsDomainEventProducerRecord(randomUUID().toString(), marcUpdate, SOURCE_RECORD_UPDATED, MARC_AUTHORITY);

    // when
    eventKafkaTemplate.send(eventProducerRecordUpdate);

    // then
    awaitAndAssert(() -> verify(resourceMarcAuthorityService, times(2))
      .saveMarcAuthority(any(org.folio.ld.dictionary.model.Resource.class)));

    var found = tenantScopedExecutionService.execute(
      TENANT_ID,
      () -> resourceTestRepository.findAllByTypeWithEdgesLoaded(Set.of(CONCEPT.getUri(), PERSON.getUri()), 2,
          Pageable.ofSize(10))
        .stream()
        .toList()
    );

    assertThat(found).hasSize(2);
    var createdResource = found.get(1);
    var updatedResource = found.getFirst();
    var expectedLabelCreated = "bValue, aValue, cValue, qValue, dValue -- vValue -- xValue -- yValue -- zValue";
    var expectedLabelUpdated = expectedLabelCreated.replace("aValue", "newAValue");
    assertAuthority(createdResource, expectedLabelCreated, false, false, updatedResource);
    assertAuthority(updatedResource, expectedLabelUpdated, true, true, null);
  }

}
