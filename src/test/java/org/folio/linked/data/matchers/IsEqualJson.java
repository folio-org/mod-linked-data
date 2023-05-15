package org.folio.linked.data.matchers;

import static org.folio.linked.data.TestUtil.OBJECT_MAPPER;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.json.JSONException;
import org.skyscreamer.jsonassert.JSONCompare;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.skyscreamer.jsonassert.JSONCompareResult;

@RequiredArgsConstructor
public class IsEqualJson extends BaseMatcher<String> {

  private final String expectedJson;
  private final JSONCompareMode jsonCompareMode;

  @Override
  public boolean matches(Object actual) {
    try {
      String json = OBJECT_MAPPER.writeValueAsString(actual);
      JSONCompareResult result = JSONCompare.compareJSON(expectedJson, json, jsonCompareMode);
      return result.passed();
    } catch (JSONException | JsonProcessingException e) {
      throw new IllegalArgumentException(e);
    }
  }

  @Override
  public void describeTo(final Description description) {
    description.appendText(expectedJson);
  }

  public static IsEqualJson equalToJson(final String expectedJson) {
    return new IsEqualJson(expectedJson, JSONCompareMode.STRICT);
  }

}
