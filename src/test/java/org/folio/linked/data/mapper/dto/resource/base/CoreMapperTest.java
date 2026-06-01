package org.folio.linked.data.mapper.dto.resource.base;

import static org.folio.ld.dictionary.PredicateDictionary.INSTANTIATES;
import static org.folio.linked.data.test.TestUtil.TEST_JSON_MAPPER;
import static org.folio.linked.data.test.TestUtil.randomLong;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;
import org.folio.ld.dictionary.PredicateDictionary;
import org.folio.ld.dictionary.model.Predicate;
import org.folio.linked.data.domain.dto.IdentifierRequest;
import org.folio.linked.data.domain.dto.InstanceResponse;
import org.folio.linked.data.model.entity.Resource;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.node.StringNode;

@UnitTest
@ExtendWith(MockitoExtension.class)
class CoreMapperTest {

  @InjectMocks
  private CoreMapperImpl coreMapper;
  @Mock
  private SingleResourceMapper singleResourceMapper;

  @BeforeEach
  void setUp() {
    reset(singleResourceMapper);
  }

  @Test
  void toDtoWithEdges_shouldThrowNpe_ifGivenResourceIsNull(@Mock Consumer<InstanceResponse> consumer) {
    // given
    Resource resource = null;

    // when
    NullPointerException thrown = assertThrows(NullPointerException.class,
      () -> coreMapper.toDtoWithEdges(resource, InstanceResponse.class, false));

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
  void toJson_shouldReturnCorrectJsonNodeFromMap() {
    // given
    var map = new HashMap<String, List<String>>();
    map.put("key1", List.of("value1.1", "value1.2"));
    map.put("key2", List.of("value2.1", "value2.2"));

    // when
    var jsonNode = coreMapper.toJson(map);

    // then
    assertThat(TEST_JSON_MAPPER.writeValueAsString(jsonNode),
      is("{\"key1\":[\"value1.1\",\"value1.2\"],\"key2\":[\"value2.1\",\"value2.2\"]}"));
  }

  @Test
  void addOutgoingEdges_shouldThrowNpe_ifGivenSourceIsNull() {
    // given
    var dtoList = new ArrayList<>();
    Resource source = null;
    var predicate = PredicateDictionary.MAP;
    var parent = InstanceResponse.class;

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
    var parent = InstanceResponse.class;

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
    var parent = InstanceResponse.class;

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
    var parent = InstanceResponse.class;

    // when
    coreMapper.addOutgoingEdges(source, parent, dtoList, predicate);

    // then
    verify(singleResourceMapper, never()).toEntity(any(), any(), any(), any());
    assertThat(source.getOutgoingEdges(), hasSize(0));
  }

  @Test
  void addOutgoingEdges_shouldAddMappedEdgesToResource_ifGivenDtoListIsNotEmpty() {
    // given
    var dto1 = new IdentifierRequest().value(List.of(randomLong().toString()));
    var dto2 = new IdentifierRequest().value(List.of(randomLong().toString()));
    var predicate = PredicateDictionary.MAP;
    var parent = InstanceResponse.class;
    var source = new Resource();
    var expectedTarget1 = new Resource().setDoc(new StringNode("1")).setIdAndRefreshEdges(111L);
    doReturn(expectedTarget1).when(singleResourceMapper).toEntity(dto1, parent, predicate, source);
    var expectedTarget2 = new Resource().setDoc(new StringNode("2")).setIdAndRefreshEdges(222L);
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

  @Test
  void addIncomingEdges_shouldThrowNpe_ifGivenChildEntityIsNull() {
    // given
    Resource childEntity = null;
    var dtoList = List.of();

    // when
    NullPointerException thrown = assertThrows(NullPointerException.class,
      () -> coreMapper.addIncomingEdges(childEntity, Object.class, dtoList, INSTANTIATES));

    // then
    assertThat(thrown.getMessage(), is("childEntity is marked non-null but is null"));
  }

  @Test
  void addIncomingEdges_shouldThrowNpe_ifGivenParentDtoClassIsNull() {
    // given
    var childEntity = new Resource();
    var dtoList = List.of();
    Class<Object> parent = null;

    // when
    NullPointerException thrown = assertThrows(NullPointerException.class,
      () -> coreMapper.addIncomingEdges(childEntity, parent, dtoList, INSTANTIATES));

    // then
    assertThat(thrown.getMessage(), is("parentDtoClass is marked non-null but is null"));
  }

  @Test
  void addIncomingEdges_shouldThrowNpe_ifGivenPredicateIsNull() {
    // given
    var childEntity = new Resource();
    var dtoList = List.of();
    Predicate predicate = null;

    // when
    NullPointerException thrown = assertThrows(NullPointerException.class,
      () -> coreMapper.addIncomingEdges(childEntity, Object.class, dtoList, predicate));

    // then
    assertThat(thrown.getMessage(), is("predicate is marked non-null but is null"));
  }

  @Test
  void addIncomingEdges_shouldDoNothing_ifGivenDtoListIsNull() {
    // given
    var childEntity = new Resource();

    // when
    coreMapper.addIncomingEdges(childEntity, Object.class, null, INSTANTIATES);

    // then
    verifyNoInteractions(singleResourceMapper);
    assertThat(childEntity.getIncomingEdges(), hasSize(0));
  }

  @Test
  void addIncomingEdges_shouldDoNothing_ifGivenDtoListIsEmpty() {
    // given
    var childEntity = new Resource();

    // when
    coreMapper.addIncomingEdges(childEntity, Object.class, Collections.emptyList(), INSTANTIATES);

    // then
    verifyNoInteractions(singleResourceMapper);
    assertThat(childEntity.getIncomingEdges(), hasSize(0));
  }

  @Test
  void addIncomingEdges_shouldAddMappedEdgesToResource_ifGivenDtoListIsNotEmpty() {
    // given
    var childEntity = new Resource();
    var parent = Object.class;
    var predicate = INSTANTIATES;
    var dto1 = new Object();
    var dto2 = new Object();
    var expectedSource1 = new Resource().setIdAndRefreshEdges(111L);
    doReturn(expectedSource1).when(singleResourceMapper).toEntity(dto1, parent, predicate, childEntity);
    var expectedSource2 = new Resource().setIdAndRefreshEdges(222L);
    doReturn(expectedSource2).when(singleResourceMapper).toEntity(dto2, parent, predicate, childEntity);
    var dtoList = List.of(dto1, dto2);

    // when
    coreMapper.addIncomingEdges(childEntity, parent, dtoList, predicate);

    // then
    assertThat(childEntity.getIncomingEdges(), hasSize(2));
    var edgesAreExpected = childEntity.getIncomingEdges().stream().allMatch(edge ->
      edge.getPredicate().getHash().equals(predicate.getHash())
        && edge.getTarget().equals(childEntity)
        && (edge.getSource().equals(expectedSource1) || edge.getSource().equals(expectedSource2))
    );
    assertTrue(edgesAreExpected);
  }
}
