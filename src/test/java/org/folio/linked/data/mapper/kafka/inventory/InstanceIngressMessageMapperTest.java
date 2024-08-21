package org.folio.linked.data.mapper.kafka.inventory;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.folio.ld.dictionary.ResourceTypeDictionary.INSTANCE;
import static org.folio.linked.data.test.TestUtil.randomLong;
import static org.mockito.Mockito.doReturn;

import java.util.Map;
import java.util.UUID;
import org.folio.linked.data.mapper.ResourceModelMapper;
import org.folio.linked.data.model.entity.FolioMetadata;
import org.folio.linked.data.model.entity.Resource;
import org.folio.marc4ld.service.ld2marc.Bibframe2MarcMapper;
import org.folio.search.domain.dto.InstanceIngressPayload;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@UnitTest
@ExtendWith(MockitoExtension.class)
class InstanceIngressMessageMapperTest {

  @InjectMocks
  private InstanceIngressMessageMapperImpl kafkaInventoryMessageMapper;
  @Mock
  private Bibframe2MarcMapper bibframe2MarcMapper;
  @Mock
  private ResourceModelMapper resourceModelMapper;

  @Test
  void testMapping() {
    // given
    var instance = new Resource().setId(randomLong()).addTypes(INSTANCE);
    var inventoryId = UUID.randomUUID().toString();
    var srsId = UUID.randomUUID().toString();
    instance.setFolioMetadata(
      new FolioMetadata(instance)
        .setInventoryId(inventoryId)
        .setSrsId(srsId)
    );
    var resourceModel = new org.folio.ld.dictionary.model.Resource()
      .setId(instance.getId())
      .addType(INSTANCE);
    doReturn(resourceModel).when(resourceModelMapper).toModel(instance);
    var marcString = "{}";
    doReturn(marcString).when(bibframe2MarcMapper).toMarcJson(resourceModel);

    // when
    var result = kafkaInventoryMessageMapper.toInstanceIngressEvent(instance);

    // when
    assertThat(result)
      .hasAllNullFieldsOrPropertiesExcept("id", "eventPayload")
      .hasFieldOrProperty("id")
      .extracting("eventPayload")
      .hasFieldOrPropertyWithValue("sourceRecordIdentifier", srsId)
      .hasFieldOrPropertyWithValue("sourceType", InstanceIngressPayload.SourceTypeEnum.LINKED_DATA)
      .hasFieldOrPropertyWithValue("sourceRecordObject", marcString)
      .hasFieldOrPropertyWithValue("additionalProperties",
        Map.of("linkedDataId", instance.getId(), "instanceId", inventoryId)
      );
  }

}
