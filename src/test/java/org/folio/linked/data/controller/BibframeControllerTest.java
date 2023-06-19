package org.folio.linked.data.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.linked.data.TestUtil.random;
import static org.folio.linked.data.TestUtil.randomString;
import static org.mockito.Mockito.when;

import org.folio.linked.data.domain.dto.BibframeCreateRequest;
import org.folio.linked.data.domain.dto.BibframeResponse;
import org.folio.linked.data.domain.dto.BibframeShortInfoPage;
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
    when(bibframeService.createBibframe(request, tenant)).thenReturn(response);

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
    when(bibframeService.getBibframeBySlug(slug, tenant)).thenReturn(response);

    // when
    var result = bibframeController.getBibframeBySlug(slug, tenant);

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
    when(bibframeService.updateBibframe(slug, request, tenant)).thenReturn(response);

    // when
    var result = bibframeController.updateBibframe(slug, tenant, request);

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
    var result = bibframeController.deleteBibframe(slug, tenant);

    // then
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    assertThat(result.getBody()).isNull();
  }

  @Test
  void getBibframesShortInfoPage_shouldReturnResultOfService() {
    // given
    var tenant = randomString();
    var page = random(Integer.class);
    var size = random(Integer.class);
    var expectedResponse = random(BibframeShortInfoPage.class);
    when(bibframeService.getBibframeShortInfoPage(page, size)).thenReturn(expectedResponse);

    // when
    var result = bibframeController.getBibframesShortInfoPage(tenant, page, size);

    // then
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(result.getBody()).isEqualTo(expectedResponse);
  }
}
