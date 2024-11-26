package org.folio.linked.data.util;

import static org.apache.commons.collections4.CollectionUtils.isEmpty;

import lombok.experimental.UtilityClass;
import org.folio.linked.data.domain.dto.LccnRequest;

@UtilityClass
public class LccnUtils {

  public static boolean isCurrent(LccnRequest lccnRequest) {
    return isEmpty(lccnRequest.getStatus()) || lccnRequest.getStatus()
      .stream()
      .flatMap(status -> status.getLink().stream())
      .anyMatch(link -> link.endsWith("current"));
  }
}
