package org.folio.linked.data.service.lccn.normalization;

import static com.github.jknack.handlebars.internal.lang3.StringUtils.SPACE;

import java.util.regex.Pattern;

public abstract class AbstractLccnNormalizer implements LccnNormalizer {

  private static final Pattern DIGIT_PATTERN = Pattern.compile("\\d");

  protected boolean isProcessable(StringBuilder builder) {
    return builder.length() < 12;
  }

  protected void addSpaces(StringBuilder builder) {
    while (isProcessable(builder)) {
      var matcher = DIGIT_PATTERN.matcher(builder);
      if (matcher.find()) {
        builder.insert(matcher.start(), SPACE);
      }
    }
  }
}
