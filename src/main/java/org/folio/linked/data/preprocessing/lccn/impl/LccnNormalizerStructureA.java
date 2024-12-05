package org.folio.linked.data.preprocessing.lccn.impl;

import static com.github.jknack.handlebars.internal.lang3.StringUtils.SPACE;

import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

@Component
public class LccnNormalizerStructureA extends AbstractLccnNormalizer {

  private static final Pattern PATTERN = Pattern.compile("(?<!\\d)\\d{8}(?!\\d)");

  @Override
  public boolean test(String lccn) {
    return PATTERN.matcher(lccn)
      .find();
  }

  @Override
  public String apply(String lccn) {
    var builder = new StringBuilder(lccn);
    if (isProcessable(builder)) {
      if (noTrailingSpace(builder)) {
        builder.append(SPACE);
      }
      addSpaces(builder);
    }
    return builder.toString();
  }

  private boolean noTrailingSpace(StringBuilder builder) {
    return !builder.toString().endsWith(SPACE);
  }
}
