package org.folio.linked.data.mapper.kafka.search;

import static org.assertj.core.api.Assertions.assertThat;

import org.folio.ld.dictionary.ResourceTypeDictionary;
import org.folio.linked.data.mapper.kafka.identifier.IndexIdentifierMapper;
import org.folio.linked.data.model.entity.Resource;
import org.folio.search.domain.dto.BibframeAuthorityIdentifiersInner;
import org.folio.search.domain.dto.LinkedDataAuthority;
import org.folio.search.domain.dto.ResourceIndexEventType;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@UnitTest
@ExtendWith(MockitoExtension.class)
class AuthoritySearchMessageMapperTest {

  @InjectMocks
  private AuthoritySearchMessageMapper mapper;

  @Mock
  private IndexIdentifierMapper<BibframeAuthorityIdentifiersInner> innerIndexIdentifierMapper;

  @ParameterizedTest
  @EnumSource(ResourceTypeDictionary.class)
  void whenContainsTypeAndConvertedByBibliographicName(ResourceTypeDictionary type) {
    //given
    var resource = new Resource().addTypes(type);
    var expectedType = type + "";

    //when
    var result = mapper.toIndex(resource, ResourceIndexEventType.CREATE);

    //then
    assertThat(result)
      .isPresent()
      .map(LinkedDataAuthority::getType)
      .contains(expectedType);
  }
}
