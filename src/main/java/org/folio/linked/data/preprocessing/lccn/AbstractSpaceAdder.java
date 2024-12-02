package org.folio.linked.data.preprocessing.lccn;

import java.util.function.UnaryOperator;
import java.util.regex.Pattern;

public abstract class AbstractSpaceAdder implements UnaryOperator<String> {

  protected abstract Pattern getPattern();

  protected abstract String handle(String lccn);

  protected boolean isProcessable(String lccn) {
    return lccn.length() < 12;
  }

  protected boolean isApplicable(String lccn) {
    return getPattern().matcher(lccn)
      .find();
  }

  @Override
  public String apply(String lccn) {
    if (isProcessable(lccn) && isApplicable(lccn)) {
      return handle(lccn);
    }
    return lccn;
  }
}
