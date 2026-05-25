package org.folio.linked.data.e2e.rdf;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.folio.linked.data.test.MonographTestUtil.getSampleInstanceResourceForRdfExport;
import static org.folio.linked.data.test.MonographTestUtil.getSampleInstanceWithProvisionActivities;
import static org.folio.linked.data.test.MonographTestUtil.getSampleInstanceWithWorkComplexSubject;
import static org.folio.linked.data.test.MonographTestUtil.getSampleInstanceWithWorkCreatorLccn;
import static org.folio.linked.data.test.MonographTestUtil.getSampleInstanceWithWorkCreatorNoLccn;
import static org.folio.linked.data.test.MonographTestUtil.getSampleInstanceWithWorkSubjectLccn;
import static org.folio.linked.data.test.MonographTestUtil.getSampleInstanceWithWorkSubjectNoLccn;
import static org.folio.linked.data.test.MonographTestUtil.getSampleInstanceWithWorkTitlesForRdfExport;
import static org.folio.linked.data.test.TestUtil.defaultHeaders;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.folio.linked.data.e2e.base.IntegrationTest;
import org.folio.linked.data.test.kafka.KafkaProducerTestConfiguration;
import org.folio.linked.data.test.resource.ResourceTestService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.test.web.servlet.MockMvc;

@IntegrationTest
@SpringBootTest(classes = {KafkaProducerTestConfiguration.class})
class RdfExportIT {
  private static final String EXPORT_ENDPOINT = "/linked-data/resource/{id}/rdf";
  @Autowired
  private MockMvc mockMvc;
  @Autowired
  private Environment env;
  @Autowired
  private ResourceTestService resourceTestService;

  @Test
  void rdfExport_shouldReturnExportedResultForExistedInstance() throws Exception {
    // given
    var existed = resourceTestService.saveGraph(getSampleInstanceResourceForRdfExport());
    var requestBuilder = get(EXPORT_ENDPOINT.replace("{id}", existed.getId().toString()))
      .contentType(APPLICATION_JSON)
      .headers(defaultHeaders(env));

    // when
    var resultActions = mockMvc.perform(requestBuilder);

    // then
    var response = resultActions
      .andExpect(status().isOk())
      .andExpect(content().contentType(APPLICATION_JSON))
      .andReturn().getResponse().getContentAsString();
    assertThat(response).contains("http://id.loc.gov/ontologies/bibframe/Title");
    assertThat(response).contains("Primary: mainTitle");
    assertThat(response).contains("http://id.loc.gov/ontologies/bibframe/ParallelTitle");
    assertThat(response).contains("Parallel: mainTitle");
    assertThat(response).contains("http://id.loc.gov/vocabulary/vartitletype/por");
    assertThat(response).contains("http://id.loc.gov/vocabulary/vartitletype/dis");
    assertThat(response).contains("http://id.loc.gov/vocabulary/vartitletype/cov");
    assertThat(response).contains("http://id.loc.gov/vocabulary/vartitletype/atp");
    assertThat(response).contains("http://id.loc.gov/vocabulary/vartitletype/cap");
    assertThat(response).contains("http://id.loc.gov/vocabulary/vartitletype/run");
    assertThat(response).contains("http://id.loc.gov/vocabulary/vartitletype/spi");
    assertThat(response).contains("http://id.loc.gov/ontologies/bibframe/dimensions");
    assertThat(response).contains("20 cm");
    assertThat(response).contains("http://id.loc.gov/ontologies/bibframe/responsibilityStatement");
    assertThat(response).contains("statement of responsibility");
  }

  @Test
  void rdfExport_shouldReturnExportedResultWithWorkAndInstanceTitles() throws Exception {
    // given
    var existed = resourceTestService.saveGraph(getSampleInstanceWithWorkTitlesForRdfExport());
    var requestBuilder = get(EXPORT_ENDPOINT.replace("{id}", existed.getId().toString()))
      .contentType(APPLICATION_JSON)
      .headers(defaultHeaders(env));

    // when
    var resultActions = mockMvc.perform(requestBuilder);

    // then
    var response = resultActions
      .andExpect(status().isOk())
      .andExpect(content().contentType(APPLICATION_JSON))
      .andReturn().getResponse().getContentAsString();
    assertThat(response).contains("http://id.loc.gov/ontologies/bibframe/Instance");
    assertThat(response).contains("Instance: mainTitle");
    assertThat(response).contains("Instance Parallel: mainTitle");
    assertThat(response).contains("Instance Variant: mainTitle");
    assertThat(response).contains("http://id.loc.gov/ontologies/bibframe/Work");
    assertThat(response).contains("Work: mainTitle");
    assertThat(response).contains("Work Parallel: mainTitle");
    assertThat(response).contains("Work Variant: mainTitle");
    assertThat(response).contains("http://id.loc.gov/vocabulary/vartitletype/por");
  }

