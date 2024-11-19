package org.folio.linked.data.e2e;

import static org.folio.linked.data.test.TestUtil.defaultHeaders;
import static org.folio.linked.data.test.TestUtil.loadResourceAsString;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.folio.linked.data.domain.dto.AssignmentCheckDto;
import org.folio.linked.data.e2e.base.IntegrationTest;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.test.web.servlet.MockMvc;

@IntegrationTest
class AuthorityAssignmentControllerIT {

  private static final String ASSIGNMENT_CHECK_ENDPOINT = "/linked-data/authority-assignment-check";

  @Autowired
  private Environment env;

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @ParameterizedTest
  @CsvSource({
    "samples/marc/authority_person.json, true",
    "samples/marc/authority_family.json, true",
    "samples/marc/authority_organization.json, true",
    "samples/marc/authority_jurisdiction.json, true",
    "samples/marc/authority_concept_meeting.json, false",
    "samples/marc/non_authority.json, false",
  })
  void authorityAssignmentCheck(String marcFile, String expectedResponse) throws Exception {
    // given
    var requestBuilder = post(ASSIGNMENT_CHECK_ENDPOINT)
      .contentType(APPLICATION_JSON)
      .headers(defaultHeaders(env))
      .content(
        objectMapper.writeValueAsString(
          new AssignmentCheckDto(
            loadResourceAsString(marcFile),
            AssignmentCheckDto.TargetEnum.CREATOR_OF_WORK))
      );

    // when
    var resultActions = mockMvc.perform(requestBuilder);

    // then
    resultActions
      .andExpect(status().isOk())
      .andExpect(content().string(expectedResponse));
  }
}
