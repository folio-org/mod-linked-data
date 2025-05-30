package org.folio.linked.data.e2e.resource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.linked.data.e2e.resource.ResourceControllerITBase.RESOURCE_URL;
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
import org.folio.linked.data.e2e.base.IntegrationTestStandalone;
import org.folio.spring.tools.kafka.KafkaAdminService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

@IntegrationTestStandalone
class ResourceControllerBaseValidationIT extends AbstractResourceControllerIT {

  @Autowired
  private ObjectMapper objectMapper;
  @MockitoSpyBean
  private KafkaAdminService kafkaAdminService;

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
    var expectedError = getError("instance", """
      [class PrimaryTitleField {
          primaryTitle: class PrimaryTitle {
              partName: [Primary: partName]
              partNumber: [Primary: partNumber]
              mainTitle: []
              subTitle: [Primary: subTitle]
              nonSortNum: [Primary: nonSortNum]
          }
      }]""");

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
      .code("required_primary_main_title")
      .parameters(List.of(
        new Parameter().key("field").value("resource." + resourceType + ".title"),
        new Parameter().key("value").value(value)
      ));
  }
}
