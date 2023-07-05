package org.folio.linked.data.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.hash.Hashing;
import java.nio.charset.StandardCharsets;
import lombok.experimental.UtilityClass;

@UtilityClass
public class HashUtil {

  public static long hash(JsonNode node) {
    return Hashing.murmur3_32_fixed().hashString(node.toString(), StandardCharsets.UTF_8).padToLong();
  }

}
