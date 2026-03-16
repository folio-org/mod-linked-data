package org.folio.linked.data.e2e.mappings.work.dissertation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.folio.linked.data.test.TestUtil.TEST_JSON_MAPPER;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import lombok.SneakyThrows;
import org.folio.ld.dictionary.ResourceTypeDictionary;
import org.folio.linked.data.e2e.mappings.PostResourceIT;
import org.folio.linked.data.model.entity.FolioMetadata;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceTypeEntity;
import org.folio.linked.data.test.TestUtil;
import org.folio.linked.data.test.resource.ResourceTestService;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.ResultActions;
import tools.jackson.core.type.TypeReference;

class WorkDissertationIT extends PostResourceIT {
  private static final Resource GRANTING_INSTITUTION_1 = createOrganization(5481852630377445080L,
    "granting institution 1", null);
  private static final Resource GRANTING_INSTITUTION_2 = createOrganization(-6468470931408362304L,
    "granting institution 2", "srsId2");

  @Autowired
  private ResourceTestService resourceTestService;

  @BeforeEach
  void createDegreeGrantingInstitutions() {
    resourceTestService.saveGraph(GRANTING_INSTITUTION_1);
    resourceTestService.saveGraph(GRANTING_INSTITUTION_2);
  }

  @Override
  protected String postPayload() {
    return """
      {
        "resource": {
          "http://bibfra.me/vocab/lite/Work": {
            "profileId": 2,
            "http://bibfra.me/vocab/library/title": [
              {
                "http://bibfra.me/vocab/library/Title": {
                  "http://bibfra.me/vocab/library/mainTitle": [ "%s" ]
                }
              }
            ],
            "http://bibfra.me/vocab/scholar/dissertation": [
              {
                "http://bibfra.me/vocab/lite/note": [ "label 1" ],
                "http://bibfra.me/vocab/library/degree": [ "degree 1" ],
                "http://bibfra.me/vocab/lite/date": [ "dissertation year 1" ],
                "http://bibfra.me/vocab/library/miscInfo": [ "dissertation note 1" ],
                "http://bibfra.me/vocab/library/dissertationID": [ "dissertation id 1" ],
                "_grantingInstitutionReference": [
                  { "id": "%s" }
                ]
              },
              {
                "http://bibfra.me/vocab/lite/note": [ "label 2" ],
                "http://bibfra.me/vocab/library/degree": [ "degree 2" ],
                "http://bibfra.me/vocab/lite/date": [ "dissertation year 2" ],
                "http://bibfra.me/vocab/library/miscInfo": [ "dissertation note 2" ],
                "http://bibfra.me/vocab/library/dissertationID": [ "dissertation id 2" ],
                "_grantingInstitutionReference": [
                  { "srsId": "%s" }
                ]
              }
            ]
          }
        }
      }"""
      .formatted(
        "TEST: " + getClass().getSimpleName(),
        GRANTING_INSTITUTION_1.getId(),
        GRANTING_INSTITUTION_2.getFolioMetadata().getSrsId()
      );
  }

  @Override
  protected void validateGraph(Resource work) {
    var dissertations = getOutgoingResources(work, "http://bibfra.me/vocab/scholar/dissertation").stream()
      .sorted(Comparator.comparing(dissertation -> getProperty(dissertation,
        "http://bibfra.me/vocab/library/dissertationID")))
      .toList();

    assertThat(dissertations).hasSize(2);
    validateDissertation(dissertations.get(0), "1", GRANTING_INSTITUTION_1);
    validateDissertation(dissertations.get(1), "2", GRANTING_INSTITUTION_2);
  }

  @Override
  @SneakyThrows
  protected void validateApiResponse(ResultActions apiResponse) {
    var responseJson = apiResponse.andReturn().getResponse().getContentAsString();
    var dissertationsNode = TEST_JSON_MAPPER.readTree(responseJson)
      .path("resource")
      .path("http://bibfra.me/vocab/lite/Work")
      .path("http://bibfra.me/vocab/scholar/dissertation");

    var actual = TEST_JSON_MAPPER.convertValue(dissertationsNode, new TypeReference<List<Map<String, Object>>>() { })
      .stream()
      .sorted(Comparator.comparing(dissertation ->
        ((List<String>) dissertation.get("http://bibfra.me/vocab/library/dissertationID")).getFirst()))
      .toList();

    assertThat(actual).hasSize(2);
    validateApiResponseDissertation(actual.get(0), "1", GRANTING_INSTITUTION_1);
    validateApiResponseDissertation(actual.get(1), "2", GRANTING_INSTITUTION_2);
  }

