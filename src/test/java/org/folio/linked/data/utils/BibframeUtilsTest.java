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
  void getLabelOrFirstValue_shouldReturnGivenLabel() {
    // given
    var label = UUID.randomUUID().toString();

    // when
    var result = BibframeUtils.getLabelOrFirstValue(label, null);

    // then
    assertThat(result).isEqualTo(label);
  }

  @Test
  void getLabelOrFirstValue_shouldReturnFirstValue_ifLabelIsNull() {
    // given
    String label = null;
    var first = UUID.randomUUID().toString();
    var second = UUID.randomUUID().toString();
    var valuesSupplier = new Supplier<List<String>>() {
      @Override
      public List<String> get() {
        return Lists.newArrayList(first, second);
      }
    };

    // when
    var result = BibframeUtils.getLabelOrFirstValue(label, valuesSupplier);

    // then
    assertThat(result).isEqualTo(first);
  }

  @Test
  void getLabelOrFirstValue_shouldReturnFirstValue_ifLabelIsEmpty() {
    // given
    var label = "";
    var first = UUID.randomUUID().toString();
    var second = UUID.randomUUID().toString();
    var valuesSupplier = new Supplier<List<String>>() {
      @Override
      public List<String> get() {
        return Lists.newArrayList(first, second);
      }
    };

    // when
    var result = BibframeUtils.getLabelOrFirstValue(label, valuesSupplier);

    // then
    assertThat(result).isEqualTo(first);
  }

  @Test
  void getLabelOrFirstValue_shouldReturnSecondValue_ifLabelAndFirstValueAreEmpty() {
    // given
    var label = "";
    var first = "";
    var second = UUID.randomUUID().toString();
    var valuesSupplier = new Supplier<List<String>>() {
      @Override
      public List<String> get() {
        return Lists.newArrayList(first, second);
      }
    };

    // when
    var result = BibframeUtils.getLabelOrFirstValue(label, valuesSupplier);

    // then
    assertThat(result).isEqualTo(second);
  }

  @Test
  void getLabelOrFirstValue_shouldReturnEmptyString_ifLabelAndAllValuesAreNull() {
    // given
    String label = null;
    String first = null;
    String second = null;
    var valuesSupplier = new Supplier<List<String>>() {
      @Override
      public List<String> get() {
        return Lists.newArrayList(first, second);
      }
    };

    // when
    var result = BibframeUtils.getLabelOrFirstValue(label, valuesSupplier);

    // then
    assertThat(result).isEqualTo("");
  }
}
