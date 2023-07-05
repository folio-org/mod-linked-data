package org.folio.linked.data.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.linked.data.test.TestUtil.random;
import static org.folio.linked.data.test.TestUtil.randomLong;
import static org.folio.linked.data.test.TestUtil.randomResource;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import org.folio.linked.data.configuration.properties.BibframeProperties;
import org.folio.linked.data.domain.dto.BibframeResponse;
import org.folio.linked.data.domain.dto.BibframeShort;
import org.folio.linked.data.domain.dto.BibframeShortInfoPage;
import org.folio.linked.data.exception.NotFoundException;
import org.folio.linked.data.mapper.BibframeMapper;
import org.folio.linked.data.model.ResourceHashAndLabel;
import org.folio.linked.data.repo.ResourceRepository;
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
  private BibframeMapper bibframeMapper;

  @Mock
  private BibframeProperties bibframeProperties;

  @Test
  void getResourceById_shouldReturnExistedEntity() {
    // given
    var id = randomLong();
    var existedResource = randomResource();
    when(resourceRepo.findById(id)).thenReturn(Optional.of(existedResource));
    var expectedResponse = random(BibframeResponse.class);
    when(bibframeMapper.map(existedResource)).thenReturn(expectedResponse);

    // when
    var result = resourceService.getBibframeById(id);

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
      () -> resourceService.getBibframeById(notExistedId)
    );

    // then
    assertThat(thrown.getMessage()).isEqualTo("Bibframe record with given id [" + notExistedId + "] is not found");
  }

  @Test
  void getResourceShortInfoPageWithParams_shouldReturnExistedEntitiesShortInfoMapped(
    @Mock Page<ResourceHashAndLabel> pageOfShortEntities, @Mock Page<BibframeShort> pageOfDto) {
    // given
    var pageNumber = 0;
    var pageSize = 10;
    var sort = Sort.by(Sort.Direction.ASC, "resourceHash");
    var profiles = Set.of("profile");
    doReturn(profiles).when(bibframeProperties).getProfiles();
    doReturn(pageOfShortEntities).when(resourceRepo).findResourcesByType(profiles,
      PageRequest.of(pageNumber, pageSize, sort));
    doReturn(pageOfDto).when(pageOfShortEntities)
      .map(ArgumentMatchers.<Function<ResourceHashAndLabel, BibframeShort>>any());
    var expectedResult = random(BibframeShortInfoPage.class);
    doReturn(expectedResult).when(bibframeMapper).map(pageOfDto);

    // when
    var result = resourceService.getBibframeShortInfoPage(pageNumber, pageSize);

    // then
    assertThat(result).isEqualTo(expectedResult);
  }

  @Test
  void getResourceShortInfoPageWithNoParams_shouldReturnExistedEntitiesShortInfoMapped(
    @Mock Page<ResourceHashAndLabel> pageOfShortEntities, @Mock Page<BibframeShort> pageOfDto) {
    // given
    var sort = Sort.by(Sort.Direction.ASC, "resourceHash");
    var profiles = Set.of("profile");
    doReturn(profiles).when(bibframeProperties).getProfiles();
    doReturn(pageOfShortEntities).when(resourceRepo).findResourcesByType(profiles,
      PageRequest.of(0, 100, sort));
    doReturn(pageOfDto).when(pageOfShortEntities)
      .map(ArgumentMatchers.<Function<ResourceHashAndLabel, BibframeShort>>any());
    var expectedResult = random(BibframeShortInfoPage.class);
    doReturn(expectedResult).when(bibframeMapper).map(pageOfDto);

    // when
    var result = resourceService.getBibframeShortInfoPage(null, null);

    // then
    assertThat(result).isEqualTo(expectedResult);
  }

}

