package org.folio.linked.data.e2e.resource;

import static java.util.Comparator.comparing;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.ld.dictionary.PredicateDictionary.AUTHOR;
import static org.folio.ld.dictionary.PredicateDictionary.CREATOR;
import static org.folio.ld.dictionary.ResourceTypeDictionary.CONCEPT;
import static org.folio.ld.dictionary.ResourceTypeDictionary.PERSON;
import static org.folio.ld.dictionary.ResourceTypeDictionary.WORK;
import static org.folio.linked.data.domain.dto.SourceRecordDomainEvent.EventTypeEnum.SOURCE_RECORD_CREATED;
import static org.folio.linked.data.domain.dto.SourceRecordDomainEvent.EventTypeEnum.SOURCE_RECORD_UPDATED;
import static org.folio.linked.data.domain.dto.SourceRecordType.MARC_AUTHORITY;
import static org.folio.linked.data.e2e.resource.ResourceControllerITBase.RESOURCE_URL;
import static org.folio.linked.data.test.TestUtil.TENANT_ID;
import static org.folio.linked.data.test.TestUtil.assertAuthority;
import static org.folio.linked.data.test.TestUtil.awaitAndAssert;
import static org.folio.linked.data.test.TestUtil.defaultHeaders;
import static org.folio.linked.data.test.TestUtil.loadResourceAsString;
import static org.folio.linked.data.test.kafka.KafkaEventsTestDataFixture.getSrsDomainEventProducerRecord;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.EnumMap;
import java.util.List;
import java.util.Set;
import org.folio.ld.dictionary.PredicateDictionary;
import org.folio.ld.dictionary.PropertyDictionary;
import org.folio.linked.data.e2e.ITBase;
import org.folio.linked.data.e2e.base.IntegrationTest;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceEdge;
import org.folio.linked.data.repo.ResourceEdgeRepository;
import org.folio.linked.data.service.resource.marc.ResourceMarcAuthorityService;
import org.folio.linked.data.service.tenant.TenantScopedExecutionService;
import org.folio.linked.data.test.MonographTestUtil;
import org.folio.linked.data.test.resource.ResourceTestRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

@IntegrationTest
class AuthorityUpdateAndReadWorkIT extends ITBase {

  @Autowired
  private ResourceEdgeRepository resourceEdgeRepository;
  @Autowired
  private KafkaTemplate<String, String> eventKafkaTemplate;
  @Autowired
  private TenantScopedExecutionService tenantScopedExecutionService;
  @Autowired
  private ResourceTestRepository resourceTestRepository;
  @MockitoSpyBean
  @Autowired
  private ResourceMarcAuthorityService resourceMarcService;


  @Test
  void authorityUpdate_withNewFingerprint_shouldLinkItToPreviousAndReturnAsWorkActiveAuthority() throws Exception {
    // given
    var authority = createAuthority();
    var work = createWorkAndLinkToAuthority(authority);
    var authorityUpdateJson = getAuthorityJson().replace("aValue", "newAValue");
    var updateAuthorityEvent = getSrsDomainEventProducerRecord(randomUUID().toString(), authorityUpdateJson,
      SOURCE_RECORD_UPDATED, MARC_AUTHORITY);

    // when
    eventKafkaTemplate.send(updateAuthorityEvent);
    awaitAndAssert(() -> verify(resourceMarcService, times(2))
      .saveMarcAuthority(any(org.folio.ld.dictionary.model.Resource.class)));

    // then
    var authoritiesFromDb = readAndAssertAuthoritiesInTheDb();
    assertWorkIsStillLinkedToObsoleteAuthorityInTheDb(work, authoritiesFromDb.getFirst());
    assertGetWorkWithActiveAuthority(work.getId(), authoritiesFromDb);
  }

