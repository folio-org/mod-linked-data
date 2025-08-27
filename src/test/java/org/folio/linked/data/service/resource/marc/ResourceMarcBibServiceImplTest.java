package org.folio.linked.data.service.resource.marc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.folio.ld.dictionary.PredicateDictionary.ADMIN_METADATA;
import static org.folio.ld.dictionary.ResourceTypeDictionary.ANNOTATION;
import static org.folio.linked.data.model.entity.ResourceSource.LINKED_DATA;
import static org.folio.linked.data.test.MonographTestUtil.getSampleInstanceResource;
import static org.folio.linked.data.test.MonographTestUtil.getSampleWork;
import static org.folio.linked.data.test.TestUtil.OBJECT_MAPPER;
import static org.folio.linked.data.test.TestUtil.emptyRequestProcessingException;
import static org.folio.linked.data.test.TestUtil.random;
import static org.folio.linked.data.test.TestUtil.randomLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.folio.ld.dictionary.model.FolioMetadata;
import org.folio.ld.dictionary.model.RawMarc;
import org.folio.ld.dictionary.model.ResourceEdge;
import org.folio.linked.data.domain.dto.ResourceIdDto;
import org.folio.linked.data.domain.dto.ResourceMarcViewDto;
import org.folio.linked.data.domain.dto.ResourceResponseDto;
import org.folio.linked.data.exception.RequestProcessingException;
import org.folio.linked.data.exception.RequestProcessingExceptionBuilder;
import org.folio.linked.data.integration.client.SrsClient;
import org.folio.linked.data.mapper.ResourceModelMapper;
import org.folio.linked.data.mapper.dto.ResourceMarcViewDtoMapper;
import org.folio.linked.data.mapper.dto.resource.ResourceDtoMapper;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.event.ResourceEvent;
import org.folio.linked.data.model.entity.event.ResourceUpdatedEvent;
import org.folio.linked.data.repo.FolioMetadataRepository;
import org.folio.linked.data.repo.ResourceRepository;
import org.folio.linked.data.service.profile.ResourceProfileLinkingService;
import org.folio.linked.data.service.resource.edge.ResourceEdgeService;
import org.folio.linked.data.service.resource.graph.ResourceGraphService;
import org.folio.marc4ld.enums.UnmappedMarcHandling;
import org.folio.marc4ld.service.ld2marc.Ld2MarcMapper;
import org.folio.marc4ld.service.marc2ld.bib.MarcBib2ldMapper;
import org.folio.rest.jaxrs.model.ParsedRecord;
import org.folio.rest.jaxrs.model.Record;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;

@UnitTest
@ExtendWith(MockitoExtension.class)
class ResourceMarcBibServiceImplTest {

  @InjectMocks
  private ResourceMarcBibServiceImpl resourceMarcService;

  @Mock
  private FolioMetadataRepository folioMetadataRepo;
  @Mock
  private ResourceRepository resourceRepo;
  @Mock
  private ResourceDtoMapper resourceDtoMapper;
  @Mock
  private ResourceMarcViewDtoMapper resourceMarcViewDtoMapper;
  @Mock
  private ResourceModelMapper resourceModelMapper;
  @Mock
  private Ld2MarcMapper ld2MarcMapper;
  @Mock
  private MarcBib2ldMapper marcBib2ldMapper;
  @Mock
  private ApplicationEventPublisher applicationEventPublisher;
  @Mock
  private ResourceGraphService resourceGraphService;
  @Mock
  private ObjectMapper objectMapper = OBJECT_MAPPER;
  @Mock
  private SrsClient srsClient;
  @Mock
  private RequestProcessingExceptionBuilder exceptionBuilder;
  @Mock
  private ResourceEdgeService resourceEdgeService;
  @Mock
  private RawMarcService rawMarcService;
  @Mock
  private ResourceProfileLinkingService resourceProfileLinkingService;

