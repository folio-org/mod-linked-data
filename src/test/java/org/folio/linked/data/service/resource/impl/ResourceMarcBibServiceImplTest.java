package org.folio.linked.data.service.resource.impl;

import static java.util.Collections.emptyMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.folio.linked.data.model.entity.ResourceSource.LINKED_DATA;
import static org.folio.linked.data.test.MonographTestUtil.getSampleInstanceResource;
import static org.folio.linked.data.test.MonographTestUtil.getSampleWork;
import static org.folio.linked.data.test.TestUtil.OBJECT_MAPPER;
import static org.folio.linked.data.test.TestUtil.random;
import static org.folio.linked.data.test.TestUtil.randomLong;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.folio.ld.dictionary.model.FolioMetadata;
import org.folio.linked.data.client.SrsClient;
import org.folio.linked.data.domain.dto.ResourceIdDto;
import org.folio.linked.data.domain.dto.ResourceMarcViewDto;
import org.folio.linked.data.domain.dto.ResourceResponseDto;
import org.folio.linked.data.exception.RequestProcessingException;
import org.folio.linked.data.exception.RequestProcessingExceptionBuilder;
import org.folio.linked.data.mapper.ResourceModelMapper;
import org.folio.linked.data.mapper.dto.ResourceDtoMapper;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.event.ResourceEvent;
import org.folio.linked.data.model.entity.event.ResourceUpdatedEvent;
import org.folio.linked.data.repo.FolioMetadataRepository;
import org.folio.linked.data.repo.ResourceRepository;
import org.folio.linked.data.service.resource.ResourceGraphService;
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
    when(ld2MarcMapper.toMarcJson(expectedModelResource))
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
  void getResourceMarcView_shouldThrowRequestProcessingException_ifNoEntityExists() {
    // given
    var notExistedId = randomLong();
    when(resourceRepo.findById(notExistedId))
      .thenReturn(Optional.empty());
    var expectedException = new RequestProcessingException(0, "", new HashMap<>(), "");
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
      .thenReturn(new RequestProcessingException(0, "", emptyMap(), ""));

    // when
    assertThatExceptionOfType(RequestProcessingException.class)
      .isThrownBy(() -> resourceMarcService.getResourceMarcView(notExistedId));
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
    when(srsClient.getSourceStorageInstanceRecordById(inventoryId))
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
    when(srsClient.getSourceStorageInstanceRecordById(inventoryId))
      .thenReturn(new ResponseEntity<>(marcRecord, HttpStatusCode.valueOf(200)));

    //expect
    assertFalse(resourceMarcService.isSupportedByInventoryId(inventoryId));
  }

  @Test
  void isSupportedByInventoryId_shouldThrowRequestProcessingException() {
    //given
    var inventoryId = UUID.randomUUID().toString();
    when(srsClient.getSourceStorageInstanceRecordById(inventoryId))
      .thenThrow(FeignException.NotFound.class);
    when(exceptionBuilder.notFoundSourceRecordException(anyString(), anyString()))
      .thenReturn(new RequestProcessingException(0, "", new HashMap<>(), ""));

    //expect
    assertThatExceptionOfType(RequestProcessingException.class)
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
    when(srsClient.getSourceStorageInstanceRecordById(inventoryId))
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
  void getResourcePreviewByInventoryId_shouldThrowRequestProcessingException() {
    // given
    var inventoryId = UUID.randomUUID().toString();
    when(srsClient.getSourceStorageInstanceRecordById(inventoryId))
      .thenThrow(FeignException.NotFound.class);
    when(exceptionBuilder.notFoundSourceRecordException(anyString(), anyString()))
      .thenReturn(new RequestProcessingException(0, "", new HashMap<>(), ""));

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

    when(srsClient.getSourceStorageInstanceRecordById(inventoryId))
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
  void importMarcRecord_shouldThrowRequestProcessingException() {
    //given
    var inventoryId = UUID.randomUUID().toString();
    when(srsClient.getSourceStorageInstanceRecordById(inventoryId))
      .thenThrow(FeignException.NotFound.class);
    when(exceptionBuilder.notFoundSourceRecordException(anyString(), anyString()))
      .thenReturn(new RequestProcessingException(0, "", new HashMap<>(), ""));

    //expect
    assertThatExceptionOfType(RequestProcessingException.class)
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

    when(srsClient.getSourceStorageInstanceRecordById(inventoryId))
      .thenReturn(new ResponseEntity<>(marcRecord, HttpStatusCode.valueOf(200)));
    when(objectMapper.writeValueAsString(marcRecord.getParsedRecord().getContent())).thenReturn(marcJson);
    when(marcBib2ldMapper.fromMarcJson(marcJson)).thenReturn(Optional.of(resourceModel));
    when(resourceRepo.existsById(resourceId)).thenReturn(true);
    when(exceptionBuilder.alreadyExistsException(anyString(), anyString()))
      .thenReturn(new RequestProcessingException(0, "", new HashMap<>(), ""));

    //expect
    assertThatExceptionOfType(RequestProcessingException.class)
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

    when(srsClient.getSourceStorageInstanceRecordById(inventoryId))
      .thenReturn(new ResponseEntity<>(marcRecord, HttpStatusCode.valueOf(200)));
    when(objectMapper.writeValueAsString(marcRecord.getParsedRecord().getContent())).thenReturn(marcJson);
    when(marcBib2ldMapper.fromMarcJson(marcJson)).thenReturn(Optional.of(resourceModel));
    when(resourceRepo.existsById(resourceId)).thenReturn(false);
    when(folioMetadataRepo.existsBySrsId(srsId)).thenReturn(true);
    when(exceptionBuilder.alreadyExistsException(anyString(), anyString()))
      .thenReturn(new RequestProcessingException(0, "", new HashMap<>(), ""));

    //expect
    assertThatExceptionOfType(RequestProcessingException.class)
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
