package org.folio.linked.data.service.resource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.ld.dictionary.PredicateDictionary.INSTANTIATES;
import static org.folio.ld.dictionary.ResourceTypeDictionary.INSTANCE;
import static org.folio.ld.dictionary.ResourceTypeDictionary.WORK;
import static org.folio.linked.data.test.MonographTestUtil.getSampleInstanceResource;
import static org.folio.linked.data.test.TestUtil.emptyRequestProcessingException;
import static org.folio.linked.data.test.TestUtil.random;
import static org.folio.linked.data.test.TestUtil.randomLong;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.folio.linked.data.domain.dto.InstanceField;
import org.folio.linked.data.domain.dto.InstanceRequest;
import org.folio.linked.data.domain.dto.InstanceResponse;
import org.folio.linked.data.domain.dto.InstanceResponseField;
import org.folio.linked.data.domain.dto.ResourceRequestDto;
import org.folio.linked.data.domain.dto.ResourceResponseDto;
import org.folio.linked.data.domain.dto.WorkField;
import org.folio.linked.data.domain.dto.WorkRequest;
import org.folio.linked.data.domain.dto.WorkResponse;
import org.folio.linked.data.domain.dto.WorkResponseField;
import org.folio.linked.data.exception.RequestProcessingException;
import org.folio.linked.data.exception.RequestProcessingExceptionBuilder;
import org.folio.linked.data.mapper.dto.resource.ResourceDtoMapper;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceEdge;
import org.folio.linked.data.repo.FolioMetadataRepository;
import org.folio.linked.data.repo.ResourceRepository;
import org.folio.linked.data.service.profile.ResourceProfileLinkingService;
import org.folio.linked.data.service.resource.copy.ResourceCopyService;
import org.folio.linked.data.service.resource.events.ResourceEventsPublisher;
import org.folio.linked.data.service.resource.graph.ResourceGraphService;
import org.folio.linked.data.service.resource.graph.SaveGraphResult;
import org.folio.linked.data.service.resource.marc.RawMarcService;
import org.folio.linked.data.service.resource.meta.MetadataService;
import org.folio.spring.FolioExecutionContext;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@UnitTest
@ExtendWith(MockitoExtension.class)
class ResourceServiceImplTest {

  @InjectMocks
  private ResourceServiceImpl resourceService;

  @Mock
  private FolioMetadataRepository folioMetadataRepo;
  @Mock
  private ResourceRepository resourceRepo;
  @Mock
  private ResourceDtoMapper resourceDtoMapper;
  @Mock
  private ResourceEventsPublisher resourceEventsPublisher;
  @Mock
  private MetadataService metadataService;
  @Mock
  private ResourceGraphService resourceGraphService;
  @Mock
  private RequestProcessingExceptionBuilder exceptionBuilder;
  @Mock
  private FolioExecutionContext folioExecutionContext;
  @Mock
  private ResourceCopyService resourceCopyService;
  @Mock
  private ResourceProfileLinkingService resourceProfileLinkingService;
  @Mock
  private RawMarcService rawMarcService;

  @Test
  void create_shouldPersistMappedResourceAndPublishResourceCreatedEvent() {
    // given
    var request = new ResourceRequestDto().resource(new InstanceField().instance(new InstanceRequest(3, List.of())));
    var work = new Resource().addTypes(WORK).setIdAndRefreshEdges(555L);
    when(resourceDtoMapper.toEntity(request)).thenReturn(work);
    var expectedResponse = new ResourceResponseDto();
    expectedResponse.setResource(new InstanceResponseField().instance(new InstanceResponse(3).id("123")));
    when(resourceDtoMapper.toDto(work)).thenReturn(expectedResponse);
    var saveGraphResult = new SaveGraphResult(work);
    when(resourceGraphService.saveMergingGraph(work)).thenReturn(saveGraphResult);

    // when
    var response = resourceService.createResource(request);

    // then
    assertThat(response).isEqualTo(expectedResponse);
    verify(resourceEventsPublisher).emitEventsForCreate(saveGraphResult);
  }