  @Test
  void getResourceMarcView_shouldReturnExistedEntity() {
    // given
    var id = randomLong();
    var existedResource = getSampleInstanceResource();
    var expectedModelResource = random(org.folio.ld.dictionary.model.Resource.class);
    var expectedMarcString = "{mark: \"json\"}";
    var expectedResponse = random(ResourceMarcViewDto.class);

    when(resourceRepo.findById(id))
      .thenReturn(Optional.of(existedResource));
    when(resourceModelMapper.toModel(existedResource))
      .thenReturn(expectedModelResource);
    when(ld2MarcMapper.toMarcJson(expectedModelResource, UnmappedMarcHandling.APPEND))
      .thenReturn(expectedMarcString);
    when(resourceMarcViewDtoMapper.toMarcViewDto(existedResource, expectedMarcString))
      .thenReturn(expectedResponse);

    // when
    var result = resourceMarcService.getResourceMarcView(id);

    // then
    assertThat(result)
      .isEqualTo(expectedResponse);
  }

  @Test
  void getResourceMarcView_shouldThrowRequestProcessingException_ifNoEntityExists() {
    // given
    var notExistedId = randomLong();
    when(resourceRepo.findById(notExistedId))
      .thenReturn(Optional.empty());
    var expectedException = emptyRequestProcessingException();
    when(exceptionBuilder.notFoundLdResourceByIdException(anyString(), anyString()))
      .thenReturn(expectedException);

    // when
    assertThatExceptionOfType(RequestProcessingException.class)
      .isThrownBy(() -> resourceMarcService.getResourceMarcView(notExistedId));
  }

  @Test
  void getResourceMarcView_shouldThrowException_ifNotInstance() {
    // given
    var notExistedId = randomLong();
    var existedResource = getSampleWork(null);
    when(resourceRepo.findById(notExistedId))
      .thenReturn(Optional.of(existedResource));
    when(exceptionBuilder.notSupportedException(anyString(), anyString()))
      .thenReturn(emptyRequestProcessingException());

    // when
    assertThatExceptionOfType(RequestProcessingException.class)
      .isThrownBy(() -> resourceMarcService.getResourceMarcView(notExistedId));
  }

  @ParameterizedTest
  @CsvSource({
    "a, a",
    "a, m",
    "a, s",
  })
  void checkMarcBibImportableToGraph_shouldReturnTrue(char type, char level) {
    //given
    var inventoryId = UUID.randomUUID().toString();
    var marcRecord = createRecord(type, level);
    when(srsClient.getSourceStorageInstanceRecordById(inventoryId))
      .thenReturn(new ResponseEntity<>(marcRecord, HttpStatusCode.valueOf(200)));

    //expect
    assertThat(resourceMarcService.checkMarcBibImportableToGraph(inventoryId)).isTrue();
  }

  @ParameterizedTest
  @CsvSource({
    "' ', ' '",
    "' ',  a",
    "' ',  m",
    "a, ' '",
    "a, f",
    "o, a",
    "o, m",
  })
  void checkMarcBibImportableToGraph_shouldReturnFalse(char type, char level) {
    //given
    var inventoryId = UUID.randomUUID().toString();
    var marcRecord = createRecord(type, level);
    when(srsClient.getSourceStorageInstanceRecordById(inventoryId))
      .thenReturn(new ResponseEntity<>(marcRecord, HttpStatusCode.valueOf(200)));

    //expect
    assertThat(resourceMarcService.checkMarcBibImportableToGraph(inventoryId)).isFalse();
  }

  @Test
  void checkMarcBibImportableToGraph_shouldThrowRequestProcessingException() {
    //given
    var inventoryId = UUID.randomUUID().toString();
    when(srsClient.getSourceStorageInstanceRecordById(inventoryId))
      .thenThrow(FeignException.NotFound.class);
    when(exceptionBuilder.notFoundSourceRecordException(anyString(), anyString()))
      .thenReturn(emptyRequestProcessingException());

    //expect
    assertThatExceptionOfType(RequestProcessingException.class)
      .isThrownBy(() -> resourceMarcService.checkMarcBibImportableToGraph(inventoryId));
  }

