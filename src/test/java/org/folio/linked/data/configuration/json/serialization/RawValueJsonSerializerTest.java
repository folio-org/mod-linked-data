package org.folio.linked.data.configuration.json.serialization;

import static org.mockito.Mockito.verify;

import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;

@UnitTest
@ExtendWith(MockitoExtension.class)
class RawValueJsonSerializerTest {

  RawValueJsonSerializer rawValueJsonSerializer = new RawValueJsonSerializer();

  @Mock
  JsonGenerator gen;
  @Mock
  SerializationContext ctx;

  @Test
  void serializeNull_shouldWriteNull() {
    //when
    rawValueJsonSerializer.serialize(null, gen, ctx);

    //then
    verify(gen)
      .writeNull();
  }

  @ParameterizedTest
  @ValueSource(strings = {"", " ", "some string"})
  void serializeNonNull_shouldWriteValue(String value) {
    //when
    rawValueJsonSerializer.serialize(value, gen, ctx);

    //then
    verify(gen)
      .writeRawValue(value);
  }
}
