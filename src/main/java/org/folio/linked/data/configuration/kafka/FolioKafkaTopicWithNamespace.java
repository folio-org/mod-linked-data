package org.folio.linked.data.configuration.kafka;

import static org.apache.commons.lang3.StringUtils.isAnyBlank;
import static org.folio.kafka.KafkaTopicNameHelper.getDefaultNameSpace;
import static org.folio.spring.tools.kafka.KafkaUtils.getTenantTopicNameWithNamespace;

import org.folio.spring.tools.kafka.FolioKafkaTopic;

public interface FolioKafkaTopicWithNamespace extends FolioKafkaTopic {

  @Override
  default String fullTopicName(String tenantId) {
    var envId = this.envId();
    var topicName = this.topicName();
    if (isAnyBlank(envId, tenantId, topicName)) {
      throw new IllegalArgumentException("envId, tenantId, topicName can't be blank");
    } else {
      return getTenantTopicNameWithNamespace(topicName, envId, tenantId, getDefaultNameSpace());
    }
  }
}
