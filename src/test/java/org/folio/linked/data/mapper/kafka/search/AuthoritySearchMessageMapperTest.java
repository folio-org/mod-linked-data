package org.folio.linked.data.mapper.kafka.search;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.ld.dictionary.PredicateDictionary.MAP;
import static org.folio.ld.dictionary.PropertyDictionary.NAME;
import static org.folio.ld.dictionary.ResourceTypeDictionary.CONCEPT;
import static org.folio.ld.dictionary.ResourceTypeDictionary.IDENTIFIER;
import static org.folio.ld.dictionary.ResourceTypeDictionary.PERSON;
import static org.folio.linked.data.domain.dto.LinkedDataIdentifier.TypeEnum.LCCN;
import static org.folio.linked.data.domain.dto.ResourceIndexEventType.CREATE;
import static org.folio.linked.data.domain.dto.ResourceIndexEventType.DELETE;
import static org.folio.linked.data.domain.dto.ResourceIndexEventType.UPDATE;
import static org.folio.linked.data.test.TestUtil.getJsonNode;
import static org.folio.linked.data.test.TestUtil.randomLong;

import java.util.List;
import java.util.Map;
import org.folio.linked.data.domain.dto.LinkedDataAuthority;
import org.folio.linked.data.mapper.kafka.search.identifier.IndexIdentifierMapperImpl;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceEdge;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@UnitTest
@ExtendWith(MockitoExtension.class)
class AuthoritySearchMessageMapperTest {

  @InjectMocks
  private AuthoritySearchMessageMapperImpl authoritySearchMessageMapper;

  @Spy
  private IndexIdentifierMapperImpl innerIndexIdentifierMapper = new IndexIdentifierMapperImpl();

  @Test
  void toIndex_shouldReturnCorrectlyMappedEvent_forCreate() {
    // given
    var resource = buildPersonAuthorityWithIdentifier();

    // when
    var result = authoritySearchMessageMapper.toIndex(resource, CREATE);

    // then
    assertThat(result.getId()).isNotNull();
    assertThat(result.getResourceName()).isEqualTo("linked-data-authority");
    assertThat(result.getType()).isEqualTo(CREATE);
    assertThat(result.getNew()).isInstanceOf(LinkedDataAuthority.class);
    var authority = (LinkedDataAuthority) result.getNew();
    assertThat(authority.getId()).isEqualTo(resource.getId().toString());
    assertThat(authority.getLabel()).isEqualTo(resource.getLabel());
    assertThat(authority.getType()).isEqualTo(PERSON.getUri());
    assertThat(authority.getIdentifiers()).hasSize(1);
    assertThat(authority.getIdentifiers().getFirst().getValue()).isEqualTo("n2024001234");
    assertThat(authority.getIdentifiers().getFirst().getType()).isEqualTo(LCCN);
  }

  @Test
  void toIndex_shouldReturnCorrectType_forUpdate() {
    // given
    var resource = buildPersonAuthorityWithIdentifier();

    // when
    var result = authoritySearchMessageMapper.toIndex(resource, UPDATE);

    // then
    assertThat(result.getType()).isEqualTo(UPDATE);
  }

  @Test
  void toIndex_shouldReturnCorrectType_forDelete() {
    // given
    var resource = buildPersonAuthorityWithIdentifier();

    // when
    var result = authoritySearchMessageMapper.toIndex(resource, DELETE);

    // then
    assertThat(result.getType()).isEqualTo(DELETE);
  }

  @Test
  void toIndex_shouldGenerateUniqueEventIds() {
    // given
    var resource = buildPersonAuthorityWithIdentifier();

    // when
    var result1 = authoritySearchMessageMapper.toIndex(resource, CREATE);
    var result2 = authoritySearchMessageMapper.toIndex(resource, CREATE);

    // then
    assertThat(result1.getId()).isNotEqualTo(result2.getId());
  }

  @Test
  void toIndex_shouldExcludeConceptType_whenAuthorityHasConceptAndPersonTypes() {
    // given
    var resource = new Resource()
      .setIdAndRefreshEdges(randomLong())
      .setLabel("Person with Concept type")
      .addTypes(CONCEPT)
      .addTypes(PERSON);

    // when
    var result = authoritySearchMessageMapper.toIndex(resource, CREATE);

    // then
    var authority = (LinkedDataAuthority) result.getNew();
    assertThat(authority.getType()).isEqualTo(PERSON.getUri());
  }

  @Test
  void toIndex_shouldReturnNullType_whenOnlyConceptTypePresent() {
    // given
    var resource = new Resource()
      .setIdAndRefreshEdges(randomLong())
      .setLabel("Only Concept")
      .addTypes(CONCEPT);

    // when
    var result = authoritySearchMessageMapper.toIndex(resource, CREATE);

    // then
    var authority = (LinkedDataAuthority) result.getNew();
    assertThat(authority.getType()).isNull();
  }

  @Test
  void toIndex_shouldReturnEmptyIdentifiers_whenResourceHasNoMapEdges() {
    // given
    var resource = new Resource()
      .setIdAndRefreshEdges(randomLong())
      .setLabel("No identifiers")
      .addTypes(PERSON);

    // when
    var result = authoritySearchMessageMapper.toIndex(resource, CREATE);

    // then
    var authority = (LinkedDataAuthority) result.getNew();
    assertThat(authority.getIdentifiers()).isEqualTo(emptyList());
  }

  private Resource buildPersonAuthorityWithIdentifier() {
    var resource = new Resource()
      .setIdAndRefreshEdges(randomLong())
      .setLabel("Test Person")
      .addTypes(PERSON);

    var lccnIdentifier = new Resource()
      .setIdAndRefreshEdges(randomLong())
      .addTypes(IDENTIFIER)
      .addTypes(org.folio.ld.dictionary.ResourceTypeDictionary.ID_LCCN)
      .setDoc(getJsonNode(Map.of(NAME.getValue(), List.of("n2024001234"))));

    resource.addOutgoingEdge(new ResourceEdge(resource, lccnIdentifier, MAP));
    return resource;
  }
}
