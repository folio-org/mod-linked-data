package org.folio.linked.data.service.resource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.folio.ld.dictionary.PredicateDictionary.REPLACED_BY;
import static org.folio.ld.dictionary.PropertyDictionary.RESOURCE_PREFERRED;
import static org.folio.ld.dictionary.ResourceTypeDictionary.PERSON;
import static org.folio.linked.data.model.entity.ResourceSource.LINKED_DATA;
import static org.folio.linked.data.test.MonographTestUtil.getSampleInstanceResource;
import static org.folio.linked.data.test.MonographTestUtil.getSampleWork;
import static org.folio.linked.data.test.TestUtil.OBJECT_MAPPER;
import static org.folio.linked.data.test.TestUtil.random;
import static org.folio.linked.data.test.TestUtil.randomLong;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.folio.ld.dictionary.model.FolioMetadata;
import org.folio.linked.data.client.SrsClient;
import org.folio.linked.data.domain.dto.ResourceIdDto;
import org.folio.linked.data.domain.dto.ResourceMarcViewDto;
import org.folio.linked.data.domain.dto.ResourceResponseDto;
import org.folio.linked.data.exception.AlreadyExistsException;
import org.folio.linked.data.exception.NotFoundException;
import org.folio.linked.data.exception.ValidationException;
import org.folio.linked.data.mapper.ResourceModelMapper;
import org.folio.linked.data.mapper.dto.ResourceDtoMapper;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceEdge;
import org.folio.linked.data.model.entity.event.ResourceCreatedEvent;
import org.folio.linked.data.model.entity.event.ResourceEvent;
import org.folio.linked.data.model.entity.event.ResourceReplacedEvent;
import org.folio.linked.data.model.entity.event.ResourceUpdatedEvent;
import org.folio.linked.data.repo.FolioMetadataRepository;
import org.folio.linked.data.repo.ResourceEdgeRepository;
import org.folio.linked.data.repo.ResourceRepository;
import org.folio.marc4ld.service.ld2marc.Bibframe2MarcMapper;
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
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;

@UnitTest
@ExtendWith(MockitoExtension.class)
class ResourceMarcServiceTest {

  @InjectMocks
  private ResourceMarcServiceImpl resourceMarcService;

  @Mock
  private FolioMetadataRepository folioMetadataRepo;
  @Mock
  private ResourceRepository resourceRepo;
  @Mock
  private ResourceEdgeRepository edgeRepo;
  @Mock
  private ResourceDtoMapper resourceDtoMapper;
  @Mock
  private ResourceModelMapper resourceModelMapper;
  @Mock
  private Bibframe2MarcMapper bibframe2MarcMapper;
  @Mock
  private MarcBib2ldMapper marcBib2ldMapper;
  @Mock
  private ApplicationEventPublisher applicationEventPublisher;
  @Mock
  private ResourceGraphService resourceGraphService;
  @Spy
  private ObjectMapper objectMapper = OBJECT_MAPPER;
  @Mock
  private SrsClient srsClient;

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
    when(bibframe2MarcMapper.toMarcJson(expectedModelResource))
      .thenReturn(expectedMarcString);
    when(resourceDtoMapper.toMarcViewDto(existedResource, expectedMarcString))
      .thenReturn(expectedResponse);

    // when
    var result = resourceMarcService.getResourceMarcView(id);

