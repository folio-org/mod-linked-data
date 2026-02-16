package org.folio.linked.data.e2e.mappings.instance.publicationfrequency;

import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.linked.data.test.TestUtil.TEST_JSON_MAPPER;

import lombok.SneakyThrows;
import org.folio.linked.data.e2e.mappings.PostResourceIT;
import org.folio.linked.data.model.entity.Resource;
import org.springframework.test.web.servlet.ResultActions;

public class PublicationFrequencyIT extends PostResourceIT {

  @Override
  protected String postPayload() {
    return """
      {
         "resource":{
            "http://bibfra.me/vocab/lite/Instance":{
               "profileId": 3,
               "http://bibfra.me/vocab/library/title":[
                  {
                     "http://bibfra.me/vocab/library/Title":{
                        "http://bibfra.me/vocab/library/mainTitle":[ "%s" ]
                     }
                  }
               ],
               "http://bibfra.me/vocab/library/publicationFrequency":[
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
    var publicationFrequency = TEST_JSON_MAPPER.readTree(responsePayload)
      .path("resource")
      .path("http://bibfra.me/vocab/lite/Instance")
      .path("http://bibfra.me/vocab/library/publicationFrequency").get(0);
    var labelNode = publicationFrequency.path("http://bibfra.me/vocab/lite/label").get(0);
    var linkNode = publicationFrequency.path("http://bibfra.me/vocab/lite/link").get(0);
    var codeNote = publicationFrequency.path("http://bibfra.me/vocab/library/code").get(0);
    assertThat(labelNode.asString()).isEqualTo("annual");
    assertThat(linkNode.asString()).isEqualTo("http://id.loc.gov/vocabulary/frequencies/ann");
    assertThat(codeNote.asString()).isEqualTo("a");
  }

  @Override
  protected void validateGraph(Resource resource) {
    var publicationFrequency = getFirstOutgoingResource(resource, "http://bibfra.me/vocab/library/publicationFrequency");
    validateResourceType(publicationFrequency, "http://bibfra.me/vocab/lite/Frequency");
    assertThat(getProperty(publicationFrequency, "http://bibfra.me/vocab/lite/label")).isEqualTo("annual");
    assertThat(getProperty(publicationFrequency, "http://bibfra.me/vocab/lite/link"))
      .isEqualTo("http://id.loc.gov/vocabulary/frequencies/ann");
    assertThat(getProperty(publicationFrequency, "http://bibfra.me/vocab/library/code")).isEqualTo("a");
    assertThat(publicationFrequency.getLabel()).isEqualTo("annual");
  }
}
