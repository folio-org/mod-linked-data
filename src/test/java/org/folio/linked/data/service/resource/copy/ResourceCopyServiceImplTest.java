package org.folio.linked.data.service.resource.copy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.ld.dictionary.ResourceTypeDictionary.HUB;
import static org.folio.ld.dictionary.ResourceTypeDictionary.INSTANCE;
import static org.folio.ld.dictionary.ResourceTypeDictionary.WORK;
import static org.folio.linked.data.test.TestUtil.TEST_JSON_MAPPER;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.Mockito.verify;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceTypeEntity;
import org.folio.linked.data.service.resource.edge.ResourceEdgeService;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.JsonNode;

@ExtendWith(MockitoExtension.class)
@UnitTest
class ResourceCopyServiceImplTest {

  @InjectMocks
  private ResourceCopyServiceImpl service;

  @Mock
  private ResourceEdgeService resourceEdgeService;

  @Test
  void copyEdgesAndProperties_shouldRetainUnmappedMarc_whenUpdatedResourceIsInstance() {
    // given
    var old = new Resource();
    var updated = new Resource().addTypes(INSTANCE);

    // when
    service.copyEdgesAndProperties(old, updated);

    // then
    verify(resourceEdgeService).copyOutgoingEdges(old, updated);
    verify(resourceEdgeService).copyIncomingEdges(old, updated);
  }

  @Test
  void copyEdgesAndProperties_shouldNotCopyProperties_whenOldResourceDoesNotHaveDoc() {
    // given
    var old = new Resource();
    var updated = new Resource();

    // when
    service.copyEdgesAndProperties(old, updated);

    // then
    verify(resourceEdgeService).copyOutgoingEdges(old, updated);
  }

  private static Stream<Arguments> dataProvider() {
    return Stream.of(
      arguments(
        createResourceWithDoc(INSTANCE.getUri(), getOldInstanceDoc()),
        new Resource(),
        getUpdatedInstanceDoc()
      ),
      arguments(
        createResourceWithDoc(WORK.getUri(), getOldWorkDoc()),
        new Resource(),
        getUpdatedWorkDoc()
      ),
      arguments(
        createResourceWithDoc(HUB.getUri(), getOldHubDoc()),
        new Resource(),
        getUpdatedHubDoc()
      )
    );
  }

  @ParameterizedTest
  @MethodSource("dataProvider")
  void copyEdgesAndProperties_shouldRetainProperties_thatAreNotSupportedOnUi(Resource old,
                                                                             Resource updated,
                                                                             Map<String, List<String>> expectedDoc) {
    // when
    service.copyEdgesAndProperties(old, updated);

    // then
    var actualDoc = TEST_JSON_MAPPER.treeToValue(updated.getDoc(),
      new TypeReference<HashMap<String, List<String>>>() {}
    );
    assertThat(actualDoc).isEqualTo(expectedDoc);
    verify(resourceEdgeService).copyOutgoingEdges(old, updated);
  }

  private static Resource createResourceWithDoc(String typeUri, HashMap<String, List<String>> doc) {
    JsonNode docNode = TEST_JSON_MAPPER.convertValue(doc, JsonNode.class);
    return new Resource()
      .addType(new ResourceTypeEntity().setUri(typeUri))
      .setDoc(docNode);
  }

  private static HashMap<String, List<String>> getOldInstanceDoc() {
    var doc = getUpdatedInstanceDoc();
    doc.put("http://bibfra.me/vocab/lite/note", List.of("generalNote"));
    return doc;
  }

  private static HashMap<String, List<String>> getUpdatedInstanceDoc() {
    var doc = new HashMap<String, List<String>>();
    doc.put("http://bibfra.me/vocab/library/governingAccessNote", List.of("governingAccessNote"));
    doc.put("http://bibfra.me/vocab/library/creditsNote", List.of("creditsNote"));
    doc.put("http://bibfra.me/vocab/library/participantNote", List.of("participantNote"));
    doc.put("http://bibfra.me/vocab/library/citationCoverage", List.of("citationCoverage"));
    doc.put("http://bibfra.me/vocab/library/locationOfOriginalsDuplicates", List.of("locationOfOriginalsDuplicates"));
    doc.put("http://bibfra.me/vocab/lite/link", List.of("instanceLink"));
    doc.put("http://bibfra.me/vocab/library/accessibilityNote", List.of("accessibilityNote"));
    return doc;
  }

  private static HashMap<String, List<String>> getOldWorkDoc() {
    var doc = getUpdatedWorkDoc();
    doc.put("http://bibfra.me/vocab/lite/note", List.of("generalNote"));
    return doc;
  }

  private static HashMap<String, List<String>> getUpdatedWorkDoc() {
    var doc = new HashMap<String, List<String>>();
    doc.put("http://bibfra.me/vocab/library/references", List.of("references"));
    doc.put("http://bibfra.me/vocab/library/otherEventInformation", List.of("otherEventInformation"));
    doc.put("http://bibfra.me/vocab/library/geographicCoverage", List.of("geographicCoverage"));
    doc.put("http://bibfra.me/vocab/lite/link", List.of("workLink"));
    return doc;
  }

  private static HashMap<String, List<String>> getOldHubDoc() {
    var doc = getUpdatedHubDoc();
    doc.put("http://bibfra.me/vocab/lite/note", List.of("generalNote"));
    return doc;
  }

  private static HashMap<String, List<String>> getUpdatedHubDoc() {
    var doc = new HashMap<String, List<String>>();
    doc.put("http://bibfra.me/vocab/lite/link", List.of("hubLink"));
    return doc;
  }
}
