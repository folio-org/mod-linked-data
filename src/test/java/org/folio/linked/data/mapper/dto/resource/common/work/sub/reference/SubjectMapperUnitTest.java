package org.folio.linked.data.mapper.dto.resource.common.work.sub.reference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.ld.dictionary.PredicateDictionary.FOCUS;
import static org.folio.ld.dictionary.PredicateDictionary.SUB_FOCUS;
import static org.folio.ld.dictionary.ResourceTypeDictionary.CONCEPT;
import static org.folio.ld.dictionary.ResourceTypeDictionary.FORM;
import static org.folio.ld.dictionary.ResourceTypeDictionary.PERSON;
import static org.folio.ld.dictionary.ResourceTypeDictionary.PLACE;
import static org.folio.ld.dictionary.ResourceTypeDictionary.TOPIC;
import static org.folio.linked.data.test.TestUtil.readTree;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import java.util.List;
import org.folio.linked.data.domain.dto.Reference;
import org.folio.linked.data.domain.dto.WorkResponse;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceEdge;
import org.folio.linked.data.model.entity.ResourceTypeEntity;
import org.folio.linked.data.service.resource.hash.HashService;
import org.folio.linked.data.service.resource.marc.ResourceMarcAuthorityService;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@UnitTest
@ExtendWith(MockitoExtension.class)
class SubjectMapperUnitTest {

  @InjectMocks
  private SubjectMapperUnit subjectMapperUnit;
  @Mock
  private HashService hashService;
  @Mock
  private ResourceMarcAuthorityService resourceMarcAuthorityService;

  @Test
  void toEntity_wrapsWithConcept_whenSubjectIsNotOfTypeConcept() {
    // given
    var name = "Sample Name";
    var doc = JsonNodeFactory.instance.objectNode();
    doc.set("http://library.link/vocab/resourcePreferred", JsonNodeFactory.instance.arrayNode().add("true"));
    doc.set("http://bibfra.me/vocab/lite/name", JsonNodeFactory.instance.arrayNode().add(name));
    var resource = new Resource().setLabel("Label").addTypes(PERSON).setDoc(doc);
    var subjectDto = new Reference()
      .srsId("123");
    when(resourceMarcAuthorityService.fetchAuthorityOrCreateFromSrsRecord(subjectDto)).thenReturn(resource);
    when(hashService.hash(any(Resource.class))).thenReturn(123L);

    // when
    var result = subjectMapperUnit.toEntity(subjectDto, new Resource());

    // then
    assertThat(result).isNotNull();
    assertThat(result.getDoc().has("http://library.link/vocab/resourcePreferred")).isFalse();
    assertThat(result.getDoc().get("http://bibfra.me/vocab/lite/name").get(0).asText()).isEqualTo(name);
    assertThat(result.getTypes().stream().map(ResourceTypeEntity::getUri)).contains(CONCEPT.getUri());
    assertThat(result.getOutgoingEdges())
      .hasSize(1)
      .allSatisfy(edge -> {
        assertThat(edge.getPredicate().getUri()).isEqualTo(FOCUS.getUri());
        assertThat(edge.getTarget()).isEqualTo(resource);
        assertThat(edge.getTarget().getDoc()).isEqualTo(doc);
      });
    assertThat(result.getId()).isEqualTo(123L);
  }

  @Test
  void toEntity_returnsSubjectDirectly_whenSubjectIsOfTypeConcept() {
    // given
    var resource = new Resource().setLabel("Label").addTypes(CONCEPT, FORM);
    var subjectDto = new Reference()
      .srsId("123");
    when(resourceMarcAuthorityService.fetchAuthorityOrCreateFromSrsRecord(subjectDto)).thenReturn(resource);

    // when
    var result = subjectMapperUnit.toEntity(subjectDto, new Resource());

    // then
    assertThat(result).isSameAs(resource);
  }

  @Test
  void toDto_shouldConvertResourceToDto() {
    // given
    var label = "label string";
    var id = 1L;
    var resource = new Resource()
      .setDoc(readTree("""
        {
            "http://library.link/vocab/resourcePreferred": ["true"]
        }
        """))
      .setLabel(label)
      .setId(id)
      .addTypes(CONCEPT, PLACE);

    // when
    var result = subjectMapperUnit.toDto(resource, new WorkResponse(2), null);

    // then
    assertThat(result.getSubjects()).hasSize(1);
    var subject = result.getSubjects().getFirst();
    assertThat(subject.getIsPreferred()).isTrue();
    assertThat(subject.getLabel()).isEqualTo(label);
    assertThat(subject.getId()).isEqualTo(id + "");
    assertThat(subject.getTypes()).isEqualTo(List.of(CONCEPT.getUri(), PLACE.getUri()));
  }

  @Test
  void toDto_preferredFlagShouldBeFalse_whenResourceHasSubFocusEdge() {
    // given
    var focus = new Resource()
      .setLabel("John Doe")
      .addTypes(PERSON)
      .setDoc(readTree("""
        {
            "http://library.link/vocab/resourcePreferred": ["true"]
        }
        """));

    var subFocus = new Resource()
      .setLabel("Childhood")
      .addTypes(TOPIC)
      .setDoc(readTree("""
        {
            "http://library.link/vocab/resourcePreferred": ["true"]
        }
        """));

    var conceptWithSubFocus = new Resource()
      .setLabel("John Doe -- Childhood")
      .addTypes(CONCEPT);

    conceptWithSubFocus.addOutgoingEdge(new ResourceEdge(conceptWithSubFocus, focus, FOCUS));
    conceptWithSubFocus.addOutgoingEdge(new ResourceEdge(conceptWithSubFocus, subFocus, SUB_FOCUS));

    // when
    var result = subjectMapperUnit.toDto(conceptWithSubFocus, new WorkResponse(2), null);

    // then
    assertThat(result.getSubjects()).hasSize(1);
    var subject = result.getSubjects().getFirst();
    assertThat(subject.getIsPreferred()).isFalse();
  }

  @Test
  void toDto_shouldFetchPreferredFlagFromFocus_whenResourceHasNoSubFocusEdges() {
    // given
    var focus = new Resource()
      .setLabel("John Doe")
      .addTypes(PERSON)
      .setDoc(readTree("""
        {
            "http://library.link/vocab/resourcePreferred": ["true"]
        }
        """));

    var conceptWithoutSubFocus = new Resource()
      .setLabel("John Doe")
      .addTypes(CONCEPT);

    conceptWithoutSubFocus.addOutgoingEdge(new ResourceEdge(conceptWithoutSubFocus, focus, FOCUS));

    // when
    var result = subjectMapperUnit.toDto(conceptWithoutSubFocus, new WorkResponse(2), null);

    // then
    assertThat(result.getSubjects()).hasSize(1);
    var subject = result.getSubjects().getFirst();
    assertThat(subject.getIsPreferred()).isTrue();
  }

  @Test
  void toDto_shouldDefaultPreferredFlagToFalse() {
    // given
    var focusWithNoPreferredFlag = new Resource()
      .setLabel("John Doe")
      .addTypes(PERSON);

    var concept = new Resource()
      .setLabel("John Doe")
      .addTypes(CONCEPT);

    concept.addOutgoingEdge(new ResourceEdge(concept, focusWithNoPreferredFlag, FOCUS));

    // when
    var result = subjectMapperUnit.toDto(concept, new WorkResponse(2), null);

    // then
    assertThat(result.getSubjects()).hasSize(1);
    var subject = result.getSubjects().getFirst();
    assertThat(subject.getIsPreferred()).isFalse();
  }
}
