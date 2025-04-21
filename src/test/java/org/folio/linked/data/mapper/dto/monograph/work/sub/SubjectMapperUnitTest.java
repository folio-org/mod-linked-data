package org.folio.linked.data.mapper.dto.monograph.work.sub;

import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.ld.dictionary.PredicateDictionary.FOCUS;
import static org.folio.ld.dictionary.ResourceTypeDictionary.CONCEPT;
import static org.folio.ld.dictionary.ResourceTypeDictionary.FORM;
import static org.folio.ld.dictionary.ResourceTypeDictionary.PERSON;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import org.folio.linked.data.domain.dto.Reference;
import org.folio.linked.data.model.entity.Resource;
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
}
