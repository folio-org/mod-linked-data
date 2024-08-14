package org.folio.linked.data.service.resource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.ld.dictionary.PredicateDictionary.INSTANTIATES;
import static org.folio.ld.dictionary.ResourceTypeDictionary.INSTANCE;
import static org.folio.ld.dictionary.ResourceTypeDictionary.WORK;
import static org.folio.linked.data.test.MonographTestUtil.getSampleInstanceResource;
import static org.folio.linked.data.test.TestUtil.random;
import static org.folio.linked.data.test.TestUtil.randomLong;
import static org.folio.linked.data.util.Constants.IS_NOT_FOUND;
import static org.folio.linked.data.util.Constants.RESOURCE_WITH_GIVEN_ID;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.collect.Sets;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import org.folio.linked.data.domain.dto.InstanceField;
import org.folio.linked.data.domain.dto.InstanceRequest;
import org.folio.linked.data.domain.dto.InstanceResponse;
import org.folio.linked.data.domain.dto.InstanceResponseField;
import org.folio.linked.data.domain.dto.ResourceRequestDto;
import org.folio.linked.data.domain.dto.ResourceResponseDto;
import org.folio.linked.data.domain.dto.ResourceShort;
import org.folio.linked.data.domain.dto.ResourceShortInfoPage;
import org.folio.linked.data.domain.dto.WorkField;
import org.folio.linked.data.domain.dto.WorkRequest;
import org.folio.linked.data.domain.dto.WorkResponse;
import org.folio.linked.data.domain.dto.WorkResponseField;
import org.folio.linked.data.exception.NotFoundException;
import org.folio.linked.data.mapper.dto.ResourceDtoMapper;
import org.folio.linked.data.model.ResourceShortInfo;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceEdge;
import org.folio.linked.data.model.entity.event.ResourceCreatedEvent;
import org.folio.linked.data.model.entity.event.ResourceDeletedEvent;
import org.folio.linked.data.model.entity.event.ResourceReplacedEvent;
import org.folio.linked.data.model.entity.event.ResourceUpdatedEvent;
import org.folio.linked.data.repo.FolioMetadataRepository;
import org.folio.linked.data.repo.ResourceEdgeRepository;
import org.folio.linked.data.repo.ResourceRepository;
import org.folio.linked.data.service.resource.meta.MetadataService;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

@UnitTest
@ExtendWith(MockitoExtension.class)
class ResourceServiceTest {

  @InjectMocks
  private ResourceServiceImpl resourceService;

  @Mock
  private FolioMetadataRepository folioMetadataRepo;
  @Mock
  private ResourceRepository resourceRepo;
  @Mock
  private ResourceEdgeRepository edgeRepo;
  @Mock
  private ResourceDtoMapper resourceDtoMapper;
  @Mock
  private ApplicationEventPublisher applicationEventPublisher;
  @Mock
  private MetadataService metadataService;
  @Mock
  private ResourceGraphService resourceGraphService;

  @Test
  void create_shouldPersistMappedResourceAndNotPublishResourceCreatedEvent_forResourceWithNoWork() {
    // given
    var request = new ResourceRequestDto();
    var mapped = new Resource().setId(12345L);
    when(resourceDtoMapper.toEntity(request)).thenReturn(mapped);
    var persisted = new Resource().setId(67890L);
    var expectedResponse = new ResourceResponseDto();
    expectedResponse.setResource(new InstanceResponseField().instance(new InstanceResponse().id("123")));
    when(resourceDtoMapper.toDto(persisted)).thenReturn(expectedResponse);
    when(resourceGraphService.saveMergingGraph(mapped)).thenReturn(persisted);

    // when
    var response = resourceService.createResource(request);

    // then
    assertThat(response).isEqualTo(expectedResponse);
    verify(applicationEventPublisher, never()).publishEvent(any());
  }

  @Test
  void create_shouldPersistMappedResourceAndPublishResourceCreatedEvent_forResourceWithWork() {
    // given
    var request = new ResourceRequestDto();
    var work = new Resource().addTypes(WORK).setId(555L);
    when(resourceDtoMapper.toEntity(request)).thenReturn(work);
    var expectedResponse = new ResourceResponseDto();
    expectedResponse.setResource(new InstanceResponseField().instance(new InstanceResponse().id("123")));
    when(resourceDtoMapper.toDto(work)).thenReturn(expectedResponse);
    when(resourceGraphService.saveMergingGraph(work)).thenReturn(work);

    // when
    var response = resourceService.createResource(request);

    // then
    assertThat(response).isEqualTo(expectedResponse);
    var resourceCreateEventCaptor = ArgumentCaptor.forClass(ResourceCreatedEvent.class);
    verify(applicationEventPublisher).publishEvent(resourceCreateEventCaptor.capture());
    assertThat(work.getId()).isEqualTo(resourceCreateEventCaptor.getValue().resource().getId());
  }

