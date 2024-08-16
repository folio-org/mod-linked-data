package org.folio.linked.data.util;

import static java.util.Optional.ofNullable;

import java.util.Optional;
import lombok.experimental.UtilityClass;
import org.apache.kafka.clients.consumer.ConsumerRecord;

@UtilityClass
public class KafkaUtils {

  public static Optional<String> getHeaderValueByName(ConsumerRecord<String, ?> consumerRecord, String headerName) {
    return ofNullable(consumerRecord.headers().lastHeader(headerName))
      .map(header -> new String(header.value()));
  }
}
