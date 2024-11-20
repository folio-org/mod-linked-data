package org.folio.linked.data.service.resource.impl;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.ld.dictionary.PredicateDictionary.REPLACED_BY;
import static org.folio.ld.dictionary.PropertyDictionary.RESOURCE_PREFERRED;
import static org.folio.ld.dictionary.ResourceTypeDictionary.CONCEPT;
import static org.folio.ld.dictionary.ResourceTypeDictionary.FORM;
import static org.folio.ld.dictionary.ResourceTypeDictionary.PERSON;
import static org.folio.ld.dictionary.ResourceTypeDictionary.WORK;
import static org.folio.linked.data.service.resource.AssignAuthorityTarget.CREATOR_OF_WORK;
import static org.folio.linked.data.service.resource.AssignAuthorityTarget.SUBJECT_OF_WORK;
import static org.folio.linked.data.test.TestUtil.OBJECT_MAPPER;
import static org.folio.linked.data.test.TestUtil.randomLong;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;
import org.folio.ld.dictionary.ResourceTypeDictionary;
import org.folio.ld.dictionary.model.FolioMetadata;
import org.folio.linked.data.client.SrsClient;
import org.folio.linked.data.domain.dto.Agent;
import org.folio.linked.data.exception.RequestProcessingException;
import org.folio.linked.data.mapper.ResourceModelMapper;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceEdge;
import org.folio.linked.data.model.entity.event.ResourceCreatedEvent;
import org.folio.linked.data.model.entity.event.ResourceReplacedEvent;
import org.folio.linked.data.model.entity.event.ResourceUpdatedEvent;
import org.folio.linked.data.repo.FolioMetadataRepository;
import org.folio.linked.data.repo.ResourceRepository;
import org.folio.linked.data.service.resource.AssignAuthorityTarget;
import org.folio.linked.data.service.resource.ResourceGraphService;
import org.folio.marc4ld.service.marc2ld.authority.MarcAuthority2ldMapper;
import org.folio.rest.jaxrs.model.ParsedRecord;
import org.folio.rest.jaxrs.model.Record;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;

@UnitTest
@Disabled
@ExtendWith(MockitoExtension.class)
class ResourceMarcAuthorityServiceImplTest {

  @InjectMocks
  private ResourceMarcAuthorityServiceImpl resourceMarcService;

  @Mock
  private ResourceModelMapper resourceModelMapper;
  @Mock
  private ResourceRepository resourceRepo;
  @Mock
  private ResourceGraphService resourceGraphService;
  @Mock
  private ApplicationEventPublisher applicationEventPublisher;
  @Mock
  private FolioMetadataRepository folioMetadataRepo;
  @Mock
  private MarcAuthority2ldMapper marcAuthority2ldMapper;
  @Mock
  private SrsClient srsClient;
  @Spy
  private ObjectMapper objectMapper = OBJECT_MAPPER;

  @Test
  void fetchResourceOrCreateFromSrsRecord_shouldFetchResource_ifExistsById() {
    // given
    var existingResource = new Resource().setId(123L).setLabel("").addTypes(PERSON);
    when(resourceRepo.findById(123L)).thenReturn(of(existingResource));

    // when
    var actualResource = resourceMarcService.fetchResourceOrCreateFromSrsRecord(new Agent().id("123"));

    // then
    assertThat(actualResource).isEqualTo(existingResource);
    verify(resourceRepo).findById(123L);
  }

  @Test
  void fetchResourceOrCreateFromSrsRecord_shouldFetchResource_ifExistsBySrsId() {
    // given
    var id = "123";
    var existingResource = new Resource().setId(123L).setLabel("").addTypes(PERSON);
    when(resourceRepo.findById(123L)).thenReturn(empty());
    when(resourceRepo.findByFolioMetadataSrsId(id)).thenReturn(of(existingResource));

    // when
    var actualResource = resourceMarcService.fetchResourceOrCreateFromSrsRecord(new Agent().id(id).srsId(id));

    // then
    assertThat(actualResource).isEqualTo(existingResource);
    verify(resourceRepo).findById(123L);
    verify(resourceRepo).findByFolioMetadataSrsId(id);
  }

