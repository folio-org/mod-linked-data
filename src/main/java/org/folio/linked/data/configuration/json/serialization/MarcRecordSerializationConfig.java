package org.folio.linked.data.configuration.json.serialization;

import tools.jackson.databind.annotation.JsonSerialize;

public interface MarcRecordSerializationConfig {

  @JsonSerialize(using = RawValueJsonSerializer.class)
  String getContent();
}
