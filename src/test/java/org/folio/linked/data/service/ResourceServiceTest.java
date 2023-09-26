package org.folio.linked.data.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.linked.data.test.TestUtil.random;
import static org.folio.linked.data.test.TestUtil.randomLong;
import static org.folio.linked.data.util.Constants.IS_NOT_FOUND;
import static org.folio.linked.data.util.Constants.RESOURCE_WITH_GIVEN_ID;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.collect.Sets;
import java.util.Optional;
import java.util.function.Function;
import org.folio.linked.data.domain.dto.Instance;
import org.folio.linked.data.domain.dto.InstanceField;
import org.folio.linked.data.domain.dto.ResourceDto;
import org.folio.linked.data.domain.dto.ResourceShort;
import org.folio.linked.data.domain.dto.ResourceShortInfoPage;
import org.folio.linked.data.exception.NotFoundException;
import org.folio.linked.data.mapper.ResourceMapper;
import org.folio.linked.data.model.ResourceShortInfo;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.repo.ResourceRepository;
import org.folio.linked.data.test.TestUtil;
import org.folio.linked.data.util.BibframeConstants;
import org.folio.search.domain.dto.BibframeIndex;
import org.folio.spring.test.type.UnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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
  private KafkaSender kafkaSender;

  @Test
  void create_shouldPersistMappedBibframeAndSendItToKafka() {
    // given
    var request = new ResourceDto();
    var mapped = new Resource().setResourceHash(12345L);
    when(resourceMapper.toEntity(request)).thenReturn(mapped);
    var persisted = new Resource().setResourceHash(67890L);
    when(resourceRepo.save(mapped)).thenReturn(persisted);
    var expectedIndex = new BibframeIndex(persisted.getResourceHash().toString());
    when(resourceMapper.mapToIndex(persisted)).thenReturn(expectedIndex);
    var expectedResponse = new ResourceDto();
    expectedResponse.setResource(new InstanceField().instance(new Instance().id("123")));
    when(resourceMapper.toDto(persisted)).thenReturn(expectedResponse);

    // when
    ResourceDto response = resourceService.createResource(request);

    // then
    verify(kafkaSender).sendResourceCreated(expectedIndex);
    assertThat(response).isEqualTo(expectedResponse);
  }

  @Test
  void getResourceById_shouldReturnExistedEntity() {
    // given
    var id = randomLong();
    var existedResource = TestUtil.bibframeSampleResource();
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
    NotFoundException thrown = assertThrows(
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
    var sort = Sort.by(Sort.Direction.ASC, "resourceHash");
    var types = Sets.newHashSet(BibframeConstants.INSTANCE);
    doReturn(pageOfShortEntities).when(resourceRepo).findResourcesByType(types,
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
    var types = Sets.newHashSet(BibframeConstants.INSTANCE);
    var sort = Sort.by(Sort.Direction.ASC, "resourceHash");
    doReturn(pageOfShortEntities).when(resourceRepo).findResourcesByType(types,
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
  void delete_shouldDeleteBibframeAndSendItToKafka() {
    // given
    var id = randomLong();

    // when
    resourceService.deleteResource(id);

    // then
    verify(resourceRepo).deleteById(id);
    verify(kafkaSender).sendResourceDeleted(id);
  }

}

