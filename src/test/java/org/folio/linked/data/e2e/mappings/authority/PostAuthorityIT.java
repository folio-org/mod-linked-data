package org.folio.linked.data.e2e.mappings.authority;

import static org.folio.linked.data.domain.dto.ResourceIndexEventType.CREATE;
import static org.folio.linked.data.test.TestUtil.awaitAndAssert;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.folio.linked.data.domain.dto.ResourceIndexEventType;
import org.folio.linked.data.e2e.mappings.PostResourceIT;
import org.folio.linked.data.test.kafka.KafkaSearchAuthorityIndexTopicListener;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles(profiles = "test", inheritProfiles = false)
abstract class PostAuthorityIT extends PostResourceIT {

  @Autowired
  private KafkaSearchAuthorityIndexTopicListener authorityIndexTopicListener;

  @BeforeEach
  void clearAuthorityKafkaMessages() {
    authorityIndexTopicListener.getMessages().clear();
  }

  @Override
  protected void validateKafkaMessages(String id) {
    checkAuthorityIndexMessage(Long.parseLong(id), CREATE);
  }

  private void checkAuthorityIndexMessage(Long id, ResourceIndexEventType eventType) {
    awaitAndAssert(() ->
      assertTrue(authorityIndexTopicListener.getMessages().stream()
        .anyMatch(m -> m.contains(id.toString()) && m.contains(eventType.getValue())))
    );
  }
}
