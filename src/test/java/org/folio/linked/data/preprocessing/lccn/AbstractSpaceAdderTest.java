package org.folio.linked.data.preprocessing.lccn;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.util.regex.Pattern;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@UnitTest
class AbstractSpaceAdderTest {

  private final AbstractSpaceAdder spaceAdder = spy(new TestSpaceAdder());

  @ParameterizedTest
  @ValueSource(strings = {
    "0123456789",
    " 0123456789",
    "12345678",
    "12345678 ",
  })
  void isProcessable_shouldReturn_true(String input) {
    // expect
    assertTrue(spaceAdder.isProcessable(input));
  }

  @ParameterizedTest
  @ValueSource(strings = {
    "ab0123456789",
    "ab0123456789c",
  })
  void isProcessable_shouldReturn_false(String input) {
    // expect
    assertFalse(spaceAdder.isProcessable(input));
  }

  @ParameterizedTest
  @ValueSource(strings = {
    "   12345678 ",
    "    12345678 ",
    "0123456789",
    "1a345678",
  })
  void apply_shouldNot_invokeHandle(String input) {
    // when
    spaceAdder.apply(input);

    // then
    verify(spaceAdder, never()).handle(input);
  }

  @ParameterizedTest
  @ValueSource(strings = {
    "12345678",
    "12345678 ",
    " 12345678",
    "  12345678",
  })
  void apply_should_invokeHandle(String input) {
    // when
    spaceAdder.apply(input);

    // then
    verify(spaceAdder).handle(input);
  }

  private static final class TestSpaceAdder extends AbstractSpaceAdder {

    @Override
    protected Pattern getPattern() {
      return Pattern.compile("(?<!\\d)\\d{8}(?!\\d)");
    }

    @Override
    protected String handle(String lccn) {
      return "";
    }
  }
}
