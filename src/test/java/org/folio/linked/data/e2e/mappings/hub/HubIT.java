package org.folio.linked.data.e2e.mappings.hub;

import org.folio.linked.data.e2e.mappings.PostResourceIT;
import org.folio.linked.data.model.entity.Resource;
import org.springframework.test.web.servlet.ResultActions;

import java.io.UnsupportedEncodingException;

public class HubIT extends PostResourceIT {
  @Override
  protected String postPayload() {
    return """
      {
        "resource": {
          "http://bibfra.me/vocab/lite/Hub": {
            "http://bibfra.me/vocab/library/title": [
              {
                "http://bibfra.me/vocab/library/Title": {
                  "http://bibfra.me/vocab/library/mainTitle": [
                    "%s"
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
  protected void validateApiResponse(ResultActions apiResponse) {
    try {
      System.out.println(apiResponse.andReturn().getResponse().getContentAsString());
    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  protected void validateGraph(Resource resource) {
    System.out.println(resource.getLabel());
  }
}
