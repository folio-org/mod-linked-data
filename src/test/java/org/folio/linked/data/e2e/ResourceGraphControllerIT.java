package org.folio.linked.data.e2e;

import static org.folio.ld.dictionary.PredicateDictionary.PROVIDER_PLACE;
import static org.folio.ld.dictionary.PropertyDictionary.NAME;
import static org.folio.ld.dictionary.PropertyDictionary.PROVIDER_DATE;
import static org.folio.ld.dictionary.PropertyDictionary.SIMPLE_PLACE;
import static org.folio.ld.dictionary.ResourceTypeDictionary.PROVIDER_EVENT;
import static org.folio.linked.data.test.TestUtil.defaultHeaders;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.folio.linked.data.domain.dto.ResourceGraphDto;
import org.folio.linked.data.e2e.base.IntegrationTest;
import org.folio.linked.data.test.MonographTestUtil;
import org.folio.linked.data.utils.ResourceTestService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.jdbc.JdbcTestUtils;
import org.springframework.test.web.servlet.MockMvc;

@IntegrationTest
class ResourceGraphControllerIT {

  @Autowired
  private JdbcTemplate jdbcTemplate;

  @Autowired
  private ResourceTestService resourceTestService;

  @Autowired
  private Environment env;

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @BeforeEach
  public void clean() {
    JdbcTestUtils.deleteFromTables(jdbcTemplate, "resource_edges", "resource_type_map", "resources");
  }

  @Test
  void getResourceGraphById_shouldReturnResourceGraph() throws Exception {
    // given
    var providerEvent = MonographTestUtil.providerEvent("production");
    var date = Timestamp.valueOf(LocalDateTime.parse("2018-05-05T11:50:55"));
    providerEvent.setIndexDate(date);
    var existingResource = resourceTestService.saveGraph(providerEvent);
    var requestBuilder = get("/graph/resource/" + existingResource.getResourceHash())
      .contentType(APPLICATION_JSON)
      .headers(defaultHeaders(env));

    // when
    var resultActions = mockMvc.perform(requestBuilder);

    // then
    var response = resultActions
      .andExpect(status().isOk())
      .andExpect(content().contentType(APPLICATION_JSON))
      .andReturn().getResponse().getContentAsString();
    var resourceGraphDto = objectMapper.readValue(response, ResourceGraphDto.class);

    assertNotNull(resourceGraphDto.getId());
    assertEquals(List.of(PROVIDER_EVENT.getUri()), resourceGraphDto.getTypes());
    assertEquals("production name",
      ((Map<String, List<String>>) resourceGraphDto.getDoc()).get(NAME.getValue()).get(0));
    assertEquals("production provider date",
      ((Map<String, List<String>>) resourceGraphDto.getDoc()).get(PROVIDER_DATE.getValue()).get(0));
    assertEquals("production simple place",
      ((Map<String, List<String>>) resourceGraphDto.getDoc()).get(SIMPLE_PLACE.getValue()).get(0));
    assertEquals("production name", resourceGraphDto.getLabel());
    assertNotNull(
      ((Map<String, List<String>>) resourceGraphDto.getOutgoingEdges()).get(PROVIDER_PLACE.getUri()).get(0));
    assertEquals(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX").format(date), resourceGraphDto.getIndexDate());
  }
}
