package org.folio.linked.data.util;

import static java.util.Optional.ofNullable;

import java.util.Optional;
import lombok.experimental.UtilityClass;
import org.apache.kafka.clients.consumer.ConsumerRecord;

@UtilityClass
public class KafkaUtils {

  public static Optional<String> getHeaderValueByName(ConsumerRecord<String, ?> record, String headerName) {
    return ofNullable(record.headers().lastHeader(headerName))
      .map(header -> new String(header.value()));
  }
}
