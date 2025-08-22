package org.folio.linked.data.e2e.mappings.publicationfrequency;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.folio.linked.data.e2e.mappings.PostResourceIT;
import org.folio.linked.data.model.entity.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.ResultActions;

public class PublicationFrequencyIT extends PostResourceIT {

  @Autowired
  private ObjectMapper objectMapper;

  @Override
  protected String postPayload() {
    return """
      {
         "resource":{
            "http://bibfra.me/vocab/lite/Instance":{
               "profileId": 3,
               "http://bibfra.me/vocab/marc/title":[
                  {
                     "http://bibfra.me/vocab/marc/Title":{
                        "http://bibfra.me/vocab/marc/mainTitle":[ "%s" ]
                     }
                  }
               ],
               "http://bibfra.me/vocab/marc/publicationFrequency":[
                  {
                     "http://bibfra.me/vocab/lite/label":["annual"],
                     "http://bibfra.me/vocab/lite/link":["http://id.loc.gov/vocabulary/frequencies/ann"]
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
    var publicationFrequency = objectMapper.readTree(responsePayload)
      .path("resource")
      .path("http://bibfra.me/vocab/lite/Instance")
      .path("http://bibfra.me/vocab/marc/publicationFrequency").get(0);
    var labelNode = publicationFrequency.path("http://bibfra.me/vocab/lite/label").get(0);
    var linkNode = publicationFrequency.path("http://bibfra.me/vocab/lite/link").get(0);
    var codeNote = publicationFrequency.path("http://bibfra.me/vocab/marc/code").get(0);
    assertThat(labelNode.asText()).isEqualTo("annual");
    assertThat(linkNode.asText()).isEqualTo("http://id.loc.gov/vocabulary/frequencies/ann");
    assertThat(codeNote.asText()).isEqualTo("a");
  }

  @Override
  protected void validateGraph(Resource resource) {
    var publicationFrequency = getFirstOutgoingResource(resource, "http://bibfra.me/vocab/marc/publicationFrequency");
    validateResourceType(publicationFrequency, "http://bibfra.me/vocab/lite/Frequency");
    assertThat(getProperty(publicationFrequency, "http://bibfra.me/vocab/lite/label")).isEqualTo("annual");
    assertThat(getProperty(publicationFrequency, "http://bibfra.me/vocab/lite/link"))
      .isEqualTo("http://id.loc.gov/vocabulary/frequencies/ann");
    assertThat(getProperty(publicationFrequency, "http://bibfra.me/vocab/marc/code")).isEqualTo("a");
    assertThat(publicationFrequency.getLabel()).isEqualTo("annual");
  }
}
