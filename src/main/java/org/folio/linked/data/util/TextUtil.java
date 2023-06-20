package org.folio.linked.data.util;

import com.google.common.hash.Hashing;
import java.nio.charset.StandardCharsets;
import lombok.experimental.UtilityClass;

@UtilityClass
public class TextUtil {

  public static long hash(String str) {
    return Hashing.murmur3_32_fixed().hashString(str, StandardCharsets.UTF_8).padToLong();
  }
}