  @Test
  void fetchResourceOrCreateFromSrsRecord_shouldCreateResourceFromSrs_ifNotExistsInRepo() {
    // given
    var id = "123";
    var createdResource = new Resource().setId(123L).setLabel("").addTypes(PERSON);
    when(resourceRepo.findById(123L)).thenReturn(empty());
    when(resourceRepo.findByFolioMetadataSrsId(id)).thenReturn(empty());
    when(srsClient.getSourceStorageRecordBySrsId(id))
      .thenReturn(new ResponseEntity<>(createRecord(), HttpStatusCode.valueOf(200)));

    var dictionaryModelMock = mock(org.folio.ld.dictionary.model.Resource.class);
    when(marcAuthority2ldMapper.fromMarcJson(any())).thenReturn(List.of(dictionaryModelMock));
    when(resourceModelMapper.toEntity(dictionaryModelMock)).thenReturn(createdResource);
    doReturn(createdResource).when(resourceGraphService).saveMergingGraph(createdResource);

    // when
    var actualResource = resourceMarcService.fetchResourceOrCreateFromSrsRecord(new Agent().id(id).srsId(id));

    // then
    assertThat(actualResource).isEqualTo(createdResource);
    verify(resourceRepo).findById(123L);
    verify(resourceRepo).findByFolioMetadataSrsId(id);
    verify(srsClient).getSourceStorageRecordBySrsId(id);
    verify(resourceGraphService).saveMergingGraph(createdResource);
  }

  private org.folio.rest.jaxrs.model.Record createRecord() {
    var leader = "04809n   a2200865 i 4500";
    leader = leader.substring(0, 6) + "am" + leader.substring(8);
    var content = Map.of("leader", leader);
    var parsedRecord = new ParsedRecord().withContent(content);
    return new Record().withParsedRecord(parsedRecord);
  }

  @Test
  void fetchResourceOrCreateFromSrsRecord_shouldThrowNotFound_ifRecordNotExistsInSrs() {
    // given
    var id = "123";
    when(resourceRepo.findById(123L)).thenReturn(empty());
    when(resourceRepo.findByFolioMetadataSrsId(id)).thenReturn(empty());
    when(srsClient.getSourceStorageRecordBySrsId(id))
      .thenThrow(FeignException.NotFound.class);
    var agent = new Agent().id(id).srsId(id);

    // then
    assertThrows(RequestProcessingException.class,
      () -> resourceMarcService.fetchResourceOrCreateFromSrsRecord(agent));
  }

  @Test
  void saveMarcAuthority_shouldCreateNewAuthority_ifGivenModelDoesNotExistsByIdAndSrsId() {
    // given
    var id = randomLong();
    var srsId = UUID.randomUUID().toString();
    var model = new org.folio.ld.dictionary.model.Resource().setId(id)
      .setFolioMetadata(new FolioMetadata().setSrsId(srsId));
    var mapped = new Resource().setId(id).addTypes(PERSON);
    mapped.setFolioMetadata(new org.folio.linked.data.model.entity.FolioMetadata(mapped).setSrsId(srsId));
    doReturn(mapped).when(resourceModelMapper).toEntity(model);
    doReturn(false).when(resourceRepo).existsById(id);
    doReturn(mapped).when(resourceGraphService).saveMergingGraph(mapped);

    // when
    var result = resourceMarcService.saveMarcResource(model);

    // then
    assertThat(result).isEqualTo(id);
    verify(resourceGraphService).saveMergingGraph(mapped);
    verify(applicationEventPublisher).publishEvent(new ResourceCreatedEvent(mapped));
  }

  @Test
  void saveMarcAuthority_shouldUpdateAuthority_ifGivenModelExistsById() {
    // given
    var id = randomLong();
    var srsId = UUID.randomUUID().toString();
    var model = new org.folio.ld.dictionary.model.Resource().setId(id)
      .setFolioMetadata(new FolioMetadata().setSrsId(srsId));
    var mapped = new Resource().setId(id).addTypes(PERSON);
    mapped.setFolioMetadata(new org.folio.linked.data.model.entity.FolioMetadata(mapped).setSrsId(srsId));
    doReturn(mapped).when(resourceModelMapper).toEntity(model);
    doReturn(true).when(resourceRepo).existsById(id);
    doReturn(mapped).when(resourceGraphService).saveMergingGraph(mapped);

    // when
    var result = resourceMarcService.saveMarcResource(model);

    // then
    assertThat(result).isEqualTo(id);
    verify(resourceGraphService).saveMergingGraph(mapped);
    verify(applicationEventPublisher).publishEvent(new ResourceUpdatedEvent(mapped));
  }

