package org.folio.linked.data.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;
import org.folio.linked.data.domain.dto.BibframeCreateRequest;
import org.folio.linked.data.domain.dto.BibframeResponse;
import org.folio.linked.data.exception.NotFoundException;
import org.folio.linked.data.mapper.BibframeMapper;
import org.folio.linked.data.model.entity.Bibframe;
import org.folio.linked.data.repo.BibframeRepo;
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
  private BibframeRepo bibframeRepo;

  @Mock
  private BibframeMapper bibframeMapper;

  @Test
  void createBibframe_shouldReturnEntityMappedAndPersistedByRepoFromRequest() {
    // given
    var request = new BibframeCreateRequest();
    request.setToBeFilled(true);
    var bibframe = Bibframe.of(null, request.getToBeFilled());
    when(bibframeMapper.map(request)).thenReturn(bibframe);
    var persisted = Bibframe.of(UUID.randomUUID(), bibframe.isToBeFilled());
    when(bibframeRepo.persist(bibframe)).thenReturn(persisted);
    var expectedResponse = new BibframeResponse();
    expectedResponse.setId(persisted.getId());
    expectedResponse.setToBeFilled(persisted.isToBeFilled());
    when(bibframeMapper.map(persisted)).thenReturn(expectedResponse);

    // when
    var result = bibframeService.createBibframe("", request);

    // then
    assertThat(result).isEqualTo(expectedResponse);
  }

  @Test
  void getBibframeById_shouldReturnExistedEntity() {
    // given
    var existedBibframe = Bibframe.of(UUID.randomUUID(), true);
    when(bibframeRepo.read(existedBibframe.getId())).thenReturn(Optional.of(existedBibframe));
    var expectedResponse = new BibframeResponse();
    expectedResponse.setId(existedBibframe.getId());
    expectedResponse.setToBeFilled(existedBibframe.isToBeFilled());
    when(bibframeMapper.map(existedBibframe)).thenReturn(expectedResponse);

    // when
    var result = bibframeService.getBibframeById(existedBibframe.getId());

    // then
    assertThat(result).isEqualTo(expectedResponse);
  }

  @Test
  void getBibframeById_shouldThrowNotFoundException_ifNoEntityExists() {
    // given
    var notExistedId = UUID.randomUUID();
    when(bibframeRepo.read(notExistedId)).thenReturn(Optional.empty());

    // when
    NotFoundException thrown = assertThrows(
      NotFoundException.class,
      () -> bibframeService.getBibframeById(notExistedId)
    );

    // then
    assertThat(thrown.getMessage()).isEqualTo("Bibframe record with given id [" + notExistedId + "] is not found");
  }
}
