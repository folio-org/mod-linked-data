package org.folio.linked.data.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.UUID;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.folio.spring.testing.type.UnitTest;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

@UnitTest
public class KafkaUtilsTest {

  @Test
  public void getHeaderValueByName_shouldReturnEmptyOptional_ifMessageContainsNoExpectedHeader() {
    // given
    var headerKey = "headerKey";
    var consumerRecord = new ConsumerRecord<>("topic", 1, 1, "key", "value");

    // when
    var result = KafkaUtils.getHeaderValueByName(consumerRecord, headerKey);

    // then
    assertThat(result).isNotPresent();
  }

  @Test
  public void getHeaderValueByName_shouldReturnOptionalWithHeader_ifMessageContainsExpectedHeader() {
    // given
    var headerKey = "headerKey";
    var headerValue = UUID.randomUUID().toString();
    var consumerRecord = new ConsumerRecord<>("topic", 1, 1, "key", "value");
    var headers = new RecordHeaders(List.of(new RecordHeader(headerKey, headerValue.getBytes())));
    ReflectionTestUtils.setField(consumerRecord, "headers", headers);

    // when
    var result = KafkaUtils.getHeaderValueByName(consumerRecord, headerKey);

    // then
    assertThat(result)
      .isPresent()
      .contains(headerValue);
  }
}
