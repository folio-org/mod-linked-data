package org.folio.linked.data.util;

import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import java.util.List;
import java.util.function.Supplier;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;

@UtilityClass
public class BibframeUtils {

  public static String getLabelOrFirstValue(String label, Supplier<List<String>> valuesSupplier) {
    if (isNotBlank(label)) {
      return label;
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
