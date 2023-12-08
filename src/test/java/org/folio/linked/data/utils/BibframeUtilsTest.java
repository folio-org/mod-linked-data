package org.folio.linked.data.utils;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.folio.linked.data.util.BibframeUtils;
import org.folio.spring.test.type.UnitTest;
import org.junit.jupiter.api.Test;

@UnitTest
class BibframeUtilsTest {

  @Test
  void getFirstValue_shouldReturnEmptyString_ifGivenSupplierIsNull() {
    // given

    // when
    var result = BibframeUtils.getFirstValue(null);

    // then
    assertThat(result).isEmpty();
  }

  @Test
  void getFirstValue_shouldReturnFirstValue() {
    // given
    var first = UUID.randomUUID().toString();
    var second = UUID.randomUUID().toString();
    var valuesSupplier = new Supplier<List<String>>() {
      @Override
      public List<String> get() {
        return Lists.newArrayList(first, second);
      }
    };

    // when
    var result = BibframeUtils.getFirstValue(valuesSupplier);

    // then
    assertThat(result).isEqualTo(first);
  }

  @Test
  void getFirstValue_shouldReturnSecondValue_ifFirstValueIsEmpty() {
    // given
    var first = "";
    var second = UUID.randomUUID().toString();
    var valuesSupplier = new Supplier<List<String>>() {
      @Override
      public List<String> get() {
        return Lists.newArrayList(first, second);
      }
    };

    // when
    var result = BibframeUtils.getFirstValue(valuesSupplier);

    // then
    assertThat(result).isEqualTo(second);
  }

  @Test
  void getFirstValue_shouldReturnEmptyString_ifAllValuesAreNull() {
    // given
    String first = null;
    String second = null;
    var valuesSupplier = new Supplier<List<String>>() {
      @Override
      public List<String> get() {
        return Lists.newArrayList(first, second);
      }
    };

    // when
    var result = BibframeUtils.getFirstValue(valuesSupplier);

    // then
    assertThat(result).isEmpty();
  }

  @Test
  void cleanDate_shouldCleanAnyUnexpectedSymbolAndReturnDateIfParsed() {
    // given
    var given = List.of(
      "2021-11-08T13:00:00.000+00:00",
      "2022-12-22T13:00:00",
      "2020-01-01",
      "[2010]",
      "sdvsdvindfinvdfnbv±!@#$%^&*()_=2000",
      "2023",
      "abc",
      "25",
      "200"
    );

    // when
    var result = given.stream().map(BibframeUtils::cleanDate).collect(Collectors.toList());

    // then
    assertThat(result).containsExactly(
      "2021-11-08T13:00:00.000+00:00",
      "2022-12-22T13:00:00",
      "2020-01-01",
      "2010",
      "2000",
      "2023",
      null,
      null,
      null
    );
  }

}
