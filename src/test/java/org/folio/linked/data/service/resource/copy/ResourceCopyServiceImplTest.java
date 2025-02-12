package org.folio.linked.data.service.resource.copy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.ld.dictionary.ResourceTypeDictionary.INSTANCE;
import static org.folio.ld.dictionary.ResourceTypeDictionary.WORK;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Stream;
import org.folio.linked.data.model.entity.RawMarc;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceTypeEntity;
import org.folio.linked.data.service.resource.edge.ResourceEdgeService;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@UnitTest
class ResourceCopyServiceImplTest {

  @InjectMocks
  private ResourceCopyServiceImpl service;

  @Mock
  private ResourceEdgeService resourceEdgeService;
  @Mock
  private ObjectMapper objectMapper;

  @Test
  void copyEdgesAndProperties_shouldRetainUnmappedMarc_whenUpdatedResourceIsInstance() {
    // given
    var old = new Resource();
    var rawMarc = "raw marc";
    old.setUnmappedMarc(new RawMarc(old).setContent(rawMarc));
    var updated = new Resource().addTypes(INSTANCE);

    // when
    service.copyEdgesAndProperties(old, updated);

    // then
    assertNotNull(updated.getUnmappedMarc());
    assertEquals(rawMarc, updated.getUnmappedMarc().getContent());
    verify(resourceEdgeService).copyOutgoingEdges(old, updated);
  }

  @Test
  void copyEdgesAndProperties_shouldNotRetainUnmappedMarc_whenUpdatedResourceIsNotInstance() {
    // given
    var old = new Resource();
    old.setUnmappedMarc(new RawMarc(old).setContent("raw marc"));
    var updated = new Resource();

    // when
    service.copyEdgesAndProperties(old, updated);

    // then
    assertNull(updated.getUnmappedMarc());
    verify(resourceEdgeService).copyOutgoingEdges(old, updated);
  }

  @Test
  void copyEdgesAndProperties_shouldNotCopyProperties_whenOldResourceDoesNotHaveDoc() {
    // given
    var old = new Resource();
    var updated = new Resource();

    // when
    service.copyEdgesAndProperties(old, updated);

    // then
    verifyNoInteractions(objectMapper);
    verify(resourceEdgeService).copyOutgoingEdges(old, updated);
  }

  private static Stream<Arguments> dataProvider() {
    return Stream.of(
      arguments(new Resource()
          .addType(new ResourceTypeEntity().setUri(INSTANCE.getUri())).setDoc(new ArrayNode(new JsonNodeFactory(true))),
        new Resource(), getOldInstanceDoc(), getUpdatedInstanceDoc()),
      arguments(new Resource()
          .addType(new ResourceTypeEntity().setUri(WORK.getUri())).setDoc(new ArrayNode(new JsonNodeFactory(true))),
        new Resource(), getOldWorkDoc(), getUpdatedWorkDoc())
    );
  }

  @ParameterizedTest
  @MethodSource("dataProvider")
  void copyEdgesAndProperties_shouldRetainProperties_thatAreNotSupportedOnUi(Resource old,
                                                                             Resource updated,
                                                                             HashMap<String, List<String>> fromDoc,
                                                                             HashMap<String, List<String>> expectedDoc)
    throws JsonProcessingException {
    // given
    when(objectMapper.treeToValue(eq(old.getDoc()), any(TypeReference.class))).thenReturn(fromDoc);

    // when
    service.copyEdgesAndProperties(old, updated);

    // then
    var docCaptor = ArgumentCaptor.forClass(HashMap.class);
    verify(objectMapper).convertValue(docCaptor.capture(), eq(JsonNode.class));
    assertThat(docCaptor.getValue()).isEqualTo(expectedDoc);
    verify(resourceEdgeService).copyOutgoingEdges(old, updated);
  }

  private static HashMap<String, List<String>> getOldInstanceDoc() {
    var doc = getUpdatedInstanceDoc();
    doc.put("http://bibfra.me/vocab/lite/note", List.of("generalNote"));
    return doc;
  }

  private static HashMap<String, List<String>> getUpdatedInstanceDoc() {
    var doc = new HashMap<String, List<String>>();
    doc.put("http://bibfra.me/vocab/marc/publicationFrequency", List.of("publicationFrequency"));
    doc.put("http://bibfra.me/vocab/marc/datesOfPublicationNote", List.of("datesOfPublicationNote"));
    doc.put("http://bibfra.me/vocab/marc/governingAccessNote", List.of("governingAccessNote"));
    doc.put("http://bibfra.me/vocab/marc/creditsNote", List.of("creditsNote"));
    doc.put("http://bibfra.me/vocab/marc/participantNote", List.of("participantNote"));
    doc.put("http://bibfra.me/vocab/marc/citationCoverage", List.of("citationCoverage"));
    doc.put("http://bibfra.me/vocab/marc/locationOfOriginalsDuplicates", List.of("locationOfOriginalsDuplicates"));
    return doc;
  }

  private static HashMap<String, List<String>> getOldWorkDoc() {
    var doc = getUpdatedWorkDoc();
    doc.put("http://bibfra.me/vocab/lite/note", List.of("generalNote"));
    return doc;
  }

  private static HashMap<String, List<String>> getUpdatedWorkDoc() {
    var doc = new HashMap<String, List<String>>();
    doc.put("http://bibfra.me/vocab/marc/references", List.of("references"));
    doc.put("http://bibfra.me/vocab/marc/otherEventInformation", List.of("otherEventInformation"));
    doc.put("http://bibfra.me/vocab/marc/geographicCoverage", List.of("geographicCoverage"));
    return doc;
  }
}
