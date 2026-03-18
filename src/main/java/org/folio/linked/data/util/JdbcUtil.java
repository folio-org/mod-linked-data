package org.folio.linked.data.util;

import static java.util.stream.Collectors.joining;

import java.util.Set;
import lombok.experimental.UtilityClass;

@UtilityClass
public class JdbcUtil {

  public static final String COL_RESOURCE_HASH = "resource_hash";

  public static String toSqlLiterals(Set<Long> hashes) {
    return hashes.stream()
      .map(String::valueOf)
      .collect(joining(", "));
  }
}
