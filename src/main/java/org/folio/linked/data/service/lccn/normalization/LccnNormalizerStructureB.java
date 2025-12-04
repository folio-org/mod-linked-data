package org.folio.linked.data.service.lccn.normalization;

import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

@Component
public class LccnNormalizerStructureB extends AbstractLccnNormalizer {

  private static final Pattern PATTERN = Pattern.compile("(?<!\\d)\\d{10}(?!\\d)");

  @Override
  public boolean test(String lccn) {
    return PATTERN.matcher(lccn)
      .find();
  }

  @Override
  public String apply(String lccn) {
    var builder = new StringBuilder(lccn);
    if (isProcessable(builder)) {
      addSpaces(builder);
    }
    return builder.toString();
  }
}
