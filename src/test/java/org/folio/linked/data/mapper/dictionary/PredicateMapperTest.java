package org.folio.linked.data.mapper.dictionary;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import org.folio.ld.dictionary.PredicateDictionary;
import org.folio.linked.data.model.entity.PredicateEntity;
import org.folio.spring.test.type.UnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mapstruct.factory.Mappers;

@UnitTest
class PredicateMapperTest {

  private final PredicateMapper mapper = Mappers.getMapper(PredicateMapper.class);

  @Test
  void whenDictionaryIsNull_shouldThrowException() {
    //expect
    assertThatExceptionOfType(IllegalArgumentException.class)
      .isThrownBy(() -> mapper.toEntity(PredicateDictionary.NULL));
  }

  @ParameterizedTest
  @EnumSource(value = PredicateDictionary.class, names = "NULL", mode = EnumSource.Mode.EXCLUDE)
  void shouldConvertDictionary(PredicateDictionary dictionary) {
    //given
    var expected = new PredicateEntity(dictionary.getHash(), dictionary.getUri());

    //when
    var actual = mapper.toEntity(dictionary);

    //then
    assertThat(actual)
      .usingRecursiveComparison()
      .isEqualTo(expected);
  }
}
