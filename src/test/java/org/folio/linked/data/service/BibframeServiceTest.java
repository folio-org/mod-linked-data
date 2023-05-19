package org.folio.linked.data.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.linked.data.TestUtil.random;
import static org.folio.linked.data.TestUtil.randomBibframe;
import static org.folio.linked.data.TestUtil.randomString;
import static org.folio.linked.data.util.TextUtil.slugify;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.folio.linked.data.domain.dto.BibframeCreateRequest;
import org.folio.linked.data.domain.dto.BibframeResponse;
import org.folio.linked.data.domain.dto.BibframeUpdateRequest;
import org.folio.linked.data.exception.AlreadyExistsException;
import org.folio.linked.data.exception.NotFoundException;
import org.folio.linked.data.mapper.BibframeMapper;
import org.folio.linked.data.model.entity.Bibframe;
import org.folio.linked.data.repo.BibframeRepository;
import org.folio.spring.test.type.UnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@UnitTest
@ExtendWith(MockitoExtension.class)
class BibframeServiceTest {

  @InjectMocks
  private BibframeServiceImpl bibframeService;

  @Mock
  private BibframeRepository bibframeRepo;

  @Mock
  private BibframeMapper bibframeMapper;

  @Test
  void createBibframe_shouldReturnEntityMappedAndPersistedByRepoFromRequest() {
    // given
    var request = random(BibframeCreateRequest.class);
    var bibframe = randomBibframe();
    when(bibframeMapper.map(request)).thenReturn(bibframe);
    var persisted = randomBibframe();
    when(bibframeRepo.save(bibframe)).thenReturn(persisted);
    var expectedResponse = random(BibframeResponse.class);
    when(bibframeMapper.map(persisted)).thenReturn(expectedResponse);

    // when
    var result = bibframeService.createBibframe("", request);

    // then
    assertThat(result).isEqualTo(expectedResponse);
  }

  @Test
  void createBibframe_shouldThrowAlreadyExistsException_ifEntityExists() {
    // given
    var request = random(BibframeCreateRequest.class);
    var slug = slugify(request.getGraphName());
    when(bibframeRepo.existsBySlug(slug)).thenReturn(true);
    var bibframe = new Bibframe();
    bibframe.setSlug(slug);
    when(bibframeMapper.map(request)).thenReturn(bibframe);

    // when
    AlreadyExistsException thrown = assertThrows(
      AlreadyExistsException.class,
      () -> bibframeService.createBibframe("", request)
    );

    // then
    assertThat(thrown.getMessage()).isEqualTo("Bibframe record with given slug [" + slug + "] exists already");
  }

  @Test
  void getBibframeSlug_shouldReturnExistedEntity() {
    // given
    var existedBibframe = randomBibframe();
    when(bibframeRepo.findBySlug(existedBibframe.getSlug())).thenReturn(Optional.of(existedBibframe));
    var expectedResponse = random(BibframeResponse.class);
    when(bibframeMapper.map(existedBibframe)).thenReturn(expectedResponse);

    // when
    var result = bibframeService.getBibframeBySlug("", existedBibframe.getSlug());

    // then
    assertThat(result).isEqualTo(expectedResponse);
  }

  @Test
  void getBibframeSlug_shouldThrowNotFoundException_ifNoEntityExists() {
    // given
    var notExistedSlug = randomString();
    when(bibframeRepo.findBySlug(notExistedSlug)).thenReturn(Optional.empty());

    // when
    NotFoundException thrown = assertThrows(
      NotFoundException.class,
      () -> bibframeService.getBibframeBySlug("", notExistedSlug)
    );

    // then
    assertThat(thrown.getMessage()).isEqualTo("Bibframe record with given slug [" + notExistedSlug + "] is not found");
  }

  @Test
  void updateBibframe_shouldThrowNotFoundException_ifNoEntityExists() {
    // given
    var notExistedSlug = randomString();
    var request = random(BibframeUpdateRequest.class);
    when(bibframeRepo.findBySlug(notExistedSlug)).thenReturn(Optional.empty());

    // when
    NotFoundException thrown = assertThrows(
      NotFoundException.class,
      () -> bibframeService.updateBibframe("", notExistedSlug, request)
    );

    // then
    assertThat(thrown.getMessage()).isEqualTo("Bibframe record with given slug [" + notExistedSlug + "] is not found");
  }

  @Test
  void updateBibframe_shouldReturnUpdatedMappedEntity() {
    // given
    var existedBibframe = randomBibframe();
    when(bibframeRepo.findBySlug(existedBibframe.getSlug())).thenReturn(Optional.of(existedBibframe));
    var request = random(BibframeUpdateRequest.class);
    var updatedBibframe = randomBibframe();
    when(bibframeMapper.update(existedBibframe, request)).thenReturn(updatedBibframe);
    when(bibframeRepo.save(updatedBibframe)).thenReturn(updatedBibframe);
    var expectedResponse = random(BibframeResponse.class);
    when(bibframeMapper.map(updatedBibframe)).thenReturn(expectedResponse);

    // when
    var result = bibframeService.updateBibframe("", existedBibframe.getSlug(), request);

    // then
    assertThat(result).isEqualTo(expectedResponse);
  }

  @Test
  void deleteBibframe_shouldDeleteExistedEntity() {
    // given
    var existedBibframe = randomBibframe();
    when(bibframeRepo.deleteBySlug(existedBibframe.getSlug())).thenReturn(1);

    // when
    bibframeService.deleteBibframe("", existedBibframe.getSlug());

    // then
    verify(bibframeRepo).deleteBySlug(existedBibframe.getSlug());
  }

  @Test
  void deleteBibframe_shouldThrowNotFoundException_ifNoEntityExists() {
    // given
    var notExistedSlug = randomString();
    when(bibframeRepo.deleteBySlug(notExistedSlug)).thenReturn(0);

    // when
    NotFoundException thrown = assertThrows(
      NotFoundException.class,
      () -> bibframeService.deleteBibframe("", notExistedSlug)
    );

    // then
    assertThat(thrown.getMessage()).isEqualTo("Bibframe record with given slug [" + notExistedSlug + "] is not found");
  }
}

