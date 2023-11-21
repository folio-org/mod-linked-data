package org.folio.linked.data.e2e;

import static org.folio.linked.data.test.MonographTestUtil.getSampleInstanceResource;
import static org.folio.linked.data.test.TestUtil.defaultHeaders;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import lombok.SneakyThrows;
import org.folio.linked.data.e2e.base.IntegrationTest;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.repo.ResourceRepository;
import org.folio.linked.data.test.ResourceEdgeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.test.web.servlet.MockMvc;

@IntegrationTest
class ReIndexControllerIT {

  public static final String INDEX_URL = "/reindex";

  @Autowired
  private MockMvc mockMvc;
  @Autowired
  private ResourceRepository resourceRepo;
  @Autowired
  private ResourceEdgeRepository resourceEdgeRepository;
  @Autowired
  private Environment env;

  @BeforeEach
  public void clean() {
    resourceEdgeRepository.deleteAll();
    resourceRepo.deleteAll();
  }

  @Test
  void createIndexIfTrue_Ok() throws Exception {
    // given
    var persisted = resourceRepo.save(getSampleInstanceResource());

    var requestBuilder = put(INDEX_URL)
      .contentType(APPLICATION_JSON)
      .headers(defaultHeaders(env));

    // when
    mockMvc.perform(requestBuilder);

    // then
    checkKafkaMessageSent(persisted);
  }

  @SneakyThrows
  protected void checkKafkaMessageSent(Resource persisted) {
    // no exception should be thrown
  }

}
