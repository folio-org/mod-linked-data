package org.folio.linked.data.util;

import static org.apache.commons.collections4.CollectionUtils.isEmpty;
import static org.folio.linked.data.util.JsonUtils.hasElementByJsonPath;

import lombok.experimental.UtilityClass;
import org.folio.linked.data.domain.dto.LccnRequest;

@UtilityClass
public class LccnUtils {

  private static final String LCCN_JSON_PATH = "$.fields[*].010.subfields[*].a";

  public static boolean isCurrent(LccnRequest lccnRequest) {
    return isEmpty(lccnRequest.getStatus()) || lccnRequest.getStatus()
      .stream()
      .flatMap(status -> status.getLink().stream())
      .anyMatch(link -> link.endsWith("current"));
  }

  public static boolean hasLccn(String marcJson) {
    return hasElementByJsonPath(marcJson, LCCN_JSON_PATH);
  }
}
