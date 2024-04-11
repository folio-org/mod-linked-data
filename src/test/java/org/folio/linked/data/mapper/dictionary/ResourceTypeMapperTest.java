package org.folio.linked.data.mapper.dictionary;

import static org.assertj.core.api.Assertions.assertThat;

import org.folio.ld.dictionary.ResourceTypeDictionary;
import org.folio.linked.data.model.entity.ResourceTypeEntity;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mapstruct.factory.Mappers;

@UnitTest
class ResourceTypeMapperTest {

  private final ResourceTypeMapper mapper = Mappers.getMapper(ResourceTypeMapper.class);

  @ParameterizedTest
  @EnumSource(value = ResourceTypeDictionary.class)
  void shouldConvertDictionary(ResourceTypeDictionary dictionary) {
    //given
    var expected = new ResourceTypeEntity(dictionary.getHash(), dictionary.getUri(), null);

    //when
    var actual = mapper.toEntity(dictionary);

    //then
    assertThat(actual)
      .usingRecursiveComparison()
      .isEqualTo(expected);
  }
}