  private void validateDissertation(Resource dissertation, String suffix, Resource grantingInstitution) {
    validateResourceType(dissertation, ResourceTypeDictionary.DISSERTATION.getUri());
    var expectedLabel = dissertationLabel(suffix);
    assertThat(dissertation.getLabel()).isEqualTo(expectedLabel);
    assertThat(getProperty(dissertation, "http://bibfra.me/vocab/lite/label")).isEqualTo(expectedLabel);
    assertThat(getProperty(dissertation, "http://bibfra.me/vocab/lite/note")).isEqualTo("label " + suffix);
    assertThat(getProperty(dissertation, "http://bibfra.me/vocab/library/degree")).isEqualTo("degree " + suffix);
    assertThat(getProperty(dissertation, "http://bibfra.me/vocab/lite/date")).isEqualTo("dissertation year " + suffix);
    assertThat(getProperty(dissertation, "http://bibfra.me/vocab/library/miscInfo"))
      .isEqualTo("dissertation note " + suffix);
    assertThat(getProperty(dissertation, "http://bibfra.me/vocab/library/dissertationID"))
      .isEqualTo("dissertation id " + suffix);

    var grantingInstitutions = getOutgoingResources(dissertation, "http://bibfra.me/vocab/relation/degreeGrantingInstitution");
    assertThat(grantingInstitutions).hasSize(1);
    assertThat(grantingInstitutions)
      .extracting(Resource::getId, Resource::getLabel)
      .containsExactly(tuple(grantingInstitution.getId(), grantingInstitution.getLabel()));
    grantingInstitutions.forEach(resource ->
      validateResourceType(resource, ResourceTypeDictionary.ORGANIZATION.getUri()));
  }

  @SuppressWarnings("unchecked")
  private void validateApiResponseDissertation(Map<String, Object> dissertation, String suffix,
                                               Resource grantingInstitution) {
    assertThat(dissertation).containsKey("id");
    assertThat(dissertation).containsEntry("http://bibfra.me/vocab/lite/note", List.of("label " + suffix));
    assertThat(dissertation).containsEntry("http://bibfra.me/vocab/library/degree", List.of("degree " + suffix));
    assertThat(dissertation).containsEntry("http://bibfra.me/vocab/lite/date", List.of("dissertation year " + suffix));
    assertThat(dissertation).containsEntry("http://bibfra.me/vocab/library/miscInfo", List.of("dissertation note " + suffix));
    assertThat(dissertation).containsEntry("http://bibfra.me/vocab/library/dissertationID",
      List.of("dissertation id " + suffix));

    var grantingInstitutionReferences = (List<Map<String, Object>>) dissertation.get("_grantingInstitutionReference");
    assertThat(grantingInstitutionReferences).hasSize(1);

    var reference = grantingInstitutionReferences.getFirst();
    assertThat(reference).containsEntry("id", String.valueOf(grantingInstitution.getId()));
    assertThat(reference).containsEntry("label", grantingInstitution.getLabel());
    assertThat(reference).containsEntry("types", List.of(ResourceTypeDictionary.ORGANIZATION.getUri()));
  }

  @SneakyThrows
  private static Resource createOrganization(Long id, String label, String srsId) {
    var resource = new Resource()
      .addType(new ResourceTypeEntity()
        .setHash(ResourceTypeDictionary.ORGANIZATION.getHash())
        .setUri(ResourceTypeDictionary.ORGANIZATION.getUri()))
      .setDoc(TestUtil.TEST_JSON_MAPPER.readTree("{}"))
      .setLabel(label)
      .setIdAndRefreshEdges(id);
    if (srsId != null) {
      resource.setFolioMetadata(new FolioMetadata(resource).setSrsId(srsId));
    }
    return resource;
  }

  private String dissertationLabel(String suffix) {
    return "label " + suffix + " degree " + suffix + " dissertation year " + suffix;
  }
}
