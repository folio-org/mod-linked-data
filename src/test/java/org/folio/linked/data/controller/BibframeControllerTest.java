package org.folio.linked.data.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.linked.data.TestUtil.CONFIGURATION;
import static org.folio.linked.data.TestUtil.GRAPH_NAME;
import static org.mockito.Mockito.when;

import java.util.Objects;
import java.util.UUID;
import org.folio.linked.data.domain.dto.BibframeCreateRequest;
import org.folio.linked.data.domain.dto.BibframeResponse;
import org.folio.linked.data.service.BibframeService;
import org.folio.spring.test.type.UnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

@UnitTest
@ExtendWith(MockitoExtension.class)
class BibframeControllerTest {

  @InjectMocks
  private BibframeController bibframeController;

  @Mock
  private BibframeService bibframeService;

  @Test
  void createBibframe_shouldReturnOkResponse_ifBibframeServiceReturnsEntity() {
    // given
    var request = new BibframeCreateRequest();
    request.setGraphName(GRAPH_NAME);
    request.setConfiguration(CONFIGURATION);

    var response = new BibframeResponse();
    response.setId(1);
    response.setGraphName(GRAPH_NAME);
    response.setSlug(String.valueOf(Objects.hash(GRAPH_NAME)));
    response.setConfiguration(CONFIGURATION);
    response.setGraphHash(Objects.hash(CONFIGURATION));

    var tenantId = UUID.randomUUID().toString();
    when(bibframeService.createBibframe(tenantId, request)).thenReturn(response);

    // when
    var result = bibframeController.createBibframe(tenantId, request);

    // then
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(result.getBody()).isEqualTo(response);
  }

  @Test
  void getBibframeById_shouldReturnOkResponse_ifBibframeServiceReturnsEntity() {
    // given
    var response = new BibframeResponse();
    response.setId(1);
    response.setGraphName(GRAPH_NAME);
    response.setSlug(String.valueOf(Objects.hash(GRAPH_NAME)));
    response.setConfiguration(CONFIGURATION);
    response.setGraphHash(Objects.hash(CONFIGURATION));

    var slug = UUID.randomUUID().toString();
    when(bibframeService.getBibframeBySlug(slug)).thenReturn(response);

    // when
    var result = bibframeController.getBibframeById(slug);

    // then
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(result.getBody()).isEqualTo(response);
  }
}
