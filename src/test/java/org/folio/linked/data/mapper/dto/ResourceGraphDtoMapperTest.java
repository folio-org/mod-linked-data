package org.folio.linked.data.mapper.dto;

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
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceEdge;
import org.folio.linked.data.model.entity.ResourceTypeEntity;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@UnitTest
@ExtendWith(MockitoExtension.class)
class ResourceGraphDtoMapperTest {

  private ResourceGraphDtoMapper resourceMapper;

  @BeforeEach
  void setUp() {
    resourceMapper = new ResourceGraphDtoMapperImpl();
  }

  @Test
  void toResourceGraphDto_shouldReturnResourceGraphDto() {
    //given
    var resource = generateTestResource();
    var providerPlace = new Resource().setIdAndRefreshEdges(1654360880L);
    resource.setOutgoingEdges(Set.of(new ResourceEdge(resource, providerPlace, PROVIDER_PLACE)));

    //when
    var resourceGraphDto = resourceMapper.toResourceGraphDto(resource);

    //then
    assertEquals(resource.getId().toString(), resourceGraphDto.getId());
    assertEquals(resource.getTypes().iterator().next().getUri(), resourceGraphDto.getTypes().getFirst());
    assertEquals(resource.getDoc(), resourceGraphDto.getDoc());
    assertEquals(resource.getLabel(), resourceGraphDto.getLabel());
    assertEquals(resource.getOutgoingEdges().iterator().next().getTarget().getId(),
      resourceGraphDto.getOutgoingEdges().getEdges()
        .get(resource.getOutgoingEdges().iterator().next().getPredicate().getUri()).getFirst());
    assertEquals(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX").format(resource.getIndexDate()),
      resourceGraphDto.getIndexDate());
  }

  private Resource generateTestResource() {
    return new Resource()
      .setIdAndRefreshEdges(3856321131L)
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
