package org.folio.linked.data.util;

import static java.util.Objects.isNull;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;

import java.util.List;
import java.util.function.Supplier;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;

@UtilityClass
public class BibframeUtils {

  public static String getFirstValue(Supplier<List<String>> valuesSupplier) {
    if (isNull(valuesSupplier)) {
      return "";
    }
    var values = valuesSupplier.get();
    if (isNotEmpty(values)) {
      return values.stream()
        .filter(StringUtils::isNotBlank)
        .findFirst()
        .orElse("");
    }
    return "";
  }
}
