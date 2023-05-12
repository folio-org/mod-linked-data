package org.folio.linked.data.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

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
    request.setToBeFilled(true);
    var response = new BibframeResponse();
    response.setToBeFilled(request.getToBeFilled());
    response.setId(UUID.randomUUID());
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
    var id = UUID.randomUUID();
    var response = new BibframeResponse();
    response.setToBeFilled(true);
    response.setId(UUID.randomUUID());
    when(bibframeService.getBibframeById(id)).thenReturn(response);

    // when
    var result = bibframeController.getBibframeById(id);

    // then
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(result.getBody()).isEqualTo(response);
  }
}
