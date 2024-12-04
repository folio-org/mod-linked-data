package org.folio.linked.data.preprocessing.lccn.impl;

import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

@Component
public class SpaceAdderStructureB extends AbstractSpaceAdder {

  private static final Pattern PATTERN = Pattern.compile("(?<!\\d)\\d{10}(?!\\d)");

  @Override
  protected Pattern getPattern() {
    return PATTERN;
  }

  @Override
  protected String handle(String lccn) {
    var builder = new StringBuilder(lccn);
    addSpaces(builder);
    return builder.toString();
  }
}
