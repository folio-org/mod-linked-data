package org.folio.linked.data.configuration.json.serialization;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

public abstract class MarcRecordSerializationConfig {

  @JsonSerialize(using = RawValueJsonSerializer.class)
  public abstract String getContent();
}
