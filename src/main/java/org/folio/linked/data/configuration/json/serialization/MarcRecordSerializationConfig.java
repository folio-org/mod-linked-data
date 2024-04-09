package org.folio.linked.data.configuration.json.serialization;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

public interface MarcRecordSerializationConfig {

  @JsonSerialize(using = RawValueJsonSerializer.class)
  String getContent();
}
