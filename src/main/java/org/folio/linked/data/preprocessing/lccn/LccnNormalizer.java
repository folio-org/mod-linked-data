package org.folio.linked.data.preprocessing.lccn;

@FunctionalInterface
public interface LccnNormalizer<T> {

  T normalize(T t);

  default LccnNormalizer<T> andThen(LccnNormalizer<T> after) {
    return t -> after.normalize(normalize(t));
  }

  static <T> LccnNormalizer<T> identity() {
    return t -> t;
  }
}
