package org.folio.linked.data.mapper.resource.common;

import static org.folio.linked.data.test.TestUtil.OBJECT_MAPPER;
import static org.folio.linked.data.test.TestUtil.randomLong;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.TextNode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;
import org.folio.ld.dictionary.PredicateDictionary;
import org.folio.ld.dictionary.model.Predicate;
import org.folio.linked.data.domain.dto.Instance;
import org.folio.linked.data.domain.dto.Isbn;
import org.folio.linked.data.mapper.dto.common.CoreMapperImpl;
import org.folio.linked.data.mapper.dto.common.SingleResourceMapper;
import org.folio.linked.data.model.entity.Resource;
import org.folio.spring.test.type.UnitTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@UnitTest
@ExtendWith(MockitoExtension.class)
class CoreMapperTest {

  @InjectMocks
  private CoreMapperImpl coreMapper;
  @Spy
  private ObjectMapper objectMapper = OBJECT_MAPPER;
  @Mock
  private SingleResourceMapper singleResourceMapper;

  @BeforeEach
  void setUp() {
    reset(singleResourceMapper);
  }

  @Test
  void toDtoWithEdges_shouldThrowNpe_ifGivenResourceIsNull(@Mock Consumer<Instance> consumer) {
    // given
    Resource resource = null;

    // when
    NullPointerException thrown = assertThrows(NullPointerException.class,
      () -> coreMapper.toDtoWithEdges(resource, Instance.class, false));

    // then
    assertThat(thrown.getMessage(), is("resource is marked non-null but is null"));
  }

  @Test
  void toDtoWithEdges_shouldThrowNpe_ifGivenDtoClassIsNull(@Mock Resource resource) {
    // given
    Class dtoClass = null;

    // when
    NullPointerException thrown = assertThrows(NullPointerException.class,
      () -> coreMapper.toDtoWithEdges(resource, dtoClass, false));

    // then
    assertThat(thrown.getMessage(), is("dtoClass is marked non-null but is null"));
  }

  @Test
  void toJson_shouldReturnCorrectJsonNodeFromMap() throws JsonProcessingException {
    // given
    var map = new HashMap<String, List<String>>();
    map.put("key1", List.of("value1.1", "value1.2"));
    map.put("key2", List.of("value2.1", "value2.2"));

    // when
    var jsonNode = coreMapper.toJson(map);

    // then
    assertThat(OBJECT_MAPPER.writeValueAsString(jsonNode),
      is("{\"key1\":[\"value1.1\",\"value1.2\"],\"key2\":[\"value2.1\",\"value2.2\"]}"));
  }

  @Test
  void addOutgoingEdges_shouldThrowNpe_ifGivenSourceIsNull() {
    // given
    var dtoList = new ArrayList<>();
    Resource source = null;
    var predicate = PredicateDictionary.MAP;
    var parent = Instance.class;

    // when
    NullPointerException thrown = assertThrows(NullPointerException.class,
      () -> coreMapper.addOutgoingEdges(source, parent, dtoList, predicate));

    // then
    assertThat(thrown.getMessage(), is("parentEntity is marked non-null but is null"));
  }

  @Test
  void addOutgoingEdges_shouldThrowNpe_ifGivenPredicateIsNull() {
    // given
    var dtoList = new ArrayList<>();
    var source = new Resource();
    Predicate predicate = null;
    var parent = Instance.class;

    // when
    NullPointerException thrown = assertThrows(NullPointerException.class,
      () -> coreMapper.addOutgoingEdges(source, parent, dtoList, predicate));

    // then
    assertThat(thrown.getMessage(), is("predicate is marked non-null but is null"));
  }

  @Test
  void addOutgoingEdges_shouldThrowNpe_ifGivenParentIsNull() {
    // given
    var dtoList = new ArrayList<>();
    var source = new Resource();
    var predicate = PredicateDictionary.MAP;
    Class<Object> parent = null;

    // when
    NullPointerException thrown = assertThrows(NullPointerException.class,
      () -> coreMapper.addOutgoingEdges(source, parent, dtoList, predicate));

    // then
    assertThat(thrown.getMessage(), is("parentDtoClass is marked non-null but is null"));
  }

  @Test
  void addOutgoingEdges_shouldDoNothing_ifGivenDtoListIsNull() {
    // given
    List dtoList = null;
    var source = new Resource();
    var predicate = PredicateDictionary.MAP;
    var parent = Instance.class;

    // when
    coreMapper.addOutgoingEdges(source, parent, dtoList, predicate);

    // then
    verify(singleResourceMapper, never()).toEntity(any(), any(), any(), any());
    assertThat(source.getOutgoingEdges().isEmpty(), is(true));
  }

  @Test
  void addOutgoingEdges_shouldDoNothing_ifGivenDtoListIsEmpty() {
    // given
    var dtoList = new ArrayList<>();
    var source = new Resource();
    var predicate = PredicateDictionary.MAP;
    var parent = Instance.class;

    // when
    coreMapper.addOutgoingEdges(source, parent, dtoList, predicate);

    // then
    verify(singleResourceMapper, never()).toEntity(any(), any(), any(), any());
    assertThat(source.getOutgoingEdges(), hasSize(0));
  }

  @Test
  void addOutgoingEdges_shouldAddMappedEdgesToResource_ifGivenDtoListIsNotEmpty() {
    // given
    var dto1 = new Isbn().id(randomLong().toString());
    var dto2 = new Isbn().id(randomLong().toString());
    var predicate = PredicateDictionary.MAP;
    var parent = Instance.class;
    var source = new Resource();
    var expectedTarget1 = new Resource().setDoc(new TextNode("1")).setResourceHash(111L);
    doReturn(expectedTarget1).when(singleResourceMapper).toEntity(dto1, parent, predicate, source);
    var expectedTarget2 = new Resource().setDoc(new TextNode("2")).setResourceHash(222L);
    doReturn(expectedTarget2).when(singleResourceMapper).toEntity(dto2, parent, predicate, source);
    var dtoList = List.of(dto1, dto2);

    // when
    coreMapper.addOutgoingEdges(source, parent, dtoList, predicate);

    // then
    assertThat(source.getOutgoingEdges(), hasSize(2));
    var edgesAreExpected = source.getOutgoingEdges().stream().allMatch(edge ->
      edge.getPredicate().getHash().equals(predicate.getHash())
        && edge.getSource().equals(source)
        && (edge.getTarget().equals(expectedTarget1)) || edge.getTarget().equals(expectedTarget2));
    assertThat(edgesAreExpected, is(true));
  }

}
