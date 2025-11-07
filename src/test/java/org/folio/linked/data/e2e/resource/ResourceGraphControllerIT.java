package org.folio.linked.data.e2e.resource;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.folio.ld.dictionary.PredicateDictionary.PROVIDER_PLACE;
import static org.folio.ld.dictionary.PropertyDictionary.NAME;
import static org.folio.ld.dictionary.PropertyDictionary.PROVIDER_DATE;
import static org.folio.ld.dictionary.PropertyDictionary.SIMPLE_PLACE;
import static org.folio.ld.dictionary.ResourceTypeDictionary.PROVIDER_EVENT;
import static org.folio.linked.data.test.TestUtil.defaultHeaders;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Set;
import org.folio.ld.dictionary.model.Resource;
import org.folio.linked.data.e2e.ITBase;
import org.folio.linked.data.e2e.base.IntegrationTest;
import org.folio.linked.data.service.tenant.TenantScopedExecutionService;
import org.folio.linked.data.test.MonographTestUtil;
import org.folio.linked.data.test.resource.ResourceTestService;
import org.folio.spring.tools.kafka.KafkaAdminService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

@IntegrationTest
class ResourceGraphControllerIT extends ITBase {

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
  @MockitoSpyBean
  private KafkaAdminService kafkaAdminService;

  @Test
  void getResourceGraphById_shouldReturnResourceGraph() throws Exception {
    // given
    var providerEvent = MonographTestUtil.providerEvent("production", "af", "Afghanistan");
    var date = Timestamp.valueOf(LocalDateTime.parse("2018-05-05T11:50:55"));
    providerEvent.setIndexDate(date);
    var providerEventResource = resourceTestService.saveGraph(providerEvent);

    // when
    var providerEventResponse = performGet(providerEventResource.getId())
      .andExpect(status().isOk())
      .andExpect(content().contentType(APPLICATION_JSON))
      .andReturn().getResponse().getContentAsString();

    // then
    var providerEventModel = objectMapper.readValue(providerEventResponse, Resource.class);
    assertNotNull(providerEventModel.getId());
    assertThat(providerEventModel.getTypes()).isEqualTo(Set.of(PROVIDER_EVENT));
    assertThat(providerEventModel.getDoc().get(NAME.getValue()).get(0).asText()).isEqualTo("production name");
    assertThat(providerEventModel.getDoc().get(PROVIDER_DATE.getValue()).get(0).asText())
      .isEqualTo("production provider date");
    assertThat(providerEventModel.getDoc().get(SIMPLE_PLACE.getValue()).get(0).asText())
      .isEqualTo("production simple place");
    assertThat(providerEventModel.getIncomingEdges()).isEmpty();
    assertThat(providerEventModel.getOutgoingEdges()).hasSize(1);
    var providerPlaceEdge = providerEventModel.getOutgoingEdges().iterator().next();
    assertThat(providerPlaceEdge.getPredicate()).isEqualTo(PROVIDER_PLACE);

    var providerPlaceResponse = performGet(providerPlaceEdge.getTarget().getId())
      .andExpect(status().isOk())
      .andExpect(content().contentType(APPLICATION_JSON))
      .andReturn().getResponse().getContentAsString();
    var providerPlaceModel = objectMapper.readValue(providerPlaceResponse, Resource.class);
    assertThat(providerPlaceModel.getOutgoingEdges()).isEmpty();
    assertThat(providerPlaceModel.getIncomingEdges()).hasSize(1);
    var providerPlaceSourceEdge = providerPlaceModel.getIncomingEdges().iterator().next();
    assertThat(providerPlaceSourceEdge.getSource().getId()).isEqualTo(providerEventResource.getId());
  }

  private ResultActions performGet(Long resourceId) throws Exception {
    var requestBuilder = get("/linked-data/resource/" + resourceId + "/graph")
      .contentType(APPLICATION_JSON)
      .headers(defaultHeaders(env));

    return mockMvc.perform(requestBuilder);
  }
}
