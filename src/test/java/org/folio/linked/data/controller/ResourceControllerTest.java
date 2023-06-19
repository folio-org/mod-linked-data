package org.folio.linked.data.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.linked.data.TestUtil.random;
import static org.folio.linked.data.TestUtil.randomLong;
import static org.folio.linked.data.TestUtil.randomString;
import static org.mockito.Mockito.when;

import org.folio.linked.data.domain.dto.ResourceResponse;
import org.folio.linked.data.domain.dto.ResourceShortInfoPage;
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
    var response = random(ResourceResponse.class);
    when(resourceService.getResourceById(id)).thenReturn(response);

    // when
    var result = resourceController.getResourceById(id, tenant);

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
    var expectedResponse = random(ResourceShortInfoPage.class);
    when(resourceService.getResourceShortInfoPage(page, size)).thenReturn(expectedResponse);

    // when
    var result = resourceController.getResourcesShortInfoPage(tenant, page, size);

    // then
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(result.getBody()).isEqualTo(expectedResponse);
  }
}