  @Test
  void rdfExport_shouldReturnRdfWithCreatorAgentLccnUri() throws Exception {
    // given
    var existed = resourceTestService.saveGraph(getSampleInstanceWithWorkCreatorLccn());
    var requestBuilder = get(EXPORT_ENDPOINT.replace("{id}", existed.getId().toString()))
      .contentType(APPLICATION_JSON)
      .headers(defaultHeaders(env));

    // when
    var resultActions = mockMvc.perform(requestBuilder);

    // then
    var response = resultActions
      .andExpect(status().isOk())
      .andExpect(content().contentType(APPLICATION_JSON))
      .andReturn().getResponse().getContentAsString();
    assertThat(response).contains("http://id.loc.gov/rwo/agents/n2021004098");
  }

  @Test
  void rdfExport_shouldReturnRdfWithCreatorAgentAsBlankNodeWhenNoLccn() throws Exception {
    // given
    var existed = resourceTestService.saveGraph(getSampleInstanceWithWorkCreatorNoLccn());
    var requestBuilder = get(EXPORT_ENDPOINT.replace("{id}", existed.getId().toString()))
      .contentType(APPLICATION_JSON)
      .headers(defaultHeaders(env));

    // when
    var resultActions = mockMvc.perform(requestBuilder);

    // then
    var response = resultActions
      .andExpect(status().isOk())
      .andExpect(content().contentType(APPLICATION_JSON))
      .andReturn().getResponse().getContentAsString();
    assertThat(response).doesNotContain("http://id.loc.gov/rwo/agents/");
    assertThat(response).contains("Creator No LCCN");
  }

  @Test
  void rdfExport_shouldReturnRdfWithSubjectLccnUri() throws Exception {
    // given
    var existed = resourceTestService.saveGraph(getSampleInstanceWithWorkSubjectLccn());
    var requestBuilder = get(EXPORT_ENDPOINT.replace("{id}", existed.getId().toString()))
      .contentType(APPLICATION_JSON)
      .headers(defaultHeaders(env));

    // when
    var resultActions = mockMvc.perform(requestBuilder);

    // then
    var response = resultActions
      .andExpect(status().isOk())
      .andExpect(content().contentType(APPLICATION_JSON))
      .andReturn().getResponse().getContentAsString();
    assertThat(response).contains("http://id.loc.gov/rwo/agents/n2021009876");
  }

  @Test
  void rdfExport_shouldReturnRdfWithSubjectAsBlankNodeWhenNoLccn() throws Exception {
    // given
    var existed = resourceTestService.saveGraph(getSampleInstanceWithWorkSubjectNoLccn());
    var requestBuilder = get(EXPORT_ENDPOINT.replace("{id}", existed.getId().toString()))
      .contentType(APPLICATION_JSON)
      .headers(defaultHeaders(env));

    // when
    var resultActions = mockMvc.perform(requestBuilder);

    // then
    var response = resultActions
      .andExpect(status().isOk())
      .andExpect(content().contentType(APPLICATION_JSON))
      .andReturn().getResponse().getContentAsString();
    assertThat(response).doesNotContain("http://id.loc.gov/rwo/agents/");
    assertThat(response).contains("Subject No LCCN Person");
  }

  @Test
  void rdfExport_shouldReturnRdfWithComplexSubject() throws Exception {
    // given
    var existed = resourceTestService.saveGraph(getSampleInstanceWithWorkComplexSubject());
    var requestBuilder = get(EXPORT_ENDPOINT.replace("{id}", existed.getId().toString()))
      .contentType(APPLICATION_JSON)
      .headers(defaultHeaders(env));

    // when
    var resultActions = mockMvc.perform(requestBuilder);

    // then
    var response = resultActions
      .andExpect(status().isOk())
      .andExpect(content().contentType(APPLICATION_JSON))
      .andReturn().getResponse().getContentAsString();
    assertThat(response).contains("Complex Subject Person");
    assertThat(response).contains("Complex Subject Topic");
  }

  @Test
  void rdfExport_shouldReturnRdfWithAllProvisionActivities() throws Exception {
    // given
    var existed = resourceTestService.saveGraph(getSampleInstanceWithProvisionActivities());
    var requestBuilder = get(EXPORT_ENDPOINT.replace("{id}", existed.getId().toString()))
      .contentType(APPLICATION_JSON)
      .headers(defaultHeaders(env));

    // when
    var resultActions = mockMvc.perform(requestBuilder);

    // then
    var response = resultActions
      .andExpect(status().isOk())
      .andExpect(content().contentType(APPLICATION_JSON))
      .andReturn().getResponse().getContentAsString();
    assertThat(response).contains("http://id.loc.gov/ontologies/bibframe/provisionActivity");
    assertThat(response).contains("http://id.loc.gov/ontologies/bibframe/Publication");
    assertThat(response).contains("http://id.loc.gov/ontologies/bibframe/Distribution");
    assertThat(response).contains("http://id.loc.gov/ontologies/bibframe/Manufacture");
    assertThat(response).contains("http://id.loc.gov/ontologies/bibframe/Production");
  }
}
