package org.folio.linked.data.service.resource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.folio.linked.data.test.MonographTestUtil.getSampleInstanceResource;
import static org.folio.linked.data.test.MonographTestUtil.getSampleWork;
import static org.folio.linked.data.test.TestUtil.random;
import static org.folio.linked.data.test.TestUtil.randomLong;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;
import org.folio.ld.dictionary.model.InstanceMetadata;
import org.folio.linked.data.domain.dto.ResourceMarcViewDto;
import org.folio.linked.data.exception.NotFoundException;
import org.folio.linked.data.exception.ValidationException;
import org.folio.linked.data.mapper.ResourceModelMapper;
import org.folio.linked.data.mapper.dto.ResourceDtoMapper;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.event.ResourceCreatedEvent;
import org.folio.linked.data.model.entity.event.ResourceReplacedEvent;
import org.folio.linked.data.model.entity.event.ResourceUpdatedEvent;
import org.folio.linked.data.repo.InstanceMetadataRepository;
import org.folio.linked.data.repo.ResourceEdgeRepository;
import org.folio.linked.data.repo.ResourceRepository;
import org.folio.marc4ld.service.ld2marc.Bibframe2MarcMapper;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

@UnitTest
@ExtendWith(MockitoExtension.class)
class ResourceMarcServiceTest {

  @InjectMocks
  private ResourceMarcServiceImpl resourceMarcService;

  @Mock
  private InstanceMetadataRepository instanceMetadataRepo;
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
  private ApplicationEventPublisher applicationEventPublisher;
  @Mock
  private ResourceGraphService resourceGraphService;

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
  void saveMarcResource_shouldCreateNewResource_ifGivenModelDoesNotExistsByIdAndInventoryId() {
    // given
    var id = randomLong();
    var model = new org.folio.ld.dictionary.model.Resource().setId(id);
    var mapped = new Resource().setId(id);
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
  void saveMarcResource_shouldUpdateResource_ifGivenModelExistsById() {
    // given
    var id = randomLong();
    var model = new org.folio.ld.dictionary.model.Resource().setId(id);
    var mapped = new Resource().setId(id);
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
  void saveMarcResource_shouldReplaceResource_ifGivenModelExistsByInventoryIdButNotById() {
    // given
    var id = randomLong();
    var invId = UUID.randomUUID().toString();
    var existed = new Resource().setId(id).setManaged(true);
    doReturn(Optional.of(existed)).when(resourceRepo).findByInstanceMetadataInventoryId(invId);
    var existedMetadata = new org.folio.linked.data.model.entity.InstanceMetadata(existed).setInventoryId(invId);
    doReturn(Optional.of(existedMetadata)).when(instanceMetadataRepo).findById(id);
    var model = new org.folio.ld.dictionary.model.Resource()
      .setId(id)
      .setInstanceMetadata(new InstanceMetadata().setInventoryId(invId));
    var mapped = new Resource().setId(id);
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

}
