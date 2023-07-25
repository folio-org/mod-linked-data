package org.folio.linked.data.service;

import static java.util.Objects.nonNull;
import static org.folio.linked.data.util.Constants.INDEX_NAME;
import static org.folio.linked.data.util.Constants.SEARCH_PROFILE;
import static org.folio.spring.tools.config.properties.FolioEnvironment.getFolioEnvName;

import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.folio.linked.data.domain.dto.BibframeRequest;
import org.folio.linked.data.domain.dto.Instance;
import org.folio.linked.data.domain.dto.IsbnField;
import org.folio.search.domain.dto.ContributorSearch;
import org.folio.search.domain.dto.Identifiers;
import org.folio.search.domain.dto.InstanceSearch;
import org.folio.search.domain.dto.ResourceEvent;
import org.folio.search.domain.dto.ResourceEventType;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

@Log4j2
@Service
@Profile(SEARCH_PROFILE)
@RequiredArgsConstructor
public class KafkaSenderFolio implements KafkaSender {

  private static final String INITIAL_TOPIC_NAME = "inventory.instance";
  private final KafkaTemplate<String, ResourceEvent> kafkaTemplate;

  @SneakyThrows
  @Override
  public void sendResourceCreated(String tenant, BibframeRequest request, Long id) {
    CompletableFuture<SendResult<String, ResourceEvent>> future = kafkaTemplate.send(getTenantTopicName(tenant),
      new ResourceEvent()
        .id(id.toString())
        .type(ResourceEventType.CREATE)
        .tenant(tenant)
        .resourceName(INDEX_NAME)
        ._new(mapToSearch(request, id, tenant))
    );
    log.info("sendResourceCreated result: [{}]", future.get().toString());
  }

  private InstanceSearch mapToSearch(BibframeRequest request, Long id, String tenant) {
    InstanceSearch instanceSearch = new InstanceSearch();
    instanceSearch.setId(id.toString());
    instanceSearch.setTenantId(tenant);
    Instance instance = (Instance) request.getInstance().get(0);
    if (nonNull(instance.getIdentifiedBy())) {
      instance.getIdentifiedBy()
        .stream()
        .filter(IsbnField.class::isInstance)
        .map(IsbnField.class::cast)
        .forEach(isbn -> instanceSearch.addIdentifiersItem(
          new Identifiers()
            .value(isbn.getIsbn().getValue().get(0))
            .identifierTypeId("ISBN")
        ));
    }
    instanceSearch.setTitle("title");
    return instanceSearch;
  }

  private String getTenantTopicName(String tenantId) {
    return String.format("%s.%s.%s", getFolioEnvName(), tenantId, INITIAL_TOPIC_NAME);
  }
}