  @Test
  void getResourcePreviewByInventoryId_shouldReturn_resourceResponseDto() throws JsonProcessingException {
    //given
    var inventoryId = UUID.randomUUID().toString();
    var marcRecord = createRecord('a', 'm');
    var marcJson = "";
    var resourceModel = new org.folio.ld.dictionary.model.Resource();
    var resourceEntity = new Resource();
    var resourceDto = new ResourceResponseDto();
    when(srsClient.getSourceStorageInstanceRecordById(inventoryId))
      .thenReturn(new ResponseEntity<>(marcRecord, HttpStatusCode.valueOf(200)));
    when(objectMapper.writeValueAsString(marcRecord.getParsedRecord().getContent())).thenReturn(marcJson);
    when(marcBib2ldMapper.fromMarcJson(marcJson)).thenReturn(Optional.of(resourceModel));
    when(resourceModelMapper.toEntity(resourceModel)).thenReturn(resourceEntity);
    when(resourceDtoMapper.toDto(resourceEntity)).thenReturn(resourceDto);

    //when
    var result = resourceMarcService.getResourcePreviewByInventoryId(inventoryId);

    //then
    assertThat(result).isEqualTo(resourceDto);
  }

  @Test
  void getResourcePreviewByInventoryId_shouldThrowRequestProcessingException() {
    // given
    var inventoryId = UUID.randomUUID().toString();
    when(srsClient.getSourceStorageInstanceRecordById(inventoryId))
      .thenThrow(FeignException.NotFound.class);
    when(exceptionBuilder.notFoundSourceRecordException(anyString(), anyString()))
      .thenReturn(emptyRequestProcessingException());

    // when
    assertThatExceptionOfType(RequestProcessingException.class)
      .isThrownBy(() -> resourceMarcService.getResourcePreviewByInventoryId(inventoryId));
  }

  @Test
  void importMarcRecord_shouldCreateResource() throws JsonProcessingException {
    //given
    var inventoryId = UUID.randomUUID().toString();
    var marcRecord = createRecord('a', 'm');
    var marcJson = "";
    var unmappedMarc = "{}";
    var resourceId = 1L;
    var srsId = UUID.randomUUID().toString();
    var resourceEntity = new Resource().setId(resourceId);
    final var profileId = random(Integer.class);
    resourceEntity.setFolioMetadata(new org.folio.linked.data.model.entity.FolioMetadata(resourceEntity)
      .setSrsId(srsId));
    var resourceModel = new org.folio.ld.dictionary.model.Resource()
      .setId(resourceId)
      .setFolioMetadata(new FolioMetadata().setSrsId(srsId))
      .setUnmappedMarc(new RawMarc().setContent(unmappedMarc));
    var resourceEventCaptor = ArgumentCaptor.forClass(ResourceEvent.class);
    var resourceModelCaptor = ArgumentCaptor.forClass(org.folio.ld.dictionary.model.Resource.class);

    when(srsClient.getSourceStorageInstanceRecordById(inventoryId))
      .thenReturn(new ResponseEntity<>(marcRecord, HttpStatusCode.valueOf(200)));
    when(objectMapper.writeValueAsString(marcRecord.getParsedRecord().getContent())).thenReturn(marcJson);
    when(marcBib2ldMapper.fromMarcJson(marcJson)).thenReturn(Optional.of(resourceModel));
    when(resourceModelMapper.toEntity(resourceModelCaptor.capture())).thenReturn(resourceEntity);
    when(resourceRepo.existsById(resourceId)).thenReturn(false);
    when(folioMetadataRepo.existsBySrsId(srsId)).thenReturn(false);
    when(resourceGraphService.saveMergingGraph(resourceEntity)).thenReturn(resourceEntity);

    //when
    var result = resourceMarcService.importMarcRecord(inventoryId, profileId);

    //then
    verify(applicationEventPublisher).publishEvent(resourceEventCaptor.capture());
    verify(resourceProfileLinkingService).linkResourceToProfile(resourceEntity, profileId);
    verify(rawMarcService).saveRawMarc(resourceEntity, unmappedMarc);
    assertThat(resourceEventCaptor.getValue())
      .satisfies(event -> {
        assertThat(event).isInstanceOf(ResourceUpdatedEvent.class);
        assertThat(((ResourceUpdatedEvent) event).resource()).isEqualTo(resourceEntity);
      });
    assertThat(result)
      .satisfies(resourceIdDto -> {
        assertThat(resourceIdDto).isInstanceOf(ResourceIdDto.class);
        assertThat(resourceIdDto.getId()).isEqualTo("1");
      });
    assertThat(resourceModelCaptor.getValue().getFolioMetadata().getSource().name()).isEqualTo(LINKED_DATA.name());
  }

