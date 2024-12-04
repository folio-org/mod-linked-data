package org.folio.linked.data.preprocessing.lccn.impl;

import static com.github.jknack.handlebars.internal.lang3.StringUtils.SPACE;

import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

@Component
public class SpaceAdderStructureA extends AbstractSpaceAdder {

  private static final Pattern PATTERN = Pattern.compile("(?<!\\d)\\d{8}(?!\\d)");

  @Override
  protected Pattern getPattern() {
    return PATTERN;
  }

  @Override
  protected String handle(String lccn) {
    var builder = new StringBuilder(lccn);
    if (noTrailingSpace(builder)) {
      builder.append(SPACE);
    }
    addSpaces(builder);
    return builder.toString();
  }

  private boolean noTrailingSpace(StringBuilder builder) {
    return !builder.toString().endsWith(SPACE);
  }
}