  @Test
  void saveMarcAuthority_shouldCreateNewAuthorityVersionAndMarkOldAsObsolete_ifGivenModelExistsBySrsIdButNotById() {
    // given
    var id = randomLong();
    var srsId = UUID.randomUUID().toString();
    var existed = new Resource().setId(id).setManaged(true);
    existed.setFolioMetadata(new org.folio.linked.data.model.entity.FolioMetadata(existed));
    doReturn(of(existed)).when(resourceRepo).findByFolioMetadataSrsId(srsId);
    doReturn(true).when(folioMetadataRepo).existsBySrsId(srsId);
    var model = new org.folio.ld.dictionary.model.Resource()
      .setId(id)
      .setFolioMetadata(new FolioMetadata().setSrsId(srsId));
    var mapped = new Resource().setId(id).addTypes(PERSON);
    mapped.setFolioMetadata(new org.folio.linked.data.model.entity.FolioMetadata(mapped).setSrsId(srsId));
    doReturn(mapped).when(resourceModelMapper).toEntity(model);
    doReturn(false).when(resourceRepo).existsById(id);
    doReturn(existed).when(resourceRepo).save(existed);

    doReturn(mapped).when(resourceGraphService).saveMergingGraph(mapped);

    // when
    var result = resourceMarcService.saveMarcResource(model);

    // then
    assertThat(result).isEqualTo(id);
    assertThat(existed.isActive()).isFalse();
    assertThat(existed.getDoc().get(RESOURCE_PREFERRED.getValue()).get(0).textValue()).isEqualTo("false");
    assertThat(existed.getFolioMetadata()).isNull();
    verify(resourceRepo).save(existed);
    verify(resourceGraphService).saveMergingGraph(mapped);
    verify(applicationEventPublisher).publishEvent(new ResourceReplacedEvent(existed, mapped));
    assertThat(mapped.getDoc().get(RESOURCE_PREFERRED.getValue()).get(0).textValue()).isEqualTo("true");
    assertThat(mapped.getIncomingEdges()).contains(new ResourceEdge(existed, mapped, REPLACED_BY));
  }

  private static Stream<Arguments> authorityAssignmentArguments() {
    return Stream.of(
      Arguments.of(Set.of(PERSON), CREATOR_OF_WORK, true),
      Arguments.of(Set.of(CONCEPT, PERSON), CREATOR_OF_WORK, false),
      Arguments.of(null, CREATOR_OF_WORK, false),
      Arguments.of(Set.of(WORK), CREATOR_OF_WORK, false),
      Arguments.of(Set.of(FORM), SUBJECT_OF_WORK, true),
      Arguments.of(Set.of(CONCEPT, FORM), SUBJECT_OF_WORK, true),
      Arguments.of(Set.of(PERSON, FORM), SUBJECT_OF_WORK, false),
      Arguments.of(Set.of(CONCEPT, FORM, PERSON), SUBJECT_OF_WORK, false)
    );
  }

  @ParameterizedTest
  @MethodSource("authorityAssignmentArguments")
  void isAuthorityTypeValidForTarget(Set<ResourceTypeDictionary> authorityTypes,
                                      AssignAuthorityTarget target,
                                      boolean isValid) {
    // given
    when(marcAuthority2ldMapper.fromMarcJson(any()))
      .thenReturn(List.of(new org.folio.ld.dictionary.model.Resource().setTypes(authorityTypes)));

    // when
    var actual = resourceMarcService.isMarcCompatibleWithTarget("{}", target);

    // then
    assertThat(actual).isEqualTo(isValid);
    verify(marcAuthority2ldMapper).fromMarcJson(any());
  }

  @Test
  void isAuthorityTypeValidForTarget_shouldReturnFalse_ifAuthorityIsEmpty() {
    // given
    when(marcAuthority2ldMapper.fromMarcJson(any())).thenReturn(List.of());

    // when
    var isValid = resourceMarcService.isMarcCompatibleWithTarget("{}", CREATOR_OF_WORK);

    // then
    assertFalse(isValid);
  }
}