  @Test
  void importMarcRecord_shouldThrowRequestProcessingException() {
    //given
    var inventoryId = UUID.randomUUID().toString();
    when(srsClient.getSourceStorageInstanceRecordById(inventoryId))
      .thenThrow(FeignException.NotFound.class);
    when(exceptionBuilder.notFoundSourceRecordException(anyString(), anyString()))
      .thenReturn(emptyRequestProcessingException());

    //expect
    assertThatExceptionOfType(RequestProcessingException.class)
      .isThrownBy(() -> resourceMarcService.importMarcRecord(inventoryId, 1));
  }

  @Test
  void importMarcRecord_shouldThrowAlreadyExistsException_whenResourceWithSameIdExists()
    throws JsonProcessingException {
    //given
    var inventoryId = UUID.randomUUID().toString();
    var marcRecord = createRecord('a', 'm');
    var marcJson = "";
    var resourceId = 1L;
    var resourceModel = new org.folio.ld.dictionary.model.Resource()
      .setId(resourceId)
      .setFolioMetadata(new FolioMetadata());

    when(srsClient.getSourceStorageInstanceRecordById(inventoryId))
      .thenReturn(new ResponseEntity<>(marcRecord, HttpStatusCode.valueOf(200)));
    when(objectMapper.writeValueAsString(marcRecord.getParsedRecord().getContent())).thenReturn(marcJson);
    when(marcBib2ldMapper.fromMarcJson(marcJson)).thenReturn(Optional.of(resourceModel));
    when(resourceRepo.existsById(resourceId)).thenReturn(true);
    when(exceptionBuilder.alreadyExistsException(anyString(), anyString()))
      .thenReturn(emptyRequestProcessingException());

    //expect
    assertThatExceptionOfType(RequestProcessingException.class)
      .isThrownBy(() -> resourceMarcService.importMarcRecord(inventoryId, 2));
  }

  @Test
  void importMarcRecord_shouldThrowAlreadyExistsException_whenResourceWithSameSrsIdExists()
    throws JsonProcessingException {
    //given
    var inventoryId = UUID.randomUUID().toString();
    var marcRecord = createRecord('a', 'm');
    var marcJson = "";
    var resourceId = 1L;
    var srsId = UUID.randomUUID().toString();
    var resourceModel = new org.folio.ld.dictionary.model.Resource()
      .setId(resourceId)
      .setFolioMetadata(new FolioMetadata().setSrsId(srsId));

    when(srsClient.getSourceStorageInstanceRecordById(inventoryId))
      .thenReturn(new ResponseEntity<>(marcRecord, HttpStatusCode.valueOf(200)));
    when(objectMapper.writeValueAsString(marcRecord.getParsedRecord().getContent())).thenReturn(marcJson);
    when(marcBib2ldMapper.fromMarcJson(marcJson)).thenReturn(Optional.of(resourceModel));
    when(resourceRepo.existsById(resourceId)).thenReturn(false);
    when(folioMetadataRepo.existsBySrsId(srsId)).thenReturn(true);
    when(exceptionBuilder.alreadyExistsException(anyString(), anyString()))
      .thenReturn(emptyRequestProcessingException());

    //expect
    assertThatExceptionOfType(RequestProcessingException.class)
      .isThrownBy(() -> resourceMarcService.importMarcRecord(inventoryId, 3));
  }

