package org.folio.linked.data.service.resource.meta;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.folio.linked.data.model.entity.ResourceSource.LINKED_DATA;
import static org.folio.linked.data.test.TestUtil.randomLong;

import java.util.UUID;
import org.folio.ld.dictionary.ResourceTypeDictionary;
import org.folio.linked.data.model.entity.FolioMetadata;
import org.folio.linked.data.model.entity.Resource;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.Test;

@UnitTest
class MetadataServiceTest {

  private final MetadataServiceImpl metadataService = new MetadataServiceImpl();

  @Test
  void ensure1_shouldDoNothing_ifGivenResourceIsNotInstance() {
    // given
    var resource = new Resource()
      .setIdAndRefreshEdges(randomLong());

    // when
    metadataService.ensure(resource);

    // then
    assertThat(resource.getFolioMetadata()).isNull();
  }

  @Test
  void ensure2_shouldDoNothing_ifGivenResourceIsNotInstance() {
    // given
    var resource = new Resource()
      .setIdAndRefreshEdges(randomLong());
    var metadata = new FolioMetadata(resource)
      .setInventoryId(UUID.randomUUID().toString())
      .setSrsId(UUID.randomUUID().toString());

    // when
    metadataService.ensure(resource, metadata);

    // then
    assertThat(resource.getFolioMetadata()).isNull();
  }

  @Test
  void ensure1_shouldSetRandomIdsAndSourceLd_ifGivenResourceIsInstance() {
    // given
    var resource = new Resource()
      .setIdAndRefreshEdges(randomLong())
      .addTypes(ResourceTypeDictionary.INSTANCE);

    // when
    metadataService.ensure(resource);

    // then
    assertThat(resource)
      .hasFieldOrPropertyWithValue("id", resource.getId())
      .extracting("folioMetadata")
      .hasFieldOrPropertyWithValue("id", resource.getId())
      .hasFieldOrPropertyWithValue("source", LINKED_DATA)
      .hasFieldOrPropertyWithValue("resource", resource);
  }

  @Test
  void ensure2_shouldSetGivenIdsAndSourceLd_ifGivenResourceIsInstance() {
    // given
    var resource = new Resource()
      .setIdAndRefreshEdges(randomLong())
      .addTypes(ResourceTypeDictionary.INSTANCE);
    var oldMetadata = new FolioMetadata(resource)
      .setInventoryId(UUID.randomUUID().toString())
      .setSrsId(UUID.randomUUID().toString());

    // when
    metadataService.ensure(resource, oldMetadata);

    // then
    assertThat(resource)
      .hasFieldOrPropertyWithValue("id", resource.getId())
      .extracting("folioMetadata")
      .hasFieldOrPropertyWithValue("id", resource.getId())
      .hasFieldOrPropertyWithValue("inventoryId", oldMetadata.getInventoryId())
      .hasFieldOrPropertyWithValue("srsId", oldMetadata.getSrsId())
      .hasFieldOrPropertyWithValue("source", LINKED_DATA)
      .hasFieldOrPropertyWithValue("resource", resource);
  }
}
