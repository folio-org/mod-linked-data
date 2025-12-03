package org.folio.linked.data.util;

import static java.util.stream.Collectors.joining;

import java.util.Collection;
import java.util.Objects;
import lombok.experimental.UtilityClass;

@UtilityClass
public class SearchQueryUtils {

  public static final String AND = " and ";
  public static final String OR = " or ";
  private static final String LCCN_EQUALS = "lccn==\"%s\"";
  private static final String EXCLUDE_SUPPRESSED = "staffSuppress <> \"true\" and discoverySuppress <> \"true\"";
  private static final String ID_NOT_EQUALS = "id <> \"%s\"";
  private static final String AUTHORIZED = "authRefType==(\"Authorized\")";

  public static String queryLccnsExcludingSuppressed(Collection<String> lccns) {
    return "(" + queryLccns(lccns) + ")" + AND + "(" + EXCLUDE_SUPPRESSED + ")";
  }

  public static String queryLccnsAuthorized(Collection<String> lccns) {
    return "(" + queryLccns(lccns) + ")" + AND + "(" + AUTHORIZED + ")";
  }

  public static String queryLccns(Collection<String> lccns) {
    return lccns.stream()
      .filter(Objects::nonNull)
      .map(LCCN_EQUALS::formatted)
      .collect(joining(OR));
  }

  public static String queryIdNotEquals(String id) {
    return ID_NOT_EQUALS.formatted(id);
  }

}
