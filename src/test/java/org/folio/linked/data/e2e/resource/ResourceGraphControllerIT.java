package org.folio.linked.data.e2e.resource;

import static org.folio.ld.dictionary.PredicateDictionary.PROVIDER_PLACE;
import static org.folio.ld.dictionary.PropertyDictionary.NAME;
import static org.folio.ld.dictionary.PropertyDictionary.PROVIDER_DATE;
import static org.folio.ld.dictionary.PropertyDictionary.SIMPLE_PLACE;
import static org.folio.ld.dictionary.ResourceTypeDictionary.PROVIDER_EVENT;
import static org.folio.linked.data.test.TestUtil.TENANT_ID;
import static org.folio.linked.data.test.TestUtil.cleanResourceTables;
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
import org.folio.linked.data.service.tenant.TenantScopedExecutionService;
import org.folio.linked.data.test.MonographTestUtil;
import org.folio.linked.data.test.resource.ResourceTestService;
import org.folio.spring.tools.kafka.KafkaAdminService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

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
  @Autowired
  private TenantScopedExecutionService tenantScopedExecutionService;
  @MockBean
  private KafkaAdminService kafkaAdminService;

  @BeforeEach
  public void clean() {
    tenantScopedExecutionService.execute(TENANT_ID, () ->
      cleanResourceTables(jdbcTemplate)
    );
  }

  @Test
  void getResourceGraphById_shouldReturnResourceGraph() throws Exception {
    // given
    var providerEvent = MonographTestUtil.providerEvent("production", "af", "Afghanistan");
    var date = Timestamp.valueOf(LocalDateTime.parse("2018-05-05T11:50:55"));
    providerEvent.setIndexDate(date);
    var providerEventResource = resourceTestService.saveGraph(providerEvent);

    // when
    var providerEventResultActions = performGet(providerEventResource.getId());

    // then
    var providerEventResponse = providerEventResultActions
      .andExpect(status().isOk())
      .andExpect(content().contentType(APPLICATION_JSON))
      .andReturn().getResponse().getContentAsString();
    var providerEventGraphDto = objectMapper.readValue(providerEventResponse, ResourceGraphDto.class);

    assertNotNull(providerEventGraphDto.getId());
    assertEquals(List.of(PROVIDER_EVENT.getUri()), providerEventGraphDto.getTypes());
    assertEquals("production name",
      ((Map<String, List<String>>) providerEventGraphDto.getDoc()).get(NAME.getValue()).get(0));
    assertEquals("production provider date",
      ((Map<String, List<String>>) providerEventGraphDto.getDoc()).get(PROVIDER_DATE.getValue()).get(0));
    assertEquals("production simple place",
      ((Map<String, List<String>>) providerEventGraphDto.getDoc()).get(SIMPLE_PLACE.getValue()).get(0));
    assertEquals("production name", providerEventGraphDto.getLabel());
    var providerPlaceHash = (providerEventGraphDto.getOutgoingEdges()).getEdges()
      .get(PROVIDER_PLACE.getUri()).get(0);
    assertNotNull(providerPlaceHash);
    assertEquals(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX").format(date),
      providerEventGraphDto.getIndexDate());
    assertEquals(0, providerEventGraphDto.getIncomingEdges().getTotalElements()); // No incoming edges to ProviderEvent
    assertEquals(1, providerEventGraphDto.getOutgoingEdges().getTotalElements()); // 1 outgoing edge to Place resource

    var providerPlaceResultActions = performGet(providerPlaceHash);
    var providerPlaceResponse = providerPlaceResultActions
      .andExpect(status().isOk())
      .andExpect(content().contentType(APPLICATION_JSON))
      .andReturn().getResponse().getContentAsString();
    var providerPlaceGraphDto = objectMapper.readValue(providerPlaceResponse, ResourceGraphDto.class);
    var providerPlaceSourceHash = providerPlaceGraphDto.getIncomingEdges().getEdges()
      .get(PROVIDER_PLACE.getUri()).get(0);
    assertEquals(providerPlaceSourceHash, providerEventResource.getId());
    assertEquals(1, providerPlaceGraphDto.getIncomingEdges().getTotalElements()); // 1 incoming edge from ProviderEvent
    assertEquals(0, providerPlaceGraphDto.getOutgoingEdges().getTotalElements()); // No outgoing edges from Place
  }

  private ResultActions performGet(Long resourceId) throws Exception {
    var requestBuilder = get("/linked-data/resource/" + resourceId + "/graph")
      .contentType(APPLICATION_JSON)
      .headers(defaultHeaders(env));

    return mockMvc.perform(requestBuilder);
  }
}
