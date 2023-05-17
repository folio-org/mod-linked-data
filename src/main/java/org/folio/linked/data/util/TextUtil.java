package org.folio.linked.data.util;

import static java.util.Objects.isNull;

import lombok.experimental.UtilityClass;

@UtilityClass
public class TextUtil {

  private static final String PATTERN_FOR_SLUGIFY = "[^a-z\\d_]+";
  private static final String SLUGIFY_REPLACEMENT = "_";
  private static final int SLUG_MAX_LENGTH = 53;

  public static String slugify(String value) {
    return isNull(value) ? null : value.toLowerCase()
      .replaceAll(PATTERN_FOR_SLUGIFY, SLUGIFY_REPLACEMENT)
      .trim()
      .substring(0, Math.min(value.length(), SLUG_MAX_LENGTH));
  }
}
