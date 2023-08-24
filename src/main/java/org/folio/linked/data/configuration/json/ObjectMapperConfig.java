package org.folio.linked.data.configuration.json;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.folio.linked.data.configuration.json.deserialization.instance.ContributionAgent2Deserializer;
import org.folio.linked.data.configuration.json.deserialization.instance.IdentifiedBy2Deserializer;
import org.folio.linked.data.configuration.json.deserialization.instance.ProvisionActivity2Deserializer;
import org.folio.linked.data.configuration.json.deserialization.instance.Title2Deserializer;
import org.folio.linked.data.domain.dto.AccessPolicyField2;
import org.folio.linked.data.domain.dto.Bibframe2InstanceInner;
import org.folio.linked.data.domain.dto.Bibframe2ItemInner;
import org.folio.linked.data.domain.dto.Bibframe2WorkInner;
import org.folio.linked.data.domain.dto.ClassificationLccField2;
import org.folio.linked.data.domain.dto.Contribution2AgentInner;
import org.folio.linked.data.domain.dto.Instance2;
import org.folio.linked.data.domain.dto.Instance2IdentifiedByInner;
import org.folio.linked.data.domain.dto.Instance2ProvisionActivityInner;
import org.folio.linked.data.domain.dto.Instance2TitleInner;
import org.folio.linked.data.domain.dto.Item2;
import org.folio.linked.data.domain.dto.Item2UsageAndAccessPolicyInner;
import org.folio.linked.data.domain.dto.PlaceField2;
import org.folio.linked.data.domain.dto.PrimaryContributionField2;
import org.folio.linked.data.domain.dto.RelatedWorkField2;
import org.folio.linked.data.domain.dto.Work2;
import org.folio.linked.data.domain.dto.Work2ClassificationInner;
import org.folio.linked.data.domain.dto.Work2ContributionInner;
import org.folio.linked.data.domain.dto.Work2RelationshipInner;
import org.folio.linked.data.domain.dto.Work2SubjectInner;
import org.folio.linked.data.domain.dto.Work2TitleInner;
import org.folio.linked.data.domain.dto.WorkTitleField2;
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
    module.addAbstractTypeMapping(Bibframe2WorkInner.class, Work2.class);
    module.addAbstractTypeMapping(Bibframe2InstanceInner.class, Instance2.class);
    module.addAbstractTypeMapping(Bibframe2ItemInner.class, Item2.class);
    module.addDeserializer(Instance2TitleInner.class, new Title2Deserializer());
    module.addDeserializer(Instance2IdentifiedByInner.class, new IdentifiedBy2Deserializer());
    module.addDeserializer(Instance2ProvisionActivityInner.class, new ProvisionActivity2Deserializer());
    module.addDeserializer(Contribution2AgentInner.class, new ContributionAgent2Deserializer());
    module.addAbstractTypeMapping(Work2ContributionInner.class, PrimaryContributionField2.class);
    module.addAbstractTypeMapping(Work2ClassificationInner.class, ClassificationLccField2.class);
    module.addAbstractTypeMapping(Work2SubjectInner.class, PlaceField2.class);
    module.addAbstractTypeMapping(Work2RelationshipInner.class, RelatedWorkField2.class);
    module.addAbstractTypeMapping(Work2TitleInner.class, WorkTitleField2.class);
    module.addAbstractTypeMapping(Item2UsageAndAccessPolicyInner.class, AccessPolicyField2.class);
    return module;
  }

}
