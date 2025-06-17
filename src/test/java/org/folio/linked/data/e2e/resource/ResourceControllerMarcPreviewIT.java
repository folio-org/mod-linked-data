package org.folio.linked.data.e2e.resource;

import static org.folio.linked.data.e2e.resource.ResourceControllerITBase.RESOURCE_URL;
import static org.folio.linked.data.test.MonographTestUtil.getSampleInstanceResource;
import static org.folio.linked.data.test.TestUtil.defaultHeaders;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.folio.linked.data.e2e.ITBase;
import org.folio.linked.data.e2e.base.IntegrationTest;
import org.folio.linked.data.model.entity.RawMarc;
import org.folio.linked.data.repo.RawMarcRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@IntegrationTest
class ResourceControllerMarcPreviewIT extends ITBase {
  @Autowired
  private RawMarcRepository rawMarcRepository;

  @Test
  void shouldDeriveMarcFromGraph() throws Exception {
    // given
    var existed = resourceTestService.saveGraph(getSampleInstanceResource());
    var unmappedMarc = """
      {
         "fields":[ {
           "630":{ "subfields":[ { "a":"Unmapped field" } ] }
         } ]
      }""";
    rawMarcRepository.save(new RawMarc(existed).setContent(unmappedMarc));
    var requestBuilder = get(RESOURCE_URL + "/" + existed.getId() + "/marc")
      .contentType(APPLICATION_JSON)
      .headers(defaultHeaders(env));

    // when
    var resultActions = mockMvc.perform(requestBuilder);

    // then
    resultActions
      .andExpect(status().isOk())
      .andExpect(content().contentType(APPLICATION_JSON))
      .andExpect(jsonPath("id", equalTo(existed.getId().toString())))
      .andExpect(jsonPath("recordType", equalTo("MARC_BIB")))
      .andExpect(jsonPath("parsedRecord.content", notNullValue()))
      .andExpect(jsonPath("parsedRecord.content.fields[*]['630'].subfields[0].a", hasItem("Unmapped field")));
  }
}