  private void assertGetWorkWithActiveAuthority(Long workId, List<Resource> authorities) throws Exception {
    var requestBuilder = get(RESOURCE_URL + "/" + workId)
      .contentType(APPLICATION_JSON)
      .headers(defaultHeaders(env));
    var resultActions = mockMvc.perform(requestBuilder);
    var response = resultActions
      .andExpect(status().isOk())
      .andExpect(content().contentType(APPLICATION_JSON))
      .andReturn().getResponse().getContentAsString();
    assertThat(response)
      .isNotEmpty()
      .contains(authorities.get(1).getId().toString())
      .contains(authorities.get(1).getLabel())
      .doesNotContain(authorities.getFirst().getId().toString())
      .doesNotContain(authorities.getFirst().getLabel());
  }

  private void assertWorkIsStillLinkedToObsoleteAuthorityInTheDb(Resource work, Resource obsoleteAuthority) {
    var workFromDb = tenantScopedExecutionService.execute(
      TENANT_ID,
      () -> resourceTestRepository.findByIdWithEdgesLoaded(work.getId()).orElseThrow()
    );
    assertThat(workFromDb.getOutgoingEdges()).hasSize(3);
    assertThat(workFromDb.getOutgoingEdges()).contains(new ResourceEdge(workFromDb, obsoleteAuthority, AUTHOR));
    assertThat(workFromDb.getOutgoingEdges()).contains(new ResourceEdge(workFromDb, obsoleteAuthority, CREATOR));
  }

  private List<Resource> readAndAssertAuthoritiesInTheDb() {
    var authoritiesFromDb = tenantScopedExecutionService.execute(
      TENANT_ID,
      () -> resourceTestRepository.findAllByTypeWithEdgesLoaded(Set.of(CONCEPT.getUri(), PERSON.getUri()), 2,
          Pageable.ofSize(10))
        .stream()
        .sorted(comparing(Resource::getLabel))
        .toList()
    );
    assertThat(authoritiesFromDb).hasSize(2);
    var expectedLabelCreated = "bValue, aValue, cValue, qValue, dValue -- vValue -- xValue -- yValue -- zValue";
    var expectedLabelUpdated = expectedLabelCreated.replace("aValue", "newAValue");
    assertAuthority(authoritiesFromDb.getFirst(), expectedLabelCreated, false, false, authoritiesFromDb.get(1));
    assertAuthority(authoritiesFromDb.get(1), expectedLabelUpdated, true, true, null);
    return authoritiesFromDb;
  }

  private Resource createAuthority() {
    var authorityCreateEvent = getSrsDomainEventProducerRecord(randomUUID().toString(), getAuthorityJson(),
      SOURCE_RECORD_CREATED, MARC_AUTHORITY);
    eventKafkaTemplate.send(authorityCreateEvent);
    awaitAndAssert(() -> verify(resourceMarcService)
      .saveMarcAuthority(any(org.folio.ld.dictionary.model.Resource.class)));
    return tenantScopedExecutionService.execute(TENANT_ID,
      () -> resourceTestRepository.findById(- 6897633277634168127L)
        .stream()
        .findFirst()
        .orElseThrow()
    );
  }

  private static String getAuthorityJson() {
    return loadResourceAsString("samples/marc2ld/authority_100.jsonl");
  }

  private Resource createWorkAndLinkToAuthority(Resource authority) {
    var work = MonographTestUtil.createResource(new EnumMap<>(PropertyDictionary.class), Set.of(WORK),
      new EnumMap<>(PredicateDictionary.class));
    var title = MonographTestUtil.createPrimaryTitle(null);
    var reTitle = new ResourceEdge(work, title, PredicateDictionary.TITLE);
    reTitle.computeId();
    work.addOutgoingEdge(reTitle);
    var reAuthor = new ResourceEdge(work, authority, PredicateDictionary.AUTHOR);
    reAuthor.computeId();
    work.addOutgoingEdge(reAuthor);
    var reCreator = new ResourceEdge(work, authority, PredicateDictionary.CREATOR);
    reCreator.computeId();
    work.addOutgoingEdge(reCreator);
    tenantScopedExecutionService.execute(TENANT_ID,
      () -> {
        resourceTestRepository.save(work);
        resourceTestRepository.save(title);
        resourceEdgeRepository.save(reTitle);
        resourceEdgeRepository.save(reAuthor);
        resourceEdgeRepository.save(reCreator);
      }
    );
    return work;
  }

}
