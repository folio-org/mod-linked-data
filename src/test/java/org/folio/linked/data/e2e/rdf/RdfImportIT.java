package org.folio.linked.data.e2e.rdf;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.folio.linked.data.test.TestUtil.defaultHeaders;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.folio.linked.data.e2e.base.IntegrationTest;
import org.folio.linked.data.repo.ResourceRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@IntegrationTest
class RdfImportIT {
  private static final String IMPORT_ENDPOINT = "/linked-data/import/file";
  @Autowired
  private MockMvc mockMvc;
  @Autowired
  private Environment env;
  @Autowired
  private ResourceRepository resourceRepo;

  @Test
  void rdfImport_shouldSaveImportedResourceAndSendEventAndReturnId() throws Exception {
    // given
    var input = this.getClass().getResourceAsStream("/rdf/instance.json");
    var multipartFile = new MockMultipartFile("fileName", "instance.json",
      "application/ld+json", input);
    var requestBuilder = MockMvcRequestBuilders.multipart(IMPORT_ENDPOINT)
      .file(multipartFile)
      .headers(defaultHeaders(env));
    var expectedId = - 4596132172574222417L;

    // when
    var resultActions = mockMvc.perform(requestBuilder);

    // then
    var response = resultActions
      .andExpect(status().isOk())
      .andReturn().getResponse().getContentAsString();
    assertThat(response).isEqualTo(String.valueOf(expectedId));
    assertThat(resourceRepo.existsById(expectedId)).isTrue();
  }

}
