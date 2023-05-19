package org.folio.linked.data.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.linked.data.TestUtil.random;
import static org.folio.linked.data.TestUtil.randomString;
import static org.mockito.Mockito.when;

import org.folio.linked.data.domain.dto.BibframeCreateRequest;
import org.folio.linked.data.domain.dto.BibframeResponse;
import org.folio.linked.data.domain.dto.BibframeUpdateRequest;
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
    var request = random(BibframeCreateRequest.class);
    var response = random(BibframeResponse.class);
    var tenant = randomString();
    when(bibframeService.createBibframe(tenant, request)).thenReturn(response);

    // when
    var result = bibframeController.createBibframe(tenant, request);

    // then
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(result.getBody()).isEqualTo(response);
  }

  @Test
  void getBibframeSlug_shouldReturnOkResponse_ifBibframeServiceReturnsEntity() {
    // given
    var slug = randomString();
    var tenant = randomString();
    var response = random(BibframeResponse.class);
    when(bibframeService.getBibframeBySlug(tenant, slug)).thenReturn(response);

    // when
    var result = bibframeController.getBibframeBySlug(tenant, slug);

    // then
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(result.getBody()).isEqualTo(response);
  }

  @Test
  void updateBibframe_shouldReturnOkResponse_ifBibframeServiceReturnsEntity() {
    // given
    var slug = randomString();
    var tenant = randomString();
    var response = random(BibframeResponse.class);
    var request = random(BibframeUpdateRequest.class);
    when(bibframeService.updateBibframe(tenant, slug, request)).thenReturn(response);

    // when
    var result = bibframeController.updateBibframe(tenant, slug, request);

    // then
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(result.getBody()).isEqualTo(response);
  }

  @Test
  void deleteBibframe_shouldReturnOkResponse_ifNoException() {
    // given
    var slug = randomString();
    var tenant = randomString();

    // when
    var result = bibframeController.deleteBibframe(tenant, slug);

    // then
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    assertThat(result.getBody()).isNull();
  }
}
