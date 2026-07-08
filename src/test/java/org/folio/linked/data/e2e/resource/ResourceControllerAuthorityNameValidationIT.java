package org.folio.linked.data.e2e.resource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.linked.data.e2e.resource.ResourceControllerITBase.RESOURCE_URL;
import static org.folio.linked.data.test.TestUtil.TEST_JSON_MAPPER;
import static org.folio.linked.data.test.TestUtil.defaultHeaders;
import static org.folio.linked.data.test.TestUtil.loadResourceAsString;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.folio.linked.data.domain.dto.ErrorResponse;
import org.folio.linked.data.e2e.base.ITBase;
import org.folio.linked.data.e2e.base.IntegrationTest;
import org.folio.spring.tools.kafka.KafkaAdminService;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

@IntegrationTest
class ResourceControllerAuthorityNameValidationIT extends ITBase {

  @MockitoSpyBean
  private KafkaAdminService kafkaAdminService;

  @Test
  void createAuthorityWithNoName_shouldReturnBadRequest() throws Exception {
    // given
    var requestBuilder = post(RESOURCE_URL)
      .contentType(APPLICATION_JSON)
      .headers(defaultHeaders(env))
      .content(loadResourceAsString("samples/empty_authority.json"));

    // when
    var resultActions = mockMvc.perform(requestBuilder);

    // then
    var response = resultActions
      .andExpect(status().isBadRequest())
      .andExpect(content().contentType(APPLICATION_JSON))
      .andReturn().getResponse().getContentAsString();

    var errorResponse = TEST_JSON_MAPPER.readValue(response, ErrorResponse.class);
    assertThat(errorResponse.getErrors())
      .extracting("code")
      .contains("required_authority_name");
  }

  @Test
  void updateAuthorityWithNoName_shouldReturnBadRequest() throws Exception {
    // given
    var requestBuilder = put(RESOURCE_URL + "/123")
      .contentType(APPLICATION_JSON)
      .headers(defaultHeaders(env))
      .content(loadResourceAsString("samples/empty_authority.json"));

    // when
    var resultActions = mockMvc.perform(requestBuilder);

    // then
    var response = resultActions
      .andExpect(status().isBadRequest())
      .andExpect(content().contentType(APPLICATION_JSON))
      .andReturn().getResponse().getContentAsString();

    var errorResponse = TEST_JSON_MAPPER.readValue(response, ErrorResponse.class);
    assertThat(errorResponse.getErrors())
      .extracting("code")
      .contains("required_authority_name");
  }
}
