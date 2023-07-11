package org.folio.linked.data.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.linked.data.test.TestUtil.random;
import static org.folio.linked.data.test.TestUtil.randomLong;
import static org.folio.linked.data.test.TestUtil.randomString;
import static org.mockito.Mockito.when;

import org.folio.linked.data.domain.dto.BibframeResponse;
import org.folio.linked.data.domain.dto.BibframeShortInfoPage;
import org.folio.linked.data.service.ResourceService;
import org.folio.spring.test.type.UnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

@UnitTest
@ExtendWith(MockitoExtension.class)
class ResourceControllerTest {

  @InjectMocks
  private ResourceController resourceController;

  @Mock
  private ResourceService resourceService;

  @Test
  void getResourceid_shouldReturnOkResponse_ifResourceServiceReturnsEntity() {
    // given
    var id = randomLong();
    var tenant = randomString();
    var response = random(BibframeResponse.class);
    when(resourceService.getBibframeById(id)).thenReturn(response);

    // when
    var result = resourceController.getBibframeById(id, tenant);

    // then
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(result.getBody()).isEqualTo(response);
  }

  @Test
  void getResourcesShortInfoPage_shouldReturnResultOfService() {
    // given
    var tenant = randomString();
    var page = random(Integer.class);
    var size = random(Integer.class);
    var expectedResponse = random(BibframeShortInfoPage.class);
    when(resourceService.getBibframeShortInfoPage(page, size)).thenReturn(expectedResponse);

    // when
    var result = resourceController.getBibframeShortInfoPage(tenant, page, size);

    // then
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(result.getBody()).isEqualTo(expectedResponse);
  }
}
