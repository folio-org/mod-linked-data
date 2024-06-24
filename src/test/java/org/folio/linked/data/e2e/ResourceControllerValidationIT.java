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
import org.folio.linked.data.domain.dto.ErrorResponse;
import org.folio.linked.data.e2e.base.IntegrationTest;
import org.folio.linked.data.model.ErrorCode;
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

    // when
    var resultActions = mockMvc.perform(requestBuilder);

    // then
    var response = resultActions
      .andExpect(status().isBadRequest())
      .andExpect(content().contentType(APPLICATION_JSON))
      .andReturn().getResponse().getContentAsString();

    var errorResponse = objectMapper.readValue(response, ErrorResponse.class);
    assertThat(errorResponse.getErrors()).hasSize(1);
    var error = errorResponse.getErrors().get(0);
    assertThat(error.getCode()).isEqualTo(ErrorCode.VALIDATION_ERROR.getValue());
    assertThat(error.getMessage()).isEqualTo("Primary main title should be presented");
    assertThat(error.getType()).isEqualTo(MethodArgumentNotValidException.class.getSimpleName());
    assertThat(error.getParameters()).hasSize(1);
    assertThat(error.getParameters().get(0).getKey()).isEqualTo("resource.instance.title");
    assertThat(error.getParameters().get(0).getValue()).isEqualTo("[]");
  }

  @Test
  void createEmptyWork_shouldReturnBadRequest() throws Exception {
    // given
    var requestBuilder = post(RESOURCE_URL)
      .contentType(APPLICATION_JSON)
      .headers(defaultHeaders(env))
      .content(loadResourceAsString("samples/empty_work.json"));

    // when
    var resultActions = mockMvc.perform(requestBuilder);

    // then
    var response = resultActions
      .andExpect(status().isBadRequest())
      .andExpect(content().contentType(APPLICATION_JSON))
      .andReturn().getResponse().getContentAsString();

    var errorResponse = objectMapper.readValue(response, ErrorResponse.class);
    assertThat(errorResponse.getErrors()).hasSize(1);
    var error = errorResponse.getErrors().get(0);
    assertThat(error.getCode()).isEqualTo(ErrorCode.VALIDATION_ERROR.getValue());
    assertThat(error.getMessage()).isEqualTo("Primary main title should be presented");
    assertThat(error.getType()).isEqualTo(MethodArgumentNotValidException.class.getSimpleName());
    assertThat(error.getParameters()).hasSize(1);
    assertThat(error.getParameters().get(0).getKey()).isEqualTo("resource.work.title");
    assertThat(error.getParameters().get(0).getValue()).isEqualTo("[]");
  }

  @Test
  void updateWithEmptyInstance_shouldReturnBadRequest() throws Exception {
    // given
    var requestBuilder = put(RESOURCE_URL  + "/123")
      .contentType(APPLICATION_JSON)
      .headers(defaultHeaders(env))
      .content(loadResourceAsString("samples/empty_instance.json"));

    // when
    var resultActions = mockMvc.perform(requestBuilder);

    // then
    var response = resultActions
      .andExpect(status().isBadRequest())
      .andExpect(content().contentType(APPLICATION_JSON))
      .andReturn().getResponse().getContentAsString();

    var errorResponse = objectMapper.readValue(response, ErrorResponse.class);
    assertThat(errorResponse.getErrors()).hasSize(1);
    var error = errorResponse.getErrors().get(0);
    assertThat(error.getCode()).isEqualTo(ErrorCode.VALIDATION_ERROR.getValue());
    assertThat(error.getMessage()).isEqualTo("Primary main title should be presented");
    assertThat(error.getType()).isEqualTo(MethodArgumentNotValidException.class.getSimpleName());
    assertThat(error.getParameters()).hasSize(1);
    assertThat(error.getParameters().get(0).getKey()).isEqualTo("resource.instance.title");
    assertThat(error.getParameters().get(0).getValue()).isEqualTo("[]");
  }

  @Test
  void updateWithEmptyWork_shouldReturnBadRequest() throws Exception {
    // given
    var requestBuilder = put(RESOURCE_URL  + "/123")
      .contentType(APPLICATION_JSON)
      .headers(defaultHeaders(env))
      .content(loadResourceAsString("samples/empty_work.json"));

    // when
    var resultActions = mockMvc.perform(requestBuilder);

    // then
    var response = resultActions
      .andExpect(status().isBadRequest())
      .andExpect(content().contentType(APPLICATION_JSON))
      .andReturn().getResponse().getContentAsString();

    var errorResponse = objectMapper.readValue(response, ErrorResponse.class);
    assertThat(errorResponse.getErrors()).hasSize(1);
    var error = errorResponse.getErrors().get(0);
    assertThat(error.getCode()).isEqualTo(ErrorCode.VALIDATION_ERROR.getValue());
    assertThat(error.getMessage()).isEqualTo("Primary main title should be presented");
    assertThat(error.getType()).isEqualTo(MethodArgumentNotValidException.class.getSimpleName());
    assertThat(error.getParameters()).hasSize(1);
    assertThat(error.getParameters().get(0).getKey()).isEqualTo("resource.work.title");
    assertThat(error.getParameters().get(0).getValue()).isEqualTo("[]");
  }
}
