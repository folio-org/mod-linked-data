package org.folio.linked.data.e2e;

import static org.folio.linked.data.test.MonographTestUtil.getSampleInstanceResource;
import static org.folio.linked.data.test.TestUtil.defaultHeaders;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import java.util.Date;
import lombok.SneakyThrows;
import org.folio.linked.data.e2e.base.IntegrationTest;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.repo.ResourceRepository;
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
  private Environment env;

  @Test
  void indexResourceWithNoIndexDate_andNotFullIndexRequest() throws Exception {
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

  @Test
  void notIndexResourceWithIndexDate_andNotFullIndexRequest() throws Exception {
    // given
    resourceRepo.save(getSampleInstanceResource().setIndexDate(new Date()));

    var requestBuilder = put(INDEX_URL)
      .contentType(APPLICATION_JSON)
      .headers(defaultHeaders(env));

    // when
    mockMvc.perform(requestBuilder);

    // then
    checkKafkaMessageSent(null);
  }

  @Test
  void indexResourceWithNoIndexDate_andFullIndexRequest() throws Exception {
    // given
    var persisted = resourceRepo.save(getSampleInstanceResource());

    var requestBuilder = put(INDEX_URL)
      .param("full", "true")
      .contentType(APPLICATION_JSON)
      .headers(defaultHeaders(env));

    // when
    mockMvc.perform(requestBuilder);

    // then
    checkKafkaMessageSent(persisted);
  }

  @Test
  void indexResourceWithIndexDate_andFullIndexRequest() throws Exception {
    // given
    var persisted = resourceRepo.save(getSampleInstanceResource().setIndexDate(new Date()));

    var requestBuilder = put(INDEX_URL)
      .param("full", "true")
      .contentType(APPLICATION_JSON)
      .headers(defaultHeaders(env));

    // when
    mockMvc.perform(requestBuilder);

    // then
    checkKafkaMessageSent(persisted);
  }

  @SneakyThrows
  protected void checkKafkaMessageSent(Resource indexed) {
    // no exception should be thrown
  }

}
