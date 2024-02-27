package org.folio.linked.data.service;

import static com.google.inject.matcher.Matchers.any;
import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.ld.dictionary.PredicateDictionary.INSTANTIATES;
import static org.folio.ld.dictionary.ResourceTypeDictionary.INSTANCE;
import static org.folio.ld.dictionary.ResourceTypeDictionary.WORK;
import static org.folio.linked.data.test.MonographTestUtil.getSampleInstanceResource;
import static org.folio.linked.data.test.TestUtil.random;
import static org.folio.linked.data.test.TestUtil.randomLong;
import static org.folio.linked.data.util.Constants.IS_NOT_FOUND;
import static org.folio.linked.data.util.Constants.RESOURCE_WITH_GIVEN_ID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.collect.Sets;
import java.util.Optional;
import java.util.function.Function;
import org.folio.linked.data.domain.dto.Instance;
import org.folio.linked.data.domain.dto.InstanceField;
import org.folio.linked.data.domain.dto.ResourceDto;
import org.folio.linked.data.domain.dto.ResourceGraphDto;
import org.folio.linked.data.domain.dto.ResourceShort;
import org.folio.linked.data.domain.dto.ResourceShortInfoPage;
import org.folio.linked.data.domain.dto.Work;
import org.folio.linked.data.domain.dto.WorkField;
import org.folio.linked.data.exception.NotFoundException;
import org.folio.linked.data.mapper.ResourceMapper;
import org.folio.linked.data.model.ResourceShortInfo;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceEdge;
import org.folio.linked.data.model.entity.event.ResourceCreatedEvent;
import org.folio.linked.data.model.entity.event.ResourceDeletedEvent;
import org.folio.linked.data.model.entity.event.ResourceUpdatedEvent;
import org.folio.linked.data.repo.ResourceRepository;
import org.folio.spring.test.type.UnitTest;
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
  private ResourceRepository resourceRepo;

  @Mock
  private ResourceMapper resourceMapper;

  @Mock
  private ApplicationEventPublisher applicationEventPublisher;

  @Test
  void create_shouldPersistMappedResourceAndNotPublishResourceCreatedEvent_forResourceWithNoWork() {
    // given
    var request = new ResourceDto();
    var mapped = new Resource().setResourceHash(12345L);
    when(resourceMapper.toEntity(request)).thenReturn(mapped);
    var persisted = new Resource().setResourceHash(67890L);
    when(resourceRepo.save(mapped)).thenReturn(persisted);
    var expectedResponse = new ResourceDto();
    expectedResponse.setResource(new InstanceField().instance(new Instance().id("123")));
    when(resourceMapper.toDto(persisted)).thenReturn(expectedResponse);

    // when
    var response = resourceService.createResource(request);

    // then
    assertThat(response).isEqualTo(expectedResponse);
    verify(applicationEventPublisher, never()).publishEvent(any());
  }

  @Test
  void create_shouldPersistMappedResourceAndPublishResourceCreatedEvent_forResourceWithWork() {
    // given
    var request = new ResourceDto();
    var mapped = new Resource().setResourceHash(12345L);
    when(resourceMapper.toEntity(request)).thenReturn(mapped);
    var persisted = new Resource().setResourceHash(67890L);
    var work = new Resource().addType(WORK);
    persisted.getOutgoingEdges().add(new ResourceEdge(persisted, work, INSTANTIATES));
    when(resourceRepo.save(mapped)).thenReturn(persisted);
    var expectedResponse = new ResourceDto();
    expectedResponse.setResource(new InstanceField().instance(new Instance().id("123")));
    when(resourceMapper.toDto(persisted)).thenReturn(expectedResponse);

    // when
    var response = resourceService.createResource(request);

    // then
    assertThat(response).isEqualTo(expectedResponse);
    var resourceCreateEventCaptor = ArgumentCaptor.forClass(ResourceCreatedEvent.class);
    verify(applicationEventPublisher).publishEvent(resourceCreateEventCaptor.capture());
    assertEquals(work, resourceCreateEventCaptor.getValue().work());
  }

  @Test
  void create_shouldPersistMappedResourceAndPublishResourceCreatedEvent_forResourceIsWork() {
    // given
    var request = new ResourceDto();
    var mapped = new Resource().setResourceHash(12345L);
    when(resourceMapper.toEntity(request)).thenReturn(mapped);
    var work = new Resource().addType(WORK);
    when(resourceRepo.save(mapped)).thenReturn(work);
    var expectedResponse = new ResourceDto();
    expectedResponse.setResource(new InstanceField().instance(new Instance().id("123")));
    when(resourceMapper.toDto(work)).thenReturn(expectedResponse);

    // when
    var response = resourceService.createResource(request);

    // then
    assertThat(response).isEqualTo(expectedResponse);
    var resourceCreateEventCaptor = ArgumentCaptor.forClass(ResourceCreatedEvent.class);
    verify(applicationEventPublisher).publishEvent(resourceCreateEventCaptor.capture());
    assertEquals(work, resourceCreateEventCaptor.getValue().work());
  }

  @Test
  void getResourceById_shouldReturnExistedEntity() {
    // given
    var id = randomLong();
    var existedResource = getSampleInstanceResource();
    when(resourceRepo.findById(id)).thenReturn(Optional.of(existedResource));
    var expectedResponse = random(ResourceDto.class);
    when(resourceMapper.toDto(existedResource)).thenReturn(expectedResponse);

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
    doReturn(expectedResult).when(resourceMapper).map(pageOfDto);

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
    doReturn(expectedResult).when(resourceMapper).map(pageOfDto);

    // when
    var result = resourceService.getResourceShortInfoPage(types.iterator().next(), null, null);

    // then
    assertThat(result).isEqualTo(expectedResult);
  }

  @Test
  void update_shouldSaveUpdatedResourceAndSendResourceUpdatedEvent_forWork() {
    // given
    var id = randomLong();
    var workDto = new ResourceDto().resource(new WorkField().work(new Work().id(id.toString())));
    var oldWork = new Resource().setResourceHash(id).addType(WORK).setLabel("oldWork");
    when(resourceRepo.findById(id)).thenReturn(Optional.of(oldWork));
    var mapped = new Resource().setResourceHash(id).setLabel("mapped");
    when(resourceMapper.toEntity(workDto)).thenReturn(mapped);
    var persisted = new Resource().setResourceHash(id).setLabel("saved").addType(WORK);
    when(resourceRepo.save(mapped)).thenReturn(persisted);
    var expectedDto = new ResourceDto().resource(new WorkField().work(new Work().id(id.toString())));
    when(resourceMapper.toDto(persisted)).thenReturn(expectedDto);

    // when
    var result = resourceService.updateResource(id, workDto);

    // then
    assertEquals(expectedDto, result);
    verify(resourceRepo).delete(oldWork);
    verify(resourceRepo).save(mapped);
    verify(applicationEventPublisher).publishEvent(new ResourceUpdatedEvent(persisted, oldWork));
  }

  @Test
  void update_shouldSaveUpdatedResourceAndSendUpdatedEventForExistedWorkFromInstance_forIncomingInstanceWithNoWork() {
    // given
    var id = randomLong();
    var instanceDto = new ResourceDto().resource(new InstanceField().instance(new Instance().id(id.toString())));
    var oldWork = new Resource().setResourceHash(id).addType(WORK).setLabel("oldWork");
    var oldInstance = new Resource().setResourceHash(id).addType(INSTANCE).setLabel("oldInstance");
    var edge = new ResourceEdge(oldInstance, oldWork, INSTANTIATES);
    oldInstance.getOutgoingEdges().add(edge);
    oldWork.getOutgoingEdges().add(edge);
    when(resourceRepo.findById(id)).thenReturn(Optional.of(oldInstance));
    var mapped = new Resource().setResourceHash(id).setLabel("mapped");
    when(resourceMapper.toEntity(instanceDto)).thenReturn(mapped);
    var persisted = new Resource().setResourceHash(id).setLabel("saved");
    when(resourceRepo.save(mapped)).thenReturn(persisted);
    var expectedDto = new ResourceDto().resource(new InstanceField().instance(new Instance().id(id.toString())));
    when(resourceMapper.toDto(persisted)).thenReturn(expectedDto);

    // when
    var result = resourceService.updateResource(id, instanceDto);

    // then
    assertEquals(expectedDto, result);
    assertThat(oldWork.getIncomingEdges()).doesNotContain(edge);
    verify(resourceRepo).delete(oldWork);
    verify(resourceRepo).save(mapped);
    verify(applicationEventPublisher).publishEvent(eq(new ResourceUpdatedEvent(oldWork, null)));
  }

  @Test
  void update_shouldSaveUpdatedResourceAndNotSendResourceUpdatedEvents_forInstanceWithNoWork() {
    // given
    var id = randomLong();
    var instanceDto = new ResourceDto().resource(new InstanceField().instance(new Instance().id(id.toString())));
    var oldInstance = new Resource().setResourceHash(id).addType(INSTANCE).setLabel("oldInstance");
    when(resourceRepo.findById(id)).thenReturn(Optional.of(oldInstance));
    var mapped = new Resource().setResourceHash(id).setLabel("mapped");
    when(resourceMapper.toEntity(instanceDto)).thenReturn(mapped);
    var persisted = new Resource().setResourceHash(id).setLabel("saved");
    when(resourceRepo.save(mapped)).thenReturn(persisted);
    var expectedDto = new ResourceDto().resource(new InstanceField().instance(new Instance().id(id.toString())));
    when(resourceMapper.toDto(persisted)).thenReturn(expectedDto);

    // when
    var result = resourceService.updateResource(id, instanceDto);

    // then
    assertEquals(expectedDto, result);
    verify(resourceRepo).delete(oldInstance);
    verify(resourceRepo).save(mapped);
    verify(applicationEventPublisher, never()).publishEvent(any());
  }

  @Test
  void update_shouldSaveUpdatedInstanceAndSendResourceUpdatedEvent_forInstanceWithSameWorkId() {
    // given
    var instanceId = randomLong();
    var workId = randomLong();
    var instanceDto =
      new ResourceDto().resource(new InstanceField().instance(new Instance().id(instanceId.toString())));
    var oldInstance = new Resource().setResourceHash(instanceId).addType(INSTANCE).setLabel("oldInstance");
    var oldWork = new Resource().setResourceHash(workId).addType(WORK).setLabel("oldWork");
    var edge = new ResourceEdge(oldInstance, oldWork, INSTANTIATES);
    oldInstance.getOutgoingEdges().add(edge);
    oldWork.getIncomingEdges().add(edge);
    when(resourceRepo.findById(instanceId)).thenReturn(Optional.of(oldInstance));
    var mapped = new Resource().setResourceHash(instanceId).setLabel("mapped");
    when(resourceMapper.toEntity(instanceDto)).thenReturn(mapped);
    var instance = new Resource().setResourceHash(instanceId).setLabel("saved").addType(INSTANCE);
    var newWork = new Resource().setResourceHash(workId).addType(WORK);
    instance.getOutgoingEdges().add(new ResourceEdge(instance, newWork, INSTANTIATES));
    when(resourceRepo.save(mapped)).thenReturn(instance);
    var expectedDto =
      new ResourceDto().resource(new InstanceField().instance(new Instance().id(instanceId.toString())));
    when(resourceMapper.toDto(instance)).thenReturn(expectedDto);

    // when
    var result = resourceService.updateResource(instanceId, instanceDto);

    // then
    assertEquals(expectedDto, result);
    verify(resourceRepo).delete(oldInstance);
    verify(resourceRepo).save(mapped);
    verify(applicationEventPublisher).publishEvent(new ResourceUpdatedEvent(newWork, oldWork));
  }

  @Test
  void update_shouldSaveUpdatedInstanceAndSendResourceUpdatedEvents_forInstanceWithAnotherWorkId() {
    // given
    var instanceId = randomLong();
    var workId = randomLong();
    var instanceDto =
      new ResourceDto().resource(new InstanceField().instance(new Instance().id(instanceId.toString())));
    var oldInstance = new Resource().setResourceHash(instanceId).addType(INSTANCE).setLabel("oldInstance");
    var oldWork = new Resource().setResourceHash(workId).addType(WORK).setLabel("oldWork");
    var edge = new ResourceEdge(oldInstance, oldWork, INSTANTIATES);
    oldInstance.getOutgoingEdges().add(edge);
    oldWork.getIncomingEdges().add(edge);
    when(resourceRepo.findById(instanceId)).thenReturn(Optional.of(oldInstance));
    var mapped = new Resource().setResourceHash(instanceId).setLabel("mapped");
    when(resourceMapper.toEntity(instanceDto)).thenReturn(mapped);
    var instance = new Resource().setResourceHash(instanceId).setLabel("saved").addType(INSTANCE);
    var newWork = new Resource().setResourceHash(workId + 1).addType(WORK);
    instance.getOutgoingEdges().add(new ResourceEdge(instance, newWork, INSTANTIATES));
    when(resourceRepo.save(mapped)).thenReturn(instance);
    var expectedDto =
      new ResourceDto().resource(new InstanceField().instance(new Instance().id(instanceId.toString())));
    when(resourceMapper.toDto(instance)).thenReturn(expectedDto);

    // when
    var result = resourceService.updateResource(instanceId, instanceDto);

    // then
    assertEquals(expectedDto, result);
    assertThat(oldWork.getIncomingEdges()).doesNotContain(edge);
    verify(resourceRepo).delete(oldInstance);
    verify(resourceRepo).save(mapped);
    verify(applicationEventPublisher).publishEvent(new ResourceUpdatedEvent(newWork, null));
    verify(applicationEventPublisher).publishEvent(new ResourceUpdatedEvent(oldWork, null));
  }

  @Test
  void update_shouldSaveUpdatedInstanceAndSendResourceUpdatedEvent_forInstanceWithJustAddedWork() {
    // given
    var id = randomLong();
    var instanceDto = new ResourceDto().resource(new InstanceField().instance(new Instance().id(id.toString())));
    var oldInstance = new Resource().addType(INSTANCE).setLabel("oldInstance");
    when(resourceRepo.findById(id)).thenReturn(Optional.of(oldInstance));
    var mapped = new Resource().setResourceHash(id).setLabel("mapped");
    when(resourceMapper.toEntity(instanceDto)).thenReturn(mapped);
    var instance = new Resource().setResourceHash(id).setLabel("saved").addType(INSTANCE);
    var newWork = new Resource().setResourceHash(123L).addType(WORK);
    instance.getOutgoingEdges().add(new ResourceEdge(instance, newWork, INSTANTIATES));
    when(resourceRepo.save(mapped)).thenReturn(instance);
    var expectedDto = new ResourceDto().resource(new WorkField().work(new Work().id(id.toString())));
    when(resourceMapper.toDto(instance)).thenReturn(expectedDto);

    // when
    var result = resourceService.updateResource(id, instanceDto);

    // then
    assertEquals(expectedDto, result);
    verify(resourceRepo).delete(oldInstance);
    verify(resourceRepo).save(mapped);
    verify(applicationEventPublisher).publishEvent(new ResourceUpdatedEvent(newWork, null));
  }

  @Test
  void delete_shouldDeleteWorkAndPublishResourceDeletedEvent() {
    // given
    var work = new Resource().setResourceHash(randomLong()).addType(WORK);
    when(resourceRepo.findById(work.getResourceHash())).thenReturn(Optional.of(work));

    // when
    resourceService.deleteResource(work.getResourceHash());

    // then
    verify(resourceRepo).delete(work);
    var resourceDeletedEventCaptor = ArgumentCaptor.forClass(ResourceDeletedEvent.class);
    verify(applicationEventPublisher).publishEvent(resourceDeletedEventCaptor.capture());
    assertEquals(work, resourceDeletedEventCaptor.getValue().work());
  }

  @Test
  void delete_shouldDeleteInstanceAndPublishResourceUpdatedEventWithNewAndOldWorks() {
    // given
    var work = new Resource().setResourceHash(randomLong()).addType(WORK);
    var instance = new Resource().setResourceHash(randomLong()).addType(INSTANCE);
    var instance2 = new Resource().setResourceHash(randomLong()).addType(INSTANCE);
    var edge1 = new ResourceEdge(instance, work, INSTANTIATES);
    var edge2 = new ResourceEdge(instance2, work, INSTANTIATES);
    work.getIncomingEdges().add(edge1);
    work.getIncomingEdges().add(edge2);
    instance.getOutgoingEdges().add(edge1);
    instance2.getOutgoingEdges().add(edge2);
    when(resourceRepo.findById(instance.getResourceHash())).thenReturn(Optional.of(instance));

    // when
    resourceService.deleteResource(instance.getResourceHash());

    // then
    verify(resourceRepo).delete(instance);
    var eventCaptor = ArgumentCaptor.forClass(ResourceUpdatedEvent.class);
    verify(applicationEventPublisher).publishEvent(eventCaptor.capture());
    assertThat(eventCaptor.getValue().newWork().getResourceHash()).isEqualTo(work.getResourceHash());
    assertThat(eventCaptor.getValue().newWork().getIncomingEdges()).hasSize(1);
    assertThat(eventCaptor.getValue().newWork().getIncomingEdges()).contains(edge2);
    assertThat(eventCaptor.getValue().oldWork().getResourceHash()).isEqualTo(work.getResourceHash());
    assertThat(eventCaptor.getValue().oldWork().getIncomingEdges()).hasSize(2);
    assertThat(eventCaptor.getValue().oldWork().getIncomingEdges()).contains(edge1, edge2);
  }

  @Test
  void getResourceGraphById_shouldReturnResourceGraphDto_whenResourceExists() {
    //given
    var id = randomLong();
    var resource = new Resource().setResourceHash(id);
    var expectedResourceGraphDto = new ResourceGraphDto().id(String.valueOf(id));

    when(resourceRepo.findById(id)).thenReturn(Optional.of(resource));
    when(resourceMapper.toResourceGraphDto(resource)).thenReturn(expectedResourceGraphDto);

    //when
    var resourceGraphDto = resourceService.getResourceGraphById(id);

    //then
    assertEquals(expectedResourceGraphDto, resourceGraphDto);
  }

  @Test
  void getResourceGraphById_shouldThrowNotFoundException_whenResourceDoesNotExist() {
    // given
    var id = randomLong();

    when(resourceRepo.findById(id)).thenReturn(Optional.empty());

    // when
    var thrown = assertThrows(NotFoundException.class, () -> resourceService.getResourceGraphById(id));

    // then
    assertThat(thrown.getMessage()).isEqualTo(RESOURCE_WITH_GIVEN_ID + id + IS_NOT_FOUND);
  }

}

