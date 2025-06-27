package org.folio.linked.data.mapper.dto;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.folio.ld.dictionary.ResourceTypeDictionary.PROVIDER_EVENT;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceTypeEntity;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@UnitTest
class ResourceMarcViewDtoMapperTest {

  private ResourceMarcViewDtoMapperImpl resourceMapper;

  @BeforeEach
  void setUp() {
    resourceMapper = new ResourceMarcViewDtoMapperImpl();
  }

  @Test
  void toMarcViewDto_shouldReturnResourceMarcViewDto() {
    //given
    var expectedDocumentType = "MARC_BIB";
    var resource = generateTestResource();
    var marc = "{value: \"some marc json string\"";

    //when
    var resourceMarcViewDto = resourceMapper.toMarcViewDto(resource, marc);

    //then
    assertThat(resourceMarcViewDto)
      .isNotNull()
      .hasFieldOrPropertyWithValue("id", resource.getId().toString())
      .hasFieldOrPropertyWithValue("recordType", expectedDocumentType)
      .extracting("parsedRecord")
      .isNotNull()
      .hasFieldOrPropertyWithValue("content", marc);
  }

  private Resource generateTestResource() {
    return new Resource()
      .setId(3856321131L)
      .setTypes(Set.of(new ResourceTypeEntity().setUri(PROVIDER_EVENT.getUri())))
      .setDoc(new ObjectMapper().valueToTree(Map.of(
        "http://bibfra.me/vocab/lite/name", List.of("name $ 2023"),
        "http://bibfra.me/vocab/lite/providerDate", List.of("1981"),
        "http://bibfra.me/vocab/lite/place", List.of("Alaska")))
      )
      .setLabel("Alaska")
      .setIndexDate(Timestamp.valueOf(LocalDateTime.parse("2018-05-05T11:50:55")));
  }
}
