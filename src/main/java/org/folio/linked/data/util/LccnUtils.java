package org.folio.linked.data.util;

import static org.apache.commons.collections4.CollectionUtils.isEmpty;

import lombok.experimental.UtilityClass;
import org.folio.linked.data.domain.dto.IdentifierRequest;

@UtilityClass
public class LccnUtils {
  public static boolean isCurrent(IdentifierRequest lccnRequest) {
    return isEmpty(lccnRequest.getStatus()) || lccnRequest.getStatus()
      .stream()
      .flatMap(status -> status.getLink().stream())
      .anyMatch(link -> link.endsWith("current"));
  }
}
