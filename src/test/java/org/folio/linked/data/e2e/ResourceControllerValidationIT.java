package org.folio.linked.data.e2e;

import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.linked.data.test.TestUtil.defaultHeaders;
import static org.folio.linked.data.test.TestUtil.loadResourceAsString;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.folio.linked.data.domain.dto.Error;
import org.folio.linked.data.domain.dto.ErrorResponse;
import org.folio.linked.data.domain.dto.Parameter;
import org.folio.linked.data.e2e.base.IntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.messaging.handler.annotation.support.MethodArgumentNotValidException;
import org.springframework.test.jdbc.JdbcTestUtils;
import org.springframework.test.web.servlet.MockMvc;

@IntegrationTest
class ResourceControllerValidationIT {
  private static final String RESOURCE_URL = "/resource";
  @Autowired
  private MockMvc mockMvc;
  @Autowired
  private ObjectMapper objectMapper;
  @Autowired
  private Environment env;
  @Autowired
  private JdbcTemplate jdbcTemplate;

  @BeforeEach
  public void beforeEach() {
    JdbcTestUtils.deleteFromTables(jdbcTemplate, "resource_edges", "resource_type_map", "resources");
  }

  @Test
  void createEmptyInstance_shouldReturnBadRequest() throws Exception {
    // given
    var requestBuilder = post(RESOURCE_URL)
      .contentType(APPLICATION_JSON)
      .headers(defaultHeaders(env))
      .content(loadResourceAsString("samples/empty_instance.json"));
    var expectedError = getError("instance");

    // when
    var resultActions = mockMvc.perform(requestBuilder);

    // then
    var response = resultActions
      .andExpect(status().isBadRequest())
      .andExpect(content().contentType(APPLICATION_JSON))
      .andReturn().getResponse().getContentAsString();

    var errorResponse = objectMapper.readValue(response, ErrorResponse.class);
    assertThat(errorResponse.getErrors()).containsOnly(expectedError);
  }

  @Test
  void createEmptyWork_shouldReturnBadRequest() throws Exception {
    // given
    var requestBuilder = post(RESOURCE_URL)
      .contentType(APPLICATION_JSON)
      .headers(defaultHeaders(env))
      .content(loadResourceAsString("samples/empty_work.json"));
    var expectedError = getError("work");

    // when
    var resultActions = mockMvc.perform(requestBuilder);

    // then
    var response = resultActions
      .andExpect(status().isBadRequest())
      .andExpect(content().contentType(APPLICATION_JSON))
      .andReturn().getResponse().getContentAsString();

    var errorResponse = objectMapper.readValue(response, ErrorResponse.class);
    assertThat(errorResponse.getErrors()).containsOnly(expectedError);
  }

  @Test
  void updateWithEmptyInstance_shouldReturnBadRequest() throws Exception {
    // given
    var requestBuilder = put(RESOURCE_URL + "/123")
      .contentType(APPLICATION_JSON)
      .headers(defaultHeaders(env))
      .content(loadResourceAsString("samples/empty_instance.json"));
    var expectedError = getError("instance");

    // when
    var resultActions = mockMvc.perform(requestBuilder);

    // then
    var response = resultActions
      .andExpect(status().isBadRequest())
      .andExpect(content().contentType(APPLICATION_JSON))
      .andReturn().getResponse().getContentAsString();

    var errorResponse = objectMapper.readValue(response, ErrorResponse.class);
    assertThat(errorResponse.getErrors()).containsOnly(expectedError);
  }

  @Test
  void updateWithEmptyWork_shouldReturnBadRequest() throws Exception {
    // given
    var requestBuilder = put(RESOURCE_URL + "/123")
      .contentType(APPLICATION_JSON)
      .headers(defaultHeaders(env))
      .content(loadResourceAsString("samples/empty_work.json"));
    var expectedError = getError("work");

    // when
    var resultActions = mockMvc.perform(requestBuilder);

    // then
    var response = resultActions
      .andExpect(status().isBadRequest())
      .andExpect(content().contentType(APPLICATION_JSON))
      .andReturn().getResponse().getContentAsString();

    var errorResponse = objectMapper.readValue(response, ErrorResponse.class);
    assertThat(errorResponse.getErrors()).containsOnly(expectedError);
  }

  @Test
  void createInstanceWithTitleWithoutMainTitle_shouldReturnBadRequest() throws Exception {
    // given
    var requestBuilder = post(RESOURCE_URL)
      .contentType(APPLICATION_JSON)
      .headers(defaultHeaders(env))
      .content(loadResourceAsString("samples/instance_with_no_main_primary_title.json"));
    var expectedError = getError("instance", "[class PrimaryTitleField {\n"
      + "    primaryTitle: class PrimaryTitle {\n"
      + "        id: null\n"
      + "        partName: [Primary: partName]\n"
      + "        partNumber: [Primary: partNumber]\n"
      + "        mainTitle: []\n"
      + "        subTitle: [Primary: subTitle]\n"
      + "        nonSortNum: [Primary: nonSortNum]\n"
      + "    }\n"
      + "}]");

    // when
    var resultActions = mockMvc.perform(requestBuilder);

    // then
    var response = resultActions
      .andExpect(status().isBadRequest())
      .andExpect(content().contentType(APPLICATION_JSON))
      .andReturn().getResponse().getContentAsString();

    var errorResponse = objectMapper.readValue(response, ErrorResponse.class);
    assertThat(errorResponse.getErrors()).containsOnly(expectedError);
  }

  private Error getError(String resourceType) {
    return getError(resourceType, "[]");
  }

  private Error getError(String resourceType, String value) {
    return new Error()
      .code("validation_error")
      .message("Primary main title should be presented")
      .type(MethodArgumentNotValidException.class.getSimpleName())
      .parameters(List.of(
        new Parameter()
          .key("resource." + resourceType + ".title")
          .value(value)
      ));
  }
}
