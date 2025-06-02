package org.folio.linked.data.e2e.rdf;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.folio.linked.data.test.TestUtil.defaultHeaders;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Optional;
import org.folio.ld.dictionary.model.Resource;
import org.folio.linked.data.e2e.base.IntegrationTest;
import org.folio.linked.data.integration.ResourceModificationEventListener;
import org.folio.linked.data.repo.ResourceRepository;
import org.folio.linked.data.service.rdf.LccnResourceProvider;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
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
  @MockitoSpyBean
  private ResourceModificationEventListener eventListener;
  @MockitoBean
  private LccnResourceProvider lccnResourceProvider;

  @Test
  void rdfImport_shouldSaveImportedResourceAndSendEventAndReturnId() throws Exception {
    // given
    var fileName = "instance.json";
    var input = this.getClass().getResourceAsStream("/rdf/" + fileName);
    var multipartFile = new MockMultipartFile("fileName", fileName,
      "application/ld+json", input);
    var requestBuilder = MockMvcRequestBuilders.multipart(IMPORT_ENDPOINT)
      .file(multipartFile)
      .headers(defaultHeaders(env));
    var expectedId = 2413788589667169533L;

    // when
    var resultActions = mockMvc.perform(requestBuilder);

    // then
    var response = resultActions
      .andExpect(status().isOk())
      .andReturn().getResponse().getContentAsString();
    assertThat(response).isEqualTo("[" + expectedId + "]");
    assertThat(resourceRepo.existsById(expectedId)).isTrue();
    verify(eventListener).afterCreate(any());
  }

  @Test
  void rdfImport_shouldFetchAuthorityByLccn() throws Exception {
    // given
    var fileName = "instance_work_agents_ids.json";
    var input = this.getClass().getResourceAsStream("/rdf/" + fileName);
    var multipartFile = new MockMultipartFile("fileName", fileName,
      "application/ld+json", input);
    var requestBuilder = MockMvcRequestBuilders.multipart(IMPORT_ENDPOINT)
      .file(multipartFile)
      .headers(defaultHeaders(env));
    var expectedId = -42292089353776380L;
    var lccn = "n2021004098";
    var existedAuthority = new Resource().setId(123L).setLabel(lccn);
    when(lccnResourceProvider.apply(lccn)).thenReturn(Optional.of(existedAuthority));

    // when
    var resultActions = mockMvc.perform(requestBuilder);

    // then
    var response = resultActions
      .andExpect(status().isOk())
      .andReturn().getResponse().getContentAsString();
    assertThat(response).isEqualTo("[" + expectedId + "]");
    assertThat(resourceRepo.existsById(expectedId)).isTrue();
    assertThat(resourceRepo.existsById(existedAuthority.getId())).isTrue();
    verify(eventListener).afterCreate(any());
  }

}