    // then
    assertThat(result)
      .isEqualTo(expectedResponse);
  }

  @Test
  void getResourceMarcView_shouldThrowNotFoundException_ifNoEntityExists() {
    // given
    var notExistedId = randomLong();
    when(resourceRepo.findById(notExistedId))
      .thenReturn(Optional.empty());

    // when
    assertThatExceptionOfType(NotFoundException.class)
      .isThrownBy(() -> resourceMarcService.getResourceMarcView(notExistedId));
  }

  @Test
  void getResourceMarcView_shouldThrowException_ifNotInstance() {
    // given
    var notExistedId = randomLong();
    var existedResource = getSampleWork(null);

    when(resourceRepo.findById(notExistedId))
      .thenReturn(Optional.of(existedResource));

    // when
    assertThatExceptionOfType(ValidationException.class)
      .isThrownBy(() -> resourceMarcService.getResourceMarcView(notExistedId));
  }

  @Test
  void saveMarcResource_shouldCreateNewBib_ifGivenModelDoesNotExistsByIdAndSrsId() {
    // given
    var id = randomLong();
    var srsId = UUID.randomUUID().toString();
    var model = new org.folio.ld.dictionary.model.Resource().setId(id)
      .setFolioMetadata(new FolioMetadata().setSrsId(srsId));
    var mapped = new Resource().setId(id);
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
  void saveMarcResource_shouldUpdateBib_ifGivenModelExistsById() {
    // given
    var id = randomLong();
    var srsId = UUID.randomUUID().toString();
    var model = new org.folio.ld.dictionary.model.Resource().setId(id)
      .setFolioMetadata(new FolioMetadata().setSrsId(srsId));
    var mapped = new Resource().setId(id);
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
  void saveMarcResource_shouldReplaceBib_ifGivenModelExistsBySrsIdButNotById() {
    // given
    var id = randomLong();
    var srsId = UUID.randomUUID().toString();
    var existed = new Resource().setId(id).setManaged(true);
    doReturn(Optional.of(existed)).when(resourceRepo).findByFolioMetadataSrsId(srsId);
    doReturn(true).when(folioMetadataRepo).existsBySrsId(srsId);
    var model = new org.folio.ld.dictionary.model.Resource()
      .setId(id)
      .setFolioMetadata(new FolioMetadata().setSrsId(srsId));
    var mapped = new Resource().setId(id);
    mapped.setFolioMetadata(new org.folio.linked.data.model.entity.FolioMetadata(mapped).setSrsId(srsId));
    doReturn(mapped).when(resourceModelMapper).toEntity(model);
    doReturn(false).when(resourceRepo).existsById(id);

    doReturn(mapped).when(resourceGraphService).saveMergingGraph(mapped);

    // when
    var result = resourceMarcService.saveMarcResource(model);

    // then
    assertThat(result).isEqualTo(id);
    verify(resourceGraphService).saveMergingGraph(mapped);
    verify(applicationEventPublisher).publishEvent(new ResourceReplacedEvent(existed, mapped));
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
    doReturn(Optional.of(existed)).when(resourceRepo).findByFolioMetadataSrsId(srsId);
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

  @ParameterizedTest
  @CsvSource({
    "a, a",
    "a, m"
  })
  void isSupportedByInventoryId_shouldReturnTrue(char type, char level) {
    //given
    var inventoryId = UUID.randomUUID().toString();
    var marcRecord = createRecord(type, level);
    when(srsClient.getFormattedSourceStorageInstanceRecordById(inventoryId))
      .thenReturn(new ResponseEntity<>(marcRecord, HttpStatusCode.valueOf(200)));

    //expect
    assertTrue(resourceMarcService.isSupportedByInventoryId(inventoryId));
  }

  @ParameterizedTest
  @CsvSource({
    "' ', ' '",
    "' ',  a",
    "' ',  m",
    "a, ' '",
    "a, s",
    "o, a",
    "o, m",
  })
  void isSupportedByInventoryId_shouldReturnFalse(char type, char level) {
    //given
    var inventoryId = UUID.randomUUID().toString();
    var marcRecord = createRecord(type, level);
    when(srsClient.getFormattedSourceStorageInstanceRecordById(inventoryId))
      .thenReturn(new ResponseEntity<>(marcRecord, HttpStatusCode.valueOf(200)));

    //expect
    assertFalse(resourceMarcService.isSupportedByInventoryId(inventoryId));
  }

  @Test
  void isSupportedByInventoryId_shouldThrowNotFoundException() {
    //given
    var inventoryId = UUID.randomUUID().toString();
    when(srsClient.getFormattedSourceStorageInstanceRecordById(inventoryId))
      .thenThrow(FeignException.NotFound.class);

    //expect
    assertThatExceptionOfType(NotFoundException.class)
      .isThrownBy(() -> resourceMarcService.isSupportedByInventoryId(inventoryId));
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
    when(srsClient.getFormattedSourceStorageInstanceRecordById(inventoryId))
      .thenReturn(new ResponseEntity<>(marcRecord, HttpStatusCode.valueOf(200)));
    when(objectMapper.writeValueAsString(marcRecord.getParsedRecord().getContent())).thenReturn(marcJson);
    when(marcBib2ldMapper.fromMarcJson(marcJson)).thenReturn(Optional.of(resourceModel));
    when(resourceModelMapper.toEntity(resourceModel)).thenReturn(resourceEntity);
    when(resourceDtoMapper.toDto(resourceEntity)).thenReturn(resourceDto);

    //when
    var result = resourceMarcService.getResourcePreviewByInventoryId(inventoryId);

    //then
    assertEquals(resourceDto, result);
  }

  @Test
  void getResourcePreviewByInventoryId_shouldThrowNotFoundException() {
    //given
    var inventoryId = UUID.randomUUID().toString();
    when(srsClient.getFormattedSourceStorageInstanceRecordById(inventoryId))
      .thenThrow(FeignException.NotFound.class);

    //expect
    assertThatExceptionOfType(NotFoundException.class)
      .isThrownBy(() -> resourceMarcService.getResourcePreviewByInventoryId(inventoryId));
  }

  @Test
  void importMarcRecord_shouldCreateResource() throws JsonProcessingException {
    //given
    var inventoryId = UUID.randomUUID().toString();
    var marcRecord = createRecord('a', 'm');
    var marcJson = "";
    var resourceId = 1L;
    var srsId = UUID.randomUUID().toString();
    var resourceEntity = new Resource().setId(resourceId);
    resourceEntity.setFolioMetadata(new org.folio.linked.data.model.entity.FolioMetadata(resourceEntity)
      .setSrsId(srsId));
    var resourceModel = new org.folio.ld.dictionary.model.Resource()
      .setId(resourceId)
      .setFolioMetadata(new FolioMetadata().setSrsId(srsId));
    var resourceEventCaptor = ArgumentCaptor.forClass(ResourceEvent.class);
    var resourceModelCaptor = ArgumentCaptor.forClass(org.folio.ld.dictionary.model.Resource.class);

    when(srsClient.getFormattedSourceStorageInstanceRecordById(inventoryId))
      .thenReturn(new ResponseEntity<>(marcRecord, HttpStatusCode.valueOf(200)));
    when(objectMapper.writeValueAsString(marcRecord.getParsedRecord().getContent())).thenReturn(marcJson);
    when(marcBib2ldMapper.fromMarcJson(marcJson)).thenReturn(Optional.of(resourceModel));
    when(resourceModelMapper.toEntity(resourceModelCaptor.capture())).thenReturn(resourceEntity);
    when(resourceRepo.existsById(resourceId)).thenReturn(false);
    when(folioMetadataRepo.existsBySrsId(srsId)).thenReturn(false);
    when(resourceGraphService.saveMergingGraph(resourceEntity)).thenReturn(resourceEntity);

    //when
    var result = resourceMarcService.importMarcRecord(inventoryId);

    //then
    verify(applicationEventPublisher).publishEvent(resourceEventCaptor.capture());
    assertThat(resourceEventCaptor.getValue())
      .satisfies(event -> {
        assertThat(event).isInstanceOf(ResourceUpdatedEvent.class);
        assertEquals(resourceEntity, ((ResourceUpdatedEvent) event).resource());
      });
    assertThat(result)
      .satisfies(resourceIdDto -> {
        assertThat(resourceIdDto).isInstanceOf(ResourceIdDto.class);
        assertEquals("1", resourceIdDto.getId());
      });
    assertEquals(LINKED_DATA.name(), resourceModelCaptor.getValue().getFolioMetadata().getSource().name());
  }

  @Test
  void importMarcRecord_shouldThrowNotFoundException() {
    //given
    var inventoryId = UUID.randomUUID().toString();
    when(srsClient.getFormattedSourceStorageInstanceRecordById(inventoryId))
      .thenThrow(FeignException.NotFound.class);

    //expect
    assertThatExceptionOfType(NotFoundException.class)
      .isThrownBy(() -> resourceMarcService.importMarcRecord(inventoryId));
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

    when(srsClient.getFormattedSourceStorageInstanceRecordById(inventoryId))
      .thenReturn(new ResponseEntity<>(marcRecord, HttpStatusCode.valueOf(200)));
    when(objectMapper.writeValueAsString(marcRecord.getParsedRecord().getContent())).thenReturn(marcJson);
    when(marcBib2ldMapper.fromMarcJson(marcJson)).thenReturn(Optional.of(resourceModel));
    when(resourceRepo.existsById(resourceId)).thenReturn(true);

    //expect
    assertThatExceptionOfType(AlreadyExistsException.class)
      .isThrownBy(() -> resourceMarcService.importMarcRecord(inventoryId));
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

    when(srsClient.getFormattedSourceStorageInstanceRecordById(inventoryId))
      .thenReturn(new ResponseEntity<>(marcRecord, HttpStatusCode.valueOf(200)));
    when(objectMapper.writeValueAsString(marcRecord.getParsedRecord().getContent())).thenReturn(marcJson);
    when(marcBib2ldMapper.fromMarcJson(marcJson)).thenReturn(Optional.of(resourceModel));
    when(resourceRepo.existsById(resourceId)).thenReturn(false);
    when(folioMetadataRepo.existsBySrsId(srsId)).thenReturn(true);

    //expect
    assertThatExceptionOfType(AlreadyExistsException.class)
      .isThrownBy(() -> resourceMarcService.importMarcRecord(inventoryId));
  }

  private org.folio.rest.jaxrs.model.Record createRecord(char type, char level) {
    var leader = "04809n   a2200865 i 4500";
    leader = leader.substring(0, 6) + type + level + leader.substring(8);
    var content = Map.of("leader", leader);
    var parsedRecord = new ParsedRecord().withContent(content);
    return new Record().withParsedRecord(parsedRecord);
  }

}
