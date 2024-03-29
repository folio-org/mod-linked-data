package org.folio.linked.data.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.ld.dictionary.PredicateDictionary.PROVIDER_PLACE;
import static org.folio.ld.dictionary.ResourceTypeDictionary.PROVIDER_EVENT;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.folio.linked.data.mapper.dto.ResourceDtoMapperImpl;
import org.folio.linked.data.mapper.kafka.KafkaMessageMapper;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceEdge;
import org.folio.linked.data.model.entity.ResourceTypeEntity;
import org.folio.spring.test.type.UnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@UnitTest
@ExtendWith(MockitoExtension.class)
class ResourceDtoMapperTest {

  @InjectMocks
  private ResourceDtoMapperImpl resourceMapper;

  @Mock
  private KafkaMessageMapper kafkaMessageMapper;

  @Test
  void toResourceGraphDto_shouldReturnResourceGraphDto() {
    //given
    var resource = generateTestResource();
    var providerPlace = new Resource().setId(1654360880L);
    resource.setOutgoingEdges(Set.of(new ResourceEdge(resource, providerPlace, PROVIDER_PLACE)));

    //when
    var resourceGraphDto = resourceMapper.toResourceGraphDto(resource);

    //then
    assertEquals(resource.getId().toString(), resourceGraphDto.getId());
    assertEquals(resource.getTypes().iterator().next().getUri(), resourceGraphDto.getTypes().get(0));
    assertEquals(resource.getDoc(), resourceGraphDto.getDoc());
    assertEquals(resource.getLabel(), resourceGraphDto.getLabel());
    assertEquals(resource.getOutgoingEdges().iterator().next().getTarget().getId(),
      resourceGraphDto.getOutgoingEdges().getEdges()
        .get(resource.getOutgoingEdges().iterator().next().getPredicate().getUri()).get(0));
    assertEquals(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX").format(resource.getIndexDate()),
      resourceGraphDto.getIndexDate());
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
