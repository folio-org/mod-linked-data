package org.folio.linked.data.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.linked.data.TestUtil.getBibframeJsonNodeSample;
import static org.folio.linked.data.TestUtil.random;
import static org.folio.linked.data.TestUtil.randomBibframe;
import static org.folio.linked.data.TestUtil.randomBibframeCreateRequest;
import static org.folio.linked.data.TestUtil.randomInt;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.function.Function;
import org.folio.linked.data.domain.dto.BibframeResponse;
import org.folio.linked.data.domain.dto.BibframeShort;
import org.folio.linked.data.domain.dto.BibframeShortInfoPage;
import org.folio.linked.data.domain.dto.BibframeUpdateRequest;
import org.folio.linked.data.exception.NotFoundException;
import org.folio.linked.data.mapper.BibframeMapper;
import org.folio.linked.data.model.BibframeIdAndGraphName;
import org.folio.linked.data.model.entity.ResourceType;
import org.folio.linked.data.repo.BibframeRepository;
import org.folio.linked.data.repo.ResourceTypeRepository;
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
class BibframeServiceTest {

  @InjectMocks
  private BibframeServiceImpl bibframeService;

  @Mock
  private BibframeRepository bibframeRepo;

  @Mock
  private ResourceTypeRepository resourceTypeRepo;

  @Mock
  private BibframeMapper bibframeMapper;


  @Test
  void createBibframe_shouldReturnEntityMappedAndPersistedByRepoFromRequest() {
    // given
    var profile = new ResourceType();
    when(resourceTypeRepo.findBySimpleLabel("lc:profile:bf2:Monograph")).thenReturn(Optional.of(profile));
    var request = randomBibframeCreateRequest();
    when(bibframeMapper.toJson(request)).thenReturn(getBibframeJsonNodeSample());
    var bibframe = randomBibframe();
    when(bibframeRepo.save(any())).thenReturn(bibframe);
    var expectedResponse = random(BibframeResponse.class);
    when(bibframeMapper.toBibframe(bibframe.getConfiguration())).thenReturn(expectedResponse);

    // when
    var result = bibframeService.createBibframe("", request);

    // then
    assertThat(result).isEqualTo(expectedResponse);
  }

  @Test
  void getBibframeById_shouldReturnExistedEntity() {
    // given
    var id = randomInt();
    var existedBibframe = randomBibframe();
    existedBibframe.setConfiguration(getBibframeJsonNodeSample());
    when(bibframeRepo.findById(id)).thenReturn(Optional.of(existedBibframe));
    var expectedResponse = random(BibframeResponse.class);
    when(bibframeMapper.toBibframe(existedBibframe.getConfiguration())).thenReturn(expectedResponse);

    // when
    var result = bibframeService.getBibframeById("", id);

    // then
    assertThat(result).isEqualTo(expectedResponse);
  }

  @Test
  void getBibframeById_shouldThrowNotFoundException_ifNoEntityExists() {
    // given
    var notExistedId = randomInt();
    when(bibframeRepo.findById(notExistedId)).thenReturn(Optional.empty());

    // when
    NotFoundException thrown = assertThrows(
      NotFoundException.class,
      () -> bibframeService.getBibframeById("", notExistedId)
    );

    // then
    assertThat(thrown.getMessage()).isEqualTo("Bibframe record with given id [" + notExistedId + "] is not found");
  }

  @Test
  void updateBibframe_shouldThrowNotFoundException_ifNoEntityExists() {
    // given
    var notExistedId = randomInt();
    var request = random(BibframeUpdateRequest.class);
    when(bibframeRepo.findById(notExistedId)).thenReturn(Optional.empty());

    // when
    NotFoundException thrown = assertThrows(
      NotFoundException.class,
      () -> bibframeService.updateBibframe("", notExistedId, request)
    );

    // then
    assertThat(thrown.getMessage()).isEqualTo("Bibframe record with given id [" + notExistedId + "] is not found");
  }

  @Test
  void updateBibframe_shouldReturnUpdatedMappedEntity() {
    // given
    var existedBibframe = randomBibframe();
    when(bibframeRepo.findById(existedBibframe.getId())).thenReturn(Optional.of(existedBibframe));
    var request = random(BibframeUpdateRequest.class);
    when(bibframeMapper.toJson(request)).thenReturn(getBibframeJsonNodeSample());
    when(bibframeRepo.save(existedBibframe)).thenReturn(existedBibframe);
    var expectedResponse = random(BibframeResponse.class);
    when(bibframeMapper.toBibframe(existedBibframe.getConfiguration())).thenReturn(expectedResponse);

    // when
    var result = bibframeService.updateBibframe("", existedBibframe.getId(), request);

    // then
    assertThat(result).isEqualTo(expectedResponse);
  }

  @Test
  void deleteBibframe_shouldDeleteExistedEntity() {
    // given
    var existedBibframe = randomBibframe();
    when(bibframeRepo.existsById(existedBibframe.getId())).thenReturn(true);

    // when
    bibframeService.deleteBibframe("", existedBibframe.getId());

    // then
    verify(bibframeRepo).deleteById(existedBibframe.getId());
  }

  @Test
  void deleteBibframe_shouldThrowNotFoundException_ifNoEntityExists() {
    // given
    var notExistedId = randomInt();
    when(bibframeRepo.existsById(notExistedId)).thenReturn(false);

    // when
    NotFoundException thrown = assertThrows(
      NotFoundException.class,
      () -> bibframeService.deleteBibframe("", notExistedId)
    );

    // then
    assertThat(thrown.getMessage()).isEqualTo("Bibframe record with given id [" + notExistedId + "] is not found");
  }

  @Test
  void getBibframeShortInfoPageWithParams_shouldReturnExistedEntitiesShortInfoMapped(
    @Mock Page<BibframeIdAndGraphName> pageOfShortEntities, @Mock Page<BibframeShort> pageOfDto) {
    // given
    var pageNumber = 0;
    var pageSize = 10;
    var sort = Sort.by(Sort.Direction.ASC, "graphName");
    doReturn(pageOfShortEntities).when(bibframeRepo).findAllBy(PageRequest.of(pageNumber, pageSize, sort));
    doReturn(pageOfDto).when(pageOfShortEntities)
      .map(ArgumentMatchers.<Function<BibframeIdAndGraphName, BibframeShort>>any());
    var expectedResult = random(BibframeShortInfoPage.class);
    doReturn(expectedResult).when(bibframeMapper).map(pageOfDto);

    // when
    var result = bibframeService.getBibframeShortInfoPage("", pageNumber, pageSize);

    // then
    assertThat(result).isEqualTo(expectedResult);
  }

  @Test
  void getBibframeShortInfoPageWithNoParams_shouldReturnExistedEntitiesShortInfoMapped(
    @Mock Page<BibframeIdAndGraphName> pageOfShortEntities, @Mock Page<BibframeShort> pageOfDto) {
    // given
    var sort = Sort.by(Sort.Direction.ASC, "graphName");
    doReturn(pageOfShortEntities).when(bibframeRepo).findAllBy(PageRequest.of(0, 100, sort));
    doReturn(pageOfDto).when(pageOfShortEntities)
      .map(ArgumentMatchers.<Function<BibframeIdAndGraphName, BibframeShort>>any());
    var expectedResult = random(BibframeShortInfoPage.class);
    doReturn(expectedResult).when(bibframeMapper).map(pageOfDto);

    // when
    var result = bibframeService.getBibframeShortInfoPage("", null, null);

    // then
    assertThat(result).isEqualTo(expectedResult);
  }


}