  @Test
  void create_shouldPersistMappedResourceAndPublishResourceCreatedEvent_forResourceIsWork() {
    // given
    var profileId = 12;
    var request = new ResourceRequestDto().resource(
      new WorkField().work(
        new WorkRequest(2, List.of()).profileId(profileId)
      )
    );
    var work = new Resource().addTypes(WORK).setIdAndRefreshEdges(444L);
    when(resourceDtoMapper.toEntity(request)).thenReturn(work);
    var expectedResponse = new ResourceResponseDto();
    expectedResponse.setResource(new InstanceResponseField().instance(new InstanceResponse(3).id("123")));
    when(resourceDtoMapper.toDto(work)).thenReturn(expectedResponse);
    var saveGraphResult = new SaveGraphResult(work);
    when(resourceGraphService.saveMergingGraph(work)).thenReturn(saveGraphResult);

    // when
    var response = resourceService.createResource(request);

    // then
    assertThat(response).isEqualTo(expectedResponse);
    verify(resourceEventsPublisher).emitEventsForCreate(saveGraphResult);
    verify(resourceProfileLinkingService).linkResourceToProfile(work, profileId);
  }

  @Test
  void create_shouldNotPersistResourceAndThrowAlreadyExists_forExistedResource() {
    // given
    var request = new ResourceRequestDto().resource(new InstanceField().instance(new InstanceRequest(3, List.of())));
    var mapped = new Resource().setIdAndRefreshEdges(12345L);
    when(resourceDtoMapper.toEntity(request)).thenReturn(mapped);
    when(resourceRepo.existsById(mapped.getId())).thenReturn(true);
    var expectedException = emptyRequestProcessingException();
    when(exceptionBuilder.alreadyExistsException(anyString(), anyString()))
      .thenReturn(expectedException);

    // when
    var thrown = assertThrows(
      RequestProcessingException.class,
      () -> resourceService.createResource(request)
    );

    // then
    assertThat(thrown).isEqualTo(expectedException);
    verify(resourceGraphService, never()).saveMergingGraph(any());
  }

  @Test
  void getResourceById_shouldReturnExistedEntity() {
    // given
    var id = randomLong();
    var existedResource = getSampleInstanceResource();
    when(resourceRepo.findById(id)).thenReturn(Optional.of(existedResource));
    var expectedResponse = random(ResourceResponseDto.class);
    when(resourceDtoMapper.toDto(existedResource)).thenReturn(expectedResponse);
    // when
    var result = resourceService.getResourceById(id);

    // then
    assertThat(result).isEqualTo(expectedResponse);
  }

  @Test
  void getResourceById_shouldThrowRequestProcessingException_ifNoEntityExists() {
    // given
    var notExistedId = randomLong();
    when(resourceRepo.findById(notExistedId)).thenReturn(Optional.empty());
    var expectedException = emptyRequestProcessingException();
    when(exceptionBuilder.notFoundLdResourceByIdException(anyString(), anyString()))
      .thenReturn(expectedException);

    // when
    var thrown = assertThrows(
      RequestProcessingException.class,
      () -> resourceService.getResourceById(notExistedId)
    );

    // then
    assertThat(thrown).isEqualTo(expectedException);
  }

  @Test
  void getResourceIdByInventoryId_shouldReturnIdOfExistedEntity() {
    // given
    var inventoryId = UUID.randomUUID().toString();
    var existedResource = getSampleInstanceResource();
    when(folioMetadataRepo.findIdByInventoryId(inventoryId)).thenReturn(Optional.of(existedResource::getId));

    // when
    var result = resourceService.getResourceIdByInventoryId(inventoryId);

    // then
    assertThat(result.getId()).isEqualTo(String.valueOf(existedResource.getId()));
  }

  @Test
  void getResourceIdByInventoryId_shouldThrowRequestProcessingException_ifNoEntityExistsWithGivenInventoryId() {
    // given
    var inventoryId = UUID.randomUUID().toString();
    var expectedException = emptyRequestProcessingException();
    when(exceptionBuilder.notFoundLdResourceByInventoryIdException(anyString()))
      .thenReturn(expectedException);

    // when
    var thrown = assertThrows(
      RequestProcessingException.class,
      () -> resourceService.getResourceIdByInventoryId(inventoryId)
    );

    // then
    assertThat(thrown).isEqualTo(expectedException);
  }

