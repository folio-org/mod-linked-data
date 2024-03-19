package org.folio.linked.data.configuration.json.serialization;

import static org.mockito.Mockito.verify;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import java.io.IOException;
import org.folio.spring.test.type.UnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@UnitTest
@ExtendWith(MockitoExtension.class)
class RawValueJsonSerializerTest {

  RawValueJsonSerializer rawValueJsonSerializer = new RawValueJsonSerializer();

  @Mock
  JsonGenerator gen;
  @Mock
  SerializerProvider serializers;

  @Test
  void serializeNull_shouldWriteNull() throws IOException {
    //when
    rawValueJsonSerializer.serialize(null, gen, serializers);

    //then
    verify(gen)
      .writeNull();
  }

  @ParameterizedTest
  @ValueSource(strings = {"", " ", "some string"})
  void serializeNonNull_shouldWriteValue(String value) throws IOException {
    //when
    rawValueJsonSerializer.serialize(value, gen, serializers);

    //then
    verify(gen)
      .writeRawValue(value);
  }
}
