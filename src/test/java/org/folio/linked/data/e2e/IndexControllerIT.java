package org.folio.linked.data.e2e;

import static org.folio.linked.data.test.TestUtil.TENANT_ID;
import static org.folio.linked.data.test.TestUtil.defaultHeaders;
import static org.folio.linked.data.test.TestUtil.getBibframe2Sample;
import static org.folio.linked.data.test.TestUtil.getIndexFalseSample;
import static org.folio.linked.data.test.TestUtil.getIndexTrueSample;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import lombok.SneakyThrows;
import org.folio.linked.data.configuration.properties.BibframeProperties;
import org.folio.linked.data.domain.dto.Bibframe2Request;
import org.folio.linked.data.e2e.base.IntegrationTest;
import org.folio.linked.data.mapper.BibframeMapper;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.repo.ResourceRepository;
import org.folio.linked.data.test.MonographTestService;
import org.folio.linked.data.test.ResourceEdgeRepository;
import org.folio.spring.test.extension.impl.OkapiConfiguration;
import org.folio.spring.tools.kafka.KafkaAdminService;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

@IntegrationTest
@Transactional
class IndexControllerIT {

  public static final String INDEX_URL = "/index";
  public static OkapiConfiguration okapi;

  @Autowired
  private MockMvc mockMvc;
  @Autowired
  private Environment env;
  @Autowired
  private ResourceRepository resourceRepo;
  @Autowired
  private ResourceEdgeRepository resourceEdgeRepository;
  @Autowired
  private BibframeMapper bibframeMapper;
  @Autowired
  private ObjectMapper objectMapper;
  @Autowired
  private MonographTestService monographTestService;
  @Autowired
  private BibframeProperties bibframeProperties;


  @BeforeAll
  static void beforeAll(@Autowired KafkaAdminService kafkaAdminService) {
    kafkaAdminService.createTopics(TENANT_ID);
  }

  @BeforeEach
  public void clean() {
    resourceEdgeRepository.deleteAll();
    resourceRepo.deleteAll();
  }

  @Test
  void createIndexIfTrue_Ok() throws Exception {
    List<Resource> resources = createMonograph();

    var requestBuilder = post(INDEX_URL)
      .contentType(APPLICATION_JSON)
      .headers(defaultHeaders(env, okapi.getOkapiUrl()))
      .content(getIndexTrueSample());

    validateSampleIndexResponse(mockMvc.perform(requestBuilder), 1)
      .andReturn()
      .getResponse()
      .getContentAsString();

    resources.forEach(this::checkKafkaMessageSent);
  }

  @Test
  void createNoIndexIfFalse_Ok() throws Exception {
    createMonograph();

    var requestBuilder = post(INDEX_URL)
      .contentType(APPLICATION_JSON)
      .headers(defaultHeaders(env, okapi.getOkapiUrl()))
      .content(getIndexFalseSample());

    validateSampleIndexResponse(mockMvc.perform(requestBuilder), 0)
      .andReturn()
      .getResponse()
      .getContentAsString();
  }

  @SneakyThrows
  protected void checkKafkaMessageSent(Resource persisted) {
  }

  private List<Resource> createMonograph() throws Exception {
    Bibframe2Request bibframe2Request = objectMapper.readValue(getBibframe2Sample(), Bibframe2Request.class);
    var mapped1 = bibframeMapper.toEntity2(bibframe2Request);
    resourceRepo.save(mapped1);

    return resourceRepo.findResourcesByType(bibframeProperties.getProfiles());
  }

  @NotNull
  private ResultActions validateSampleIndexResponse(ResultActions resultActions, Integer count) throws Exception {
    return resultActions
      .andExpect(status().isOk())
      .andExpect(content().contentType(APPLICATION_JSON))
      .andExpect(jsonPath("status", equalTo("ok")))
      .andExpect(jsonPath("count", is(count)));
  }

}
