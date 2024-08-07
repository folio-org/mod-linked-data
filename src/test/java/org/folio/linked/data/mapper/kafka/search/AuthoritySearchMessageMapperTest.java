package org.folio.linked.data.mapper.kafka.search;

import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.ld.dictionary.PredicateDictionary.MAP;
import static org.folio.ld.dictionary.ResourceTypeDictionary.CONCEPT;
import static org.folio.ld.dictionary.ResourceTypeDictionary.ID_LCCN;
import static org.folio.linked.data.test.TestUtil.randomLong;
import static org.mockito.Mockito.doReturn;

import java.util.List;
import org.folio.linked.data.mapper.kafka.search.identifier.IndexIdentifierMapper;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceEdge;
import org.folio.search.domain.dto.LinkedDataAuthorityIdentifiersInner;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@UnitTest
@ExtendWith(MockitoExtension.class)
class AuthoritySearchMessageMapperTest {

  @InjectMocks
  private AuthoritySearchMessageMapperImpl mapper;

  @Mock
  private IndexIdentifierMapper<LinkedDataAuthorityIdentifiersInner> innerIndexIdentifierMapper;

  @Test
  void toIndex_shouldMapResourceCorrectly() {
    //given
    var resource = new Resource()
      .setId(randomLong())
      .addTypes(CONCEPT)
      .setLabel("label");
    var id = new Resource()
      .setId(randomLong())
      .addTypes(ID_LCCN);
    resource
      .addOutgoingEdge(new ResourceEdge(resource, id, MAP));
    var expectedIdentifiers = List.of(new LinkedDataAuthorityIdentifiersInner()
      .type(LinkedDataAuthorityIdentifiersInner.TypeEnum.LCCN)
      .value(randomLong().toString())
    );
    doReturn(expectedIdentifiers).when(innerIndexIdentifierMapper).extractIdentifiers(resource);

    //when
    var result = mapper.toIndex(resource);

    //then
    assertThat(result)
      .hasAllNullFieldsOrPropertiesExcept("id", "resourceName", "_new")
      .hasFieldOrProperty("id")
      .hasFieldOrPropertyWithValue("resourceName", "linked-data-authority")
      .extracting("_new")
      .hasFieldOrPropertyWithValue("id", String.valueOf(resource.getId()))
      .hasFieldOrPropertyWithValue("label", resource.getLabel())
      .hasFieldOrPropertyWithValue("type", "CONCEPT")
      .hasFieldOrPropertyWithValue("identifiers", expectedIdentifiers);
  }
}
