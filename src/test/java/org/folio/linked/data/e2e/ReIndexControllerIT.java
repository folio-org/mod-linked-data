package org.folio.linked.data.e2e;

import static org.folio.ld.dictionary.ResourceTypeDictionary.INSTANCE;
import static org.folio.linked.data.test.TestUtil.bibframeSampleResource;
import static org.folio.linked.data.test.TestUtil.defaultHeaders;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Set;
import lombok.SneakyThrows;
import org.folio.ld.dictionary.ResourceTypeDictionary;
import org.folio.linked.data.e2e.base.IntegrationTest;
import org.folio.linked.data.mapper.ResourceMapper;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.repo.ResourceRepository;
import org.folio.linked.data.test.ResourceEdgeRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.Pageable;
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
  private ResourceMapper resourceMapper;
  @Autowired
  private ObjectMapper objectMapper;
  @Autowired
  private Environment env;

  @AfterEach
  public void clean() {
    resourceEdgeRepository.deleteAll();
    resourceRepo.deleteAll();
  }

  @Test
  void createIndexIfTrue_Ok() throws Exception {
    var resources = createMonograph();

    var requestBuilder = put(INDEX_URL)
      .contentType(APPLICATION_JSON)
      .headers(defaultHeaders(env));

    mockMvc.perform(requestBuilder);

    resources.forEach(this::checkKafkaMessageSent);
  }

  @SneakyThrows
  protected void checkKafkaMessageSent(Resource persisted) {
  }

  private List<Resource> createMonograph() throws Exception {
    resourceRepo.save(bibframeSampleResource(1L, INSTANCE));
    resourceRepo.save(bibframeSampleResource(2L, INSTANCE));
    return resourceRepo.findResourcesByTypeFull(Set.of(ResourceTypeDictionary.INSTANCE.getUri()),
        Pageable.ofSize(10000))
      .getContent();
  }
}
