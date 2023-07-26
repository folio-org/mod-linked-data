package org.folio.linked.data.configuration.json;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.folio.linked.data.configuration.json.deserialization.instance.ContributionAgentDeserializer;
import org.folio.linked.data.configuration.json.deserialization.instance.IdentifiedByDeserializer;
import org.folio.linked.data.configuration.json.deserialization.instance.ProvisionActivityDeserializer;
import org.folio.linked.data.configuration.json.deserialization.instance.TitleDeserializer;
import org.folio.linked.data.domain.dto.AccessPolicyField;
import org.folio.linked.data.domain.dto.BibframeInstanceInner;
import org.folio.linked.data.domain.dto.BibframeItemInner;
import org.folio.linked.data.domain.dto.BibframeWorkInner;
import org.folio.linked.data.domain.dto.ClassificationLccField;
import org.folio.linked.data.domain.dto.ContributionAgentInner;
import org.folio.linked.data.domain.dto.Instance;
import org.folio.linked.data.domain.dto.InstanceIdentifiedByInner;
import org.folio.linked.data.domain.dto.InstanceProvisionActivityInner;
import org.folio.linked.data.domain.dto.InstanceTitleInner;
import org.folio.linked.data.domain.dto.Item;
import org.folio.linked.data.domain.dto.ItemUsageAndAccessPolicyInner;
import org.folio.linked.data.domain.dto.PlaceField;
import org.folio.linked.data.domain.dto.PrimaryContributionField;
import org.folio.linked.data.domain.dto.RelatedWorkField;
import org.folio.linked.data.domain.dto.Work;
import org.folio.linked.data.domain.dto.WorkClassificationInner;
import org.folio.linked.data.domain.dto.WorkContributionInner;
import org.folio.linked.data.domain.dto.WorkRelationshipInner;
import org.folio.linked.data.domain.dto.WorkSubjectInner;
import org.folio.linked.data.domain.dto.WorkTitleField;
import org.folio.linked.data.domain.dto.WorkTitleInner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ObjectMapperConfig {

  @Bean
  public ObjectMapper objectMapper() {
    return new ObjectMapper()
      .setSerializationInclusion(JsonInclude.Include.NON_NULL)
      .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
      .configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true)
      .registerModule(monographModule());
  }

  private Module monographModule() {
    var module = new SimpleModule();
    module.addAbstractTypeMapping(BibframeWorkInner.class, Work.class);
    module.addAbstractTypeMapping(BibframeInstanceInner.class, Instance.class);
    module.addAbstractTypeMapping(BibframeItemInner.class, Item.class);
    module.addDeserializer(InstanceTitleInner.class, new TitleDeserializer());
    module.addDeserializer(InstanceIdentifiedByInner.class, new IdentifiedByDeserializer());
    module.addDeserializer(InstanceProvisionActivityInner.class, new ProvisionActivityDeserializer());
    module.addDeserializer(ContributionAgentInner.class, new ContributionAgentDeserializer());
    module.addAbstractTypeMapping(WorkContributionInner.class, PrimaryContributionField.class);
    module.addAbstractTypeMapping(WorkClassificationInner.class, ClassificationLccField.class);
    module.addAbstractTypeMapping(WorkSubjectInner.class, PlaceField.class);
    module.addAbstractTypeMapping(WorkRelationshipInner.class, RelatedWorkField.class);
    module.addAbstractTypeMapping(WorkTitleInner.class, WorkTitleField.class);
    module.addAbstractTypeMapping(ItemUsageAndAccessPolicyInner.class, AccessPolicyField.class);
    return module;
  }

}
