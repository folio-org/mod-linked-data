package org.folio.linked.data.configuration.json.serialization;

import java.util.Objects;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueSerializer;

public class RawValueJsonSerializer extends ValueSerializer<String> {

  @Override
  public void serialize(String value, JsonGenerator gen, SerializationContext ctx) {
    if (Objects.nonNull(value)) {
      gen.writeRawValue(value);
    } else {
      gen.writeNull();
    }
  }
}