  @Test
  void saveAdminMetadata_shouldDoNothingAndReturnFalse_ifGivenResourceDoesNotContainFolioMetadata() {
    // given
    var resourceModel = new org.folio.ld.dictionary.model.Resource();

    // when
    boolean result = resourceMarcService.saveAdminMetadata(resourceModel);

    // then
    assertThat(result).isFalse();
    verifyNoInteractions(resourceRepo);
    verifyNoInteractions(resourceEdgeService);
  }

  @Test
  void saveAdminMetadata_shouldDoNothingAndReturnFalse_ifGivenResourceDoesNotContainInventoryId() {
    // given
    var resourceModel = new org.folio.ld.dictionary.model.Resource().setFolioMetadata(new FolioMetadata());

    // when
    boolean result = resourceMarcService.saveAdminMetadata(resourceModel);

    // then
    assertThat(result).isFalse();
    verifyNoInteractions(resourceRepo);
    verifyNoInteractions(resourceEdgeService);
  }

  @Test
  void saveAdminMetadata_shouldDoNothingAndReturnFalse_ifGivenResourceDoesNotContainAdminMetadata() {
    // given
    var folioMetadata = new FolioMetadata().setInventoryId(UUID.randomUUID().toString());
    var resourceModel = new org.folio.ld.dictionary.model.Resource().setFolioMetadata(folioMetadata);

    // when
    boolean result = resourceMarcService.saveAdminMetadata(resourceModel);

    // then
    assertThat(result).isFalse();
    verifyNoInteractions(resourceRepo);
    verifyNoInteractions(resourceEdgeService);
  }

  @Test
  void saveAdminMetadata_shouldSaveNothingAndReturnFalse_ifThereIsNoResourceByGivenInventoryId() {
    // given
    var adminMetadata = new org.folio.ld.dictionary.model.Resource()
      .addType(ANNOTATION);
    var folioMetadata = new FolioMetadata().setInventoryId(UUID.randomUUID().toString());
    var resourceModel = new org.folio.ld.dictionary.model.Resource()
      .setId(randomLong())
      .setFolioMetadata(folioMetadata);
    resourceModel
      .addOutgoingEdge(new ResourceEdge(resourceModel, adminMetadata, ADMIN_METADATA));

    // when
    boolean result = resourceMarcService.saveAdminMetadata(resourceModel);

    // then
    assertThat(result).isFalse();
    verifyNoInteractions(resourceEdgeService);
  }

  @Test
  void saveAdminMetadata_shouldSaveAdminMetadata_ifGivenResourceModelContainsAdminMetadataAndSuchResourceExists() {
    // given
    var id = randomLong();
    var adminMetadata = new org.folio.ld.dictionary.model.Resource()
      .addType(ANNOTATION);
    var folioMetadata = new FolioMetadata().setInventoryId(UUID.randomUUID().toString());
    var resourceModel = new org.folio.ld.dictionary.model.Resource()
      .setId(id)
      .setFolioMetadata(folioMetadata);
    var edgeModel = new ResourceEdge(resourceModel, adminMetadata, ADMIN_METADATA);
    resourceModel.addOutgoingEdge(edgeModel);
    doReturn(Optional.of((FolioMetadataRepository.IdOnly) () -> id))
      .when(folioMetadataRepo).findIdByInventoryId(folioMetadata.getInventoryId());

    // when
    boolean result = resourceMarcService.saveAdminMetadata(resourceModel);

    // then
    assertThat(result).isTrue();
    verify(resourceEdgeService).saveNewResourceEdge(id, edgeModel);
    verify(resourceEdgeService).deleteEdgesHavingPredicate(id, ADMIN_METADATA);
  }

  private org.folio.rest.jaxrs.model.Record createRecord(char type, char level) {
    var leader = "04809n   a2200865 i 4500";
    leader = leader.substring(0, 6) + type + level + leader.substring(8);
    var content = Map.of("leader", leader);
    var parsedRecord = new ParsedRecord().withContent(content);
    return new Record().withParsedRecord(parsedRecord);
  }

}
