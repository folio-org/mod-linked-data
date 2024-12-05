package org.folio.linked.data.preprocessing.lccn;

import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

public interface LccnNormalizer extends Predicate<String>, UnaryOperator<String> {

  default Optional<String> normalize(String lccn) {
    if (this.test(lccn)) {
      return Optional.of(this.apply(lccn));
    }
    return Optional.empty();
  }
}
