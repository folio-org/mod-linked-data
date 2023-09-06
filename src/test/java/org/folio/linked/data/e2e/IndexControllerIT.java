package org.folio.linked.data.e2e;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.folio.linked.data.domain.dto.Bibframe2Response;
import org.folio.linked.data.e2e.base.IntegrationTest;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.repo.ResourceRepository;
import org.folio.linked.data.test.MonographTestService;
import org.folio.linked.data.test.ResourceEdgeRepository;
import org.folio.spring.test.extension.impl.OkapiConfiguration;
import org.folio.spring.tools.kafka.KafkaAdminService;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.linked.data.test.TestUtil.*;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@IntegrationTest
@Transactional
class IndexControllerIT {

  public static final String INDEX_URL = "/index";
  public static final String BIBFRAME_URL = "/bibframe2";
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
  private ObjectMapper objectMapper;
  @Autowired
  private MonographTestService monographTestService;


  @BeforeAll
  static void beforeAll(@Autowired KafkaAdminService kafkaAdminService) {
    kafkaAdminService.createTopics(TENANT_ID);
  }

  @AfterEach
  public void clean() {
    resourceEdgeRepository.deleteAll();
    resourceRepo.deleteAll();
  }

  @Test
  void createIndexIfTrue_OK() throws Exception {
    List<Resource> resources = createTwoMonographInstancesWithSharedResources();

    var requestBuilder = post(INDEX_URL)
      .contentType(APPLICATION_JSON)
      .headers(defaultHeaders(env, okapi.getOkapiUrl()))
      .content(getIndexTrueSample());

    validateSampleIndexResponse(mockMvc.perform(requestBuilder), 54)
      .andReturn()
      .getResponse()
      .getContentAsString();

    resources.forEach(this::checkKafkaMessageSent);
  }

  @Test
  void createNoIndexIfFalse_OK() throws Exception {
    List<Resource> resources = createTwoMonographInstancesWithSharedResources();
    System.out.println("resoure size" + resources.size());

    var requestBuilder = post(INDEX_URL)
      .contentType(APPLICATION_JSON)
      .headers(defaultHeaders(env, okapi.getOkapiUrl()))
      .content(getIndexFalseSample());

    var resultActions = validateSampleIndexResponse(mockMvc.perform(requestBuilder), 0)
      .andReturn()
      .getResponse()
      .getContentAsString();

    System.out.println("resultactions: " + resultActions);
  }

  @SneakyThrows
  protected void checkKafkaMessageSent(Resource persisted) {
  }

  private List<Resource> createTwoMonographInstancesWithSharedResources() throws Exception {
    List<Resource> resourceList = new ArrayList<>();

    var requestBuilder1 = post(BIBFRAME_URL)
      .contentType(APPLICATION_JSON)
      .headers(defaultHeaders(env, okapi.getOkapiUrl()))
      .content(getBibframe2Sample());
    var resultActions1 = mockMvc.perform(requestBuilder1);
    var response1 = resultActions1.andReturn().getResponse().getContentAsString();
    var bibframeResponse1 = objectMapper.readValue(response1, Bibframe2Response.class);
    var persistedOptional1 = resourceRepo.findById(bibframeResponse1.getId());
    assertThat(persistedOptional1).isPresent();
    resourceList.add(persistedOptional1.get());

    var requestBuilder2 = post(BIBFRAME_URL)
      .contentType(APPLICATION_JSON)
      .headers(defaultHeaders(env, okapi.getOkapiUrl()))
      .content(getBibframe2Sample().replace("volume", "length"));

    // when
    var response2 = mockMvc.perform(requestBuilder2).andReturn().getResponse().getContentAsString();
    var bibframeResponse2 = objectMapper.readValue(response2, Bibframe2Response.class);
    var persistedOptional2 = resourceRepo.findById(bibframeResponse2.getId());

    assertThat(persistedOptional2).isPresent();
    resourceList.add(persistedOptional2.get());

    return resourceList;
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
