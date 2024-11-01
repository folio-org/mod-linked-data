package org.folio.linked.data.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import org.folio.linked.data.domain.dto.ResourceMarcViewDto;
import org.folio.linked.data.service.resource.ResourceMarcBibService;
import org.folio.linked.data.service.resource.ResourceService;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@UnitTest
@ExtendWith(MockitoExtension.class)
class ResourceControllerTest {

  private static final String OKAPI_TENANT = "okapiTenant";

  @InjectMocks
  ResourceController resourceController;
  @Mock
  ResourceService resourceService;
  @Mock
  ResourceMarcBibService resourceMarcService;

  @Test
  void getResourceMarcViewById_shouldReturnOkResponse() {
    // given
    var id = 1L;
    var expectedDto = new ResourceMarcViewDto();
    expectedDto.setId(String.valueOf(id));

    when(resourceMarcService.getResourceMarcView(id))
      .thenReturn(expectedDto);

    //when
    var response = resourceController.getResourceMarcViewById(id, OKAPI_TENANT);

    //then
    assertThat(response)
      .isNotNull()
      .hasFieldOrPropertyWithValue("statusCode.value", 200)
      .hasFieldOrPropertyWithValue("body", expectedDto);
  }
}
