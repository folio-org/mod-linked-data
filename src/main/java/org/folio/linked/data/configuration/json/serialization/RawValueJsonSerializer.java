package org.folio.linked.data.configuration.json.serialization;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import java.io.IOException;
import java.util.Objects;

public class RawValueJsonSerializer extends JsonSerializer<String> {

  @Override
  public void serialize(String value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
    if (Objects.nonNull(value)) {
      gen.writeRawValue(value);
    } else {
      gen.writeNull();
    }
  }
}