  @Test
  void create_shouldPersistMappedResourceAndPublishResourceCreatedEvent_forResourceIsWork() {
    // given
    var request = new ResourceRequestDto();
    var work = new Resource().addTypes(WORK).setId(444L);
    when(resourceDtoMapper.toEntity(request)).thenReturn(work);
    var expectedResponse = new ResourceResponseDto();
    expectedResponse.setResource(new InstanceResponseField().instance(new InstanceResponse().id("123")));
    when(resourceDtoMapper.toDto(work)).thenReturn(expectedResponse);
    when(resourceGraphService.saveMergingGraph(work)).thenReturn(work);

    // when
    var response = resourceService.createResource(request);

    // then
    assertThat(response).isEqualTo(expectedResponse);
    var resourceCreateEventCaptor = ArgumentCaptor.forClass(ResourceCreatedEvent.class);
    verify(applicationEventPublisher).publishEvent(resourceCreateEventCaptor.capture());
    assertThat(work.getId()).isEqualTo(resourceCreateEventCaptor.getValue().resource().getId());
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
  void getResourceById_shouldThrowNotFoundException_ifNoEntityExists() {
    // given
    var notExistedId = randomLong();
    when(resourceRepo.findById(notExistedId)).thenReturn(Optional.empty());

    // when
    var thrown = assertThrows(
      NotFoundException.class,
      () -> resourceService.getResourceById(notExistedId)
    );

    // then
    assertThat(thrown.getMessage()).isEqualTo(RESOURCE_WITH_GIVEN_ID + notExistedId + IS_NOT_FOUND);
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
  void getResourceIdByInventoryId_shouldThrowNotFoundException_ifNoEntityExistsWithGivenInventoryId() {
    // given
    var inventoryId = UUID.randomUUID().toString();

    // when
    var thrown = assertThrows(
      NotFoundException.class,
      () -> resourceService.getResourceIdByInventoryId(inventoryId)
    );

    // then
    assertThat(thrown.getMessage())
      .isEqualTo("Resource with given inventory id [" + inventoryId + "] is not found");
  }

  @Test
  void getResourceShortInfoPageWithParams_shouldReturnExistedEntitiesShortInfoMapped(
    @Mock Page<ResourceShortInfo> pageOfShortEntities, @Mock Page<ResourceShort> pageOfDto) {
    // given
    var pageNumber = 0;
    var pageSize = 10;
    var sort = Sort.by(Sort.Direction.ASC, "label");
    var types = Sets.newHashSet(INSTANCE.getUri());
    doReturn(pageOfShortEntities).when(resourceRepo).findAllShortByType(types,
      PageRequest.of(pageNumber, pageSize, sort));
    doReturn(pageOfDto).when(pageOfShortEntities)
      .map(ArgumentMatchers.<Function<ResourceShortInfo, ResourceShort>>any());
    var expectedResult = random(ResourceShortInfoPage.class);
    doReturn(expectedResult).when(resourceDtoMapper).map(pageOfDto);

    // when
    var result = resourceService.getResourceShortInfoPage(types.iterator().next(), pageNumber, pageSize);

    // then
    assertThat(result).isEqualTo(expectedResult);
  }

  @Test
  void getResourceShortInfoPageWithNoParams_shouldReturnExistedEntitiesShortInfoMapped(
    @Mock Page<ResourceShortInfo> pageOfShortEntities, @Mock Page<ResourceShort> pageOfDto) {
    // given
    var types = Sets.newHashSet(INSTANCE.getUri());
    var sort = Sort.by(Sort.Direction.ASC, "label");
    doReturn(pageOfShortEntities).when(resourceRepo).findAllShortByType(types,
      PageRequest.of(0, 100, sort));
    doReturn(pageOfDto).when(pageOfShortEntities)
      .map(ArgumentMatchers.<Function<ResourceShortInfo, ResourceShort>>any());
    var expectedResult = random(ResourceShortInfoPage.class);
    doReturn(expectedResult).when(resourceDtoMapper).map(pageOfDto);

    // when
    var result = resourceService.getResourceShortInfoPage(types.iterator().next(), null, null);

    // then
    assertThat(result).isEqualTo(expectedResult);
  }

  @Test
  void update_shouldSaveUpdatedResourceAndSendResourceUpdatedEvent_forResourceWithSameId() {
    // given
    var id = randomLong();
    var workDto = new ResourceRequestDto().resource(new WorkField().work(new WorkRequest()));
    var oldWork = new Resource().setId(id).addTypes(WORK).setLabel("oldWork");
    when(resourceRepo.findById(id)).thenReturn(Optional.of(oldWork));
    var work = new Resource().setId(id).setLabel("saved").addTypes(WORK);
    when(resourceDtoMapper.toEntity(workDto)).thenReturn(work);
    var expectedDto = new ResourceResponseDto().resource(
      new WorkResponseField().work(new WorkResponse().id(id.toString()))
    );
    when(resourceDtoMapper.toDto(work)).thenReturn(expectedDto);
    when(resourceGraphService.saveMergingGraph(work)).thenReturn(work);

    // when
    var result = resourceService.updateResource(id, workDto);

    // then
    assertThat(expectedDto).isEqualTo(result);
    verify(resourceGraphService).breakEdgesAndDelete(oldWork);
    verify(resourceGraphService).saveMergingGraph(work);
    verify(applicationEventPublisher).publishEvent(new ResourceUpdatedEvent(work));
  }

  @Test
  void update_shouldSaveUpdatedResourceAndSendReplaceEvent_forResourceWithDifferentId() {
    // given
    var oldId = randomLong();
    var newId = randomLong();
    var oldInstance = new Resource().setId(oldId).addTypes(INSTANCE).setLabel("oldInstance");
    when(resourceRepo.findById(oldId)).thenReturn(Optional.of(oldInstance));
    var mapped = new Resource().setId(newId).setLabel("mapped");
    var instanceDto =
      new ResourceRequestDto().resource(new InstanceField().instance(new InstanceRequest()));
    when(resourceDtoMapper.toEntity(instanceDto)).thenReturn(mapped);
    var persisted = new Resource().setId(newId).setLabel("saved");
    var expectedDto = new ResourceResponseDto().resource(
      new InstanceResponseField().instance(new InstanceResponse().id(newId.toString()))
    );
    when(resourceDtoMapper.toDto(persisted)).thenReturn(expectedDto);
    when(resourceGraphService.saveMergingGraph(mapped)).thenReturn(persisted);

    // when
    var result = resourceService.updateResource(oldId, instanceDto);

    // then
    assertThat(expectedDto).isEqualTo(result);
    verify(resourceGraphService).breakEdgesAndDelete(oldInstance);
    verify(resourceGraphService).saveMergingGraph(mapped);
    verify(applicationEventPublisher).publishEvent(new ResourceReplacedEvent(oldInstance, mapped));
  }

  @Test
  void delete_shouldDeleteWorkAndPublishResourceDeletedEvent() {
    // given
    var work = new Resource().setId(randomLong()).addTypes(WORK);
    when(resourceRepo.findById(work.getId())).thenReturn(Optional.of(work));

    // when
    resourceService.deleteResource(work.getId());

    // then
    verify(resourceGraphService).breakEdgesAndDelete(work);
    var resourceDeletedEventCaptor = ArgumentCaptor.forClass(ResourceDeletedEvent.class);
    verify(applicationEventPublisher).publishEvent(resourceDeletedEventCaptor.capture());
    assertThat(work).isEqualTo(resourceDeletedEventCaptor.getValue().resource());
  }

  @Test
  void delete_shouldDeleteInstanceAndPublishResourceDeletedEvent() {
    // given
    var work = new Resource().setId(randomLong()).addTypes(WORK);
    var instance = new Resource().setId(randomLong()).addTypes(INSTANCE);
    var instance2 = new Resource().setId(randomLong()).addTypes(INSTANCE);
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
    var resourceDeletedEventCaptor = ArgumentCaptor.forClass(ResourceDeletedEvent.class);
    verify(applicationEventPublisher).publishEvent(resourceDeletedEventCaptor.capture());
    assertThat(instance).isEqualTo(resourceDeletedEventCaptor.getValue().resource());
  }

}
