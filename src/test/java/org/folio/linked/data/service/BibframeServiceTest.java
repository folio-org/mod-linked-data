package org.folio.linked.data.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.linked.data.TestUtil.GRAPH_NAME;
import static org.folio.linked.data.TestUtil.OBJECT_MAPPER;
import static org.folio.linked.data.TestUtil.getBibframeSample;
import static org.folio.linked.data.util.TextUtil.slugify;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import org.folio.linked.data.domain.dto.BibframeRequest;
import org.folio.linked.data.domain.dto.BibframeResponse;
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
  void createBibframe_shouldReturnEntityMappedAndPersistedByRepoFromRequest() throws JsonProcessingException {
    // given
    var request = new BibframeRequest(GRAPH_NAME, getBibframeSample());
    var bibframe = Bibframe.of(GRAPH_NAME, OBJECT_MAPPER.readTree(getBibframeSample()));
    when(bibframeMapper.map(request)).thenReturn(bibframe);
    var persisted = Bibframe.of(GRAPH_NAME, OBJECT_MAPPER.readTree(getBibframeSample()));
    when(bibframeRepo.save(bibframe)).thenReturn(persisted);
    var expectedResponse = new BibframeResponse();
    expectedResponse.setId(1);
    expectedResponse.setGraphName(GRAPH_NAME);
    expectedResponse.setSlug(String.valueOf(Objects.hash(GRAPH_NAME)));
    expectedResponse.setConfiguration(getBibframeSample());
    expectedResponse.setGraphHash(Objects.hash(getBibframeSample()));
    when(bibframeMapper.map(persisted)).thenReturn(expectedResponse);

    // when
    var result = bibframeService.createBibframe("", request);

    // then
    assertThat(result).isEqualTo(expectedResponse);
  }

  @Test
  void createBibframe_shouldThrowAlreadyExistsException_ifEntityExists() {
    // given
    var request = new BibframeRequest(GRAPH_NAME, getBibframeSample());
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
  void getBibframeSlug_shouldReturnExistedEntity() throws JsonProcessingException {
    // given
    var existedBibframe = Bibframe.of(GRAPH_NAME, OBJECT_MAPPER.readTree(getBibframeSample()));
    when(bibframeRepo.findBySlug(existedBibframe.getSlug())).thenReturn(Optional.of(existedBibframe));
    var expectedResponse = new BibframeResponse();
    expectedResponse.setId(1);
    expectedResponse.setGraphName(GRAPH_NAME);
    expectedResponse.setSlug(String.valueOf(Objects.hash(GRAPH_NAME)));
    expectedResponse.setConfiguration(getBibframeSample());
    expectedResponse.setGraphHash(Objects.hash(getBibframeSample()));
    when(bibframeMapper.map(existedBibframe)).thenReturn(expectedResponse);

    // when
    var result = bibframeService.getBibframeBySlug("", existedBibframe.getSlug());

    // then
    assertThat(result).isEqualTo(expectedResponse);
  }

  @Test
  void getBibframeSlug_shouldThrowNotFoundException_ifNoEntityExists() {
    // given
    var notExistedSlug = UUID.randomUUID().toString();
    when(bibframeRepo.findBySlug(notExistedSlug)).thenReturn(Optional.empty());

    // when
    NotFoundException thrown = assertThrows(
      NotFoundException.class,
      () -> bibframeService.getBibframeBySlug("", notExistedSlug)
    );

    // then
    assertThat(thrown.getMessage()).isEqualTo("Bibframe record with given slug [" + notExistedSlug + "] is not found");
  }
}

