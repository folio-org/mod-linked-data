package org.folio.linked.data.preprocessing.lccn.impl;

import static com.github.jknack.handlebars.internal.lang3.StringUtils.SPACE;

import java.util.regex.Pattern;
import org.folio.linked.data.preprocessing.lccn.LccnNormalizer;

public abstract class AbstractSpaceAdder implements LccnNormalizer<String> {

  private static final Pattern DIGIT_PATTERN = Pattern.compile("\\d");

  protected abstract Pattern getPattern();

  protected abstract String handle(String lccn);

  protected boolean isProcessable(String lccn) {
    return lccn.length() < 12;
  }

  protected boolean isApplicable(String lccn) {
    return getPattern().matcher(lccn)
      .find();
  }

  protected void addSpaces(StringBuilder builder) {
    while (isProcessable(builder.toString())) {
      var matcher = DIGIT_PATTERN.matcher(builder);
      if (matcher.find()) {
        builder.insert(matcher.start(), SPACE);
      }
    }
  }

  @Override
  public String normalize(String lccn) {
    if (isProcessable(lccn) && isApplicable(lccn)) {
      return handle(lccn);
    }
    return lccn;
  }
}
