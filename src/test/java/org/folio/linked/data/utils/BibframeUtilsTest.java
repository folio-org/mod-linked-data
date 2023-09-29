package org.folio.linked.data.utils;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;
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
    assertThat(result).isEqualTo("");
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
    assertThat(result).isEqualTo("");
  }

}
