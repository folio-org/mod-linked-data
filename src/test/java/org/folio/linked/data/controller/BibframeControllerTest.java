package org.folio.linked.data.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.linked.data.TestUtil.random;
import static org.folio.linked.data.TestUtil.randomBibframeCreateRequest;
import static org.folio.linked.data.TestUtil.randomInt;
import static org.folio.linked.data.TestUtil.randomString;
import static org.mockito.Mockito.when;

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
    var request = randomBibframeCreateRequest();
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
  void getBibframeid_shouldReturnOkResponse_ifBibframeServiceReturnsEntity() {
    // given
    var id = randomInt();
    var tenant = randomString();
    var response = random(BibframeResponse.class);
    when(bibframeService.getBibframeById(tenant, id)).thenReturn(response);

    // when
    var result = bibframeController.getBibframeById(tenant, id);

    // then
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(result.getBody()).isEqualTo(response);
  }

  @Test
  void updateBibframe_shouldReturnOkResponse_ifBibframeServiceReturnsEntity() {
    // given
    var id = randomInt();
    var tenant = randomString();
    var response = random(BibframeResponse.class);
    var request = random(BibframeUpdateRequest.class);
    when(bibframeService.updateBibframe(tenant, id, request)).thenReturn(response);

    // when
    var result = bibframeController.updateBibframe(tenant, id, request);

    // then
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(result.getBody()).isEqualTo(response);
  }

  @Test
  void deleteBibframe_shouldReturnOkResponse_ifNoException() {
    // given
    var id = randomInt();
    var tenant = randomString();

    // when
    var result = bibframeController.deleteBibframe(tenant, id);

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
    when(bibframeService.getBibframeShortInfoPage(tenant, page, size)).thenReturn(expectedResponse);

    // when
    var result = bibframeController.getBibframesShortInfoPage(tenant, page, size);

    // then
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(result.getBody()).isEqualTo(expectedResponse);
  }
}
