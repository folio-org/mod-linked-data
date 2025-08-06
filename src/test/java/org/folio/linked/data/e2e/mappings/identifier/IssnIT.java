package org.folio.linked.data.e2e.mappings.identifier;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.folio.linked.data.e2e.mappings.PostResourceIT;
import org.folio.linked.data.model.entity.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.ResultActions;

public class IssnIT extends PostResourceIT {

  @Autowired
  private ObjectMapper objectMapper;

  @Override
  protected String postPayload() {
    return """
      {
         "resource":{
            "http://bibfra.me/vocab/lite/Instance":{
               "http://bibfra.me/vocab/marc/title":[
                  {
                     "http://bibfra.me/vocab/marc/Title":{
                        "http://bibfra.me/vocab/marc/mainTitle":[ "%s" ]
                     }
                  }
               ],
               "http://library.link/vocab/map":[
                  {
                     "http://library.link/identifier/ISSN":{
                        "http://bibfra.me/vocab/lite/name":[ "1234567890" ],
                        "http://bibfra.me/vocab/marc/status":[
                           {
                              "http://bibfra.me/vocab/lite/link":[ "http://id.loc.gov/vocabulary/mstatus/current" ],
                              "http://bibfra.me/vocab/lite/label":[ "current" ]
                           }
                        ]
                     }
                  }
               ]
            }
         }
      }"""
      .formatted("TEST: " + this.getClass().getSimpleName());
  }

  @Override
  @SneakyThrows
  protected void validateApiResponse(ResultActions apiResponse) {
    var responsePayload = apiResponse.andReturn().getResponse().getContentAsString();

    var issnNode = objectMapper.readTree(responsePayload)
      .path("resource")
      .path("http://bibfra.me/vocab/lite/Instance")
      .path("http://library.link/vocab/map").get(0)
      .path("http://library.link/identifier/ISSN");

    var issnValueNode = issnNode.path("http://bibfra.me/vocab/lite/name").get(0);
    assertThat(issnValueNode.asText()).isEqualTo("1234567890");

    var statusNode = issnNode.path("http://bibfra.me/vocab/marc/status").get(0);
    var statusLink = statusNode.path("http://bibfra.me/vocab/lite/link").get(0);
    var statusLabel = statusNode.path("http://bibfra.me/vocab/lite/label").get(0);
    assertThat(statusLink.asText()).isEqualTo("http://id.loc.gov/vocabulary/mstatus/current");
    assertThat(statusLabel.asText()).isEqualTo("current");
  }

  @Override
  protected void validateGraph(Resource resource) {
    var issn = getFirstOutgoingResource(resource, "http://library.link/vocab/map");
    validateResourceType(issn,
      "http://bibfra.me/vocab/lite/Identifier", "http://library.link/identifier/ISSN");
    assertThat(getProperty(issn, "http://bibfra.me/vocab/lite/name")).isEqualTo("1234567890");
    assertThat(issn.getLabel()).isEqualTo("1234567890");

    var status = getFirstOutgoingResource(issn, "http://bibfra.me/vocab/marc/status");
    validateResourceType(status, "http://bibfra.me/vocab/marc/Status");
    assertThat(getProperty(status, "http://bibfra.me/vocab/lite/label")).isEqualTo("current");
    assertThat(getProperty(status, "http://bibfra.me/vocab/lite/link"))
      .isEqualTo("http://id.loc.gov/vocabulary/mstatus/current");
    assertThat(status.getLabel()).isEqualTo("current");
  }
}