  @Test
  void update_shouldSaveUpdatedResourceAndSendResourceUpdatedEvent_forResourceWithSameId() {
    // given
    var id = randomLong();
    var workDto = new ResourceRequestDto().resource(new WorkField().work(new WorkRequest(2, List.of())));
    var oldWork = new Resource().setIdAndRefreshEdges(id).addTypes(WORK).setLabel("oldWork");
    when(resourceRepo.findById(id)).thenReturn(Optional.of(oldWork));
    var work = new Resource().setIdAndRefreshEdges(id).setLabel("saved").addTypes(WORK);
    var saveGraphResult = new SaveGraphResult(work);
    when(resourceDtoMapper.toEntity(workDto)).thenReturn(work);
    var expectedDto = new ResourceResponseDto().resource(
      new WorkResponseField().work(new WorkResponse(2).id(id.toString()))
    );
    when(resourceDtoMapper.toDto(work)).thenReturn(expectedDto);
    when(resourceGraphService.saveMergingGraph(work)).thenReturn(saveGraphResult);
    when(folioExecutionContext.getUserId()).thenReturn(UUID.randomUUID());

    // when
    var result = resourceService.updateResource(id, workDto);

    // then
    assertThat(expectedDto).isEqualTo(result);
    verify(resourceGraphService).breakEdgesAndDelete(oldWork);
    verify(resourceGraphService).saveMergingGraph(work);
    verify(folioExecutionContext).getUserId();
    verify(resourceEventsPublisher).emitEventsForUpdate(oldWork, saveGraphResult);
    verify(resourceCopyService).copyEdgesAndProperties(oldWork, work);
  }

  @Test
  void update_shouldSaveUpdatedResourceAndSendReplaceEvent_forResourceWithDifferentId() {
    // given
    var profileId = 125;
    var oldId = randomLong();
    var newId = randomLong();
    final var unmappedMarc = "{}";
    var oldInstance = new Resource().setIdAndRefreshEdges(oldId).addTypes(INSTANCE).setLabel("oldInstance");
    when(resourceRepo.findById(oldId)).thenReturn(Optional.of(oldInstance));
    var mapped = new Resource().setIdAndRefreshEdges(newId).setLabel("mapped");
    var instanceDto = new ResourceRequestDto().resource(
      new InstanceField().instance(
        new InstanceRequest(3, List.of()).profileId(profileId)
      )
    );
    when(resourceDtoMapper.toEntity(instanceDto)).thenReturn(mapped);
    var persisted = new Resource().setIdAndRefreshEdges(newId).setLabel("saved");
    var saveGraphResult = new SaveGraphResult(persisted);
    var expectedDto = new ResourceResponseDto().resource(
      new InstanceResponseField().instance(new InstanceResponse(3).id(newId.toString()))
    );
    when(resourceDtoMapper.toDto(persisted)).thenReturn(expectedDto);
    when(resourceGraphService.saveMergingGraph(mapped)).thenReturn(saveGraphResult);
    when(rawMarcService.getRawMarc(oldInstance)).thenReturn(Optional.of(unmappedMarc));

    // when
    var result = resourceService.updateResource(oldId, instanceDto);

    // then
    assertThat(expectedDto).isEqualTo(result);
    verify(resourceGraphService).breakEdgesAndDelete(oldInstance);
    verify(resourceGraphService).saveMergingGraph(mapped);
    verify(resourceEventsPublisher).emitEventsForUpdate(oldInstance, saveGraphResult);
    verify(resourceCopyService).copyEdgesAndProperties(oldInstance, mapped);
    verify(rawMarcService).saveRawMarc(persisted, unmappedMarc);
    verify(resourceProfileLinkingService).linkResourceToProfile(persisted, profileId);
  }

  @Test
  void delete_shouldDeleteWorkAndPublishResourceDeletedEvent() {
    // given
    var work = new Resource().setIdAndRefreshEdges(randomLong()).addTypes(WORK);
    when(resourceRepo.findById(work.getId())).thenReturn(Optional.of(work));

    // when
    resourceService.deleteResource(work.getId());

    // then
    verify(resourceGraphService).breakEdgesAndDelete(work);
    verify(resourceEventsPublisher).emitEventForDelete(work);
  }

  @Test
  void delete_shouldDeleteInstanceAndPublishResourceDeletedEvent() {
    // given
    var work = new Resource().setIdAndRefreshEdges(randomLong()).addTypes(WORK);
    var instance = new Resource().setIdAndRefreshEdges(randomLong()).addTypes(INSTANCE);
    var instance2 = new Resource().setIdAndRefreshEdges(randomLong()).addTypes(INSTANCE);
    var edge1 = new ResourceEdge(instance, work, INSTANTIATES);
    var edge2 = new ResourceEdge(instance2, work, INSTANTIATES);
    work.addIncomingEdge(edge1);
    work.addIncomingEdge(edge2);
    instance.addOutgoingEdge(edge1);
    instance2.addOutgoingEdge(edge2);
    when(resourceRepo.findById(instance.getId())).thenReturn(Optional.of(instance));

    // when
    resourceService.deleteResource(instance.getId());

    // then
    verify(resourceGraphService).breakEdgesAndDelete(instance);
    verify(resourceEventsPublisher).emitEventForDelete(instance);
  }

}
