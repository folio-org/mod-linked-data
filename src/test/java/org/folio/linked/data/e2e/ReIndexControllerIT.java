package org.folio.linked.data.e2e;

import static org.folio.linked.data.test.TestUtil.TENANT_ID;
import static org.folio.linked.data.test.TestUtil.defaultHeaders;
import static org.folio.linked.data.test.TestUtil.getBibframeSample;
import static org.folio.linked.data.test.TestUtil.getBibframeSampleTest;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import lombok.SneakyThrows;
import org.folio.linked.data.configuration.properties.BibframeProperties;
import org.folio.linked.data.domain.dto.BibframeRequest;
import org.folio.linked.data.e2e.base.IntegrationTest;
import org.folio.linked.data.mapper.BibframeMapper;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.repo.ResourceRepository;
import org.folio.linked.data.test.ResourceEdgeRepository;
import org.folio.spring.test.extension.impl.OkapiConfiguration;
import org.folio.spring.tools.kafka.KafkaAdminService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.Pageable;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@IntegrationTest
@Transactional
class ReIndexControllerIT {

  public static final String INDEX_URL = "/reindex";
  public static OkapiConfiguration okapi;

  @Autowired
  private MockMvc mockMvc;
  @Autowired
  private ResourceRepository resourceRepo;
  @Autowired
  private ResourceEdgeRepository resourceEdgeRepository;
  @Autowired
  private BibframeMapper bibframeMapper;
  @Autowired
  private ObjectMapper objectMapper;
  @Autowired
  private BibframeProperties bibframeProperties;
  @Autowired
  private Environment env;

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
  void createIndexIfTrue_Ok() throws Exception {
    List<Resource> resources = createMonograph();

    var requestBuilder = put(INDEX_URL)
      .contentType(APPLICATION_JSON)
      .headers(defaultHeaders(env, okapi.getOkapiUrl()));

    mockMvc.perform(requestBuilder);

    resources.forEach(this::checkKafkaMessageSent);
  }

  @SneakyThrows
  protected void checkKafkaMessageSent(Resource persisted) {
  }

  private List<Resource> createMonograph() throws Exception {
    BibframeRequest bibframeRequest1 = objectMapper.readValue(getBibframeSample(), BibframeRequest.class);
    System.out.println(bibframeRequest1);
    Resource resource1 = bibframeMapper.toEntity(bibframeRequest1);
    resourceRepo.save(resource1);

    BibframeRequest bibframeRequest2 = objectMapper.readValue(getBibframeSampleTest("77 mm"), BibframeRequest.class);
    System.out.println(bibframeRequest2);
    Resource resource2 = bibframeMapper.toEntity(bibframeRequest2);
    resourceRepo.save(resource2);

    return resourceRepo.findAllResourcesByType(bibframeProperties.getProfiles(), Pageable.ofSize(10000))
      .getContent();
  }
}
