package org.folio.linked.data.service.resource.edge;

import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.ld.dictionary.PredicateDictionary.DISSERTATION;
import static org.folio.ld.dictionary.PredicateDictionary.GENRE;
import static org.folio.ld.dictionary.PredicateDictionary.IS_PART_OF;
import static org.folio.ld.dictionary.PredicateDictionary.OTHER_EDITION;
import static org.folio.ld.dictionary.PredicateDictionary.OTHER_VERSION;
import static org.folio.ld.dictionary.PredicateDictionary.RELATED_WORK;
import static org.folio.ld.dictionary.PredicateDictionary.TITLE;
import static org.folio.ld.dictionary.ResourceTypeDictionary.SERIES;
import static org.folio.linked.data.test.TestUtil.randomLong;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import org.folio.ld.dictionary.PredicateDictionary;
import org.folio.ld.dictionary.ResourceTypeDictionary;
import org.folio.linked.data.mapper.ResourceModelMapper;
import org.folio.linked.data.model.entity.PredicateEntity;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceEdge;
import org.folio.linked.data.model.entity.ResourceTypeEntity;
import org.folio.linked.data.repo.ResourceEdgeRepository;
import org.folio.linked.data.service.resource.graph.ResourceGraphService;
import org.folio.linked.data.service.resource.graph.SaveGraphResult;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@UnitTest
@ExtendWith(MockitoExtension.class)
class ResourceEdgeServiceTest {

  @InjectMocks
  private ResourceEdgeServiceImpl resourceEdgeService;

  @Mock
  private ResourceGraphService resourceGraphService;
  @Mock
  private ResourceModelMapper resourceModelMapper;
  @Mock
  private ResourceEdgeRepository resourceEdgeRepository;

  @Test
  void saveNewResourceEdge_shouldSaveMappedEdgeResourceWithReferenceToSource() {
    // given
    var sourceId = randomLong();
    var targetModel = new org.folio.ld.dictionary.model.Resource().setId(randomLong());
    var mappedTarget = new Resource().setIdAndRefreshEdges(targetModel.getId());
    doReturn(mappedTarget).when(resourceModelMapper).toEntity(targetModel);
    doReturn(new SaveGraphResult(mappedTarget)).when(resourceGraphService).saveMergingGraph(mappedTarget);
    when(resourceEdgeRepository.save(any(ResourceEdge.class)))
      .thenAnswer(i -> i.getArgument(0));

    // when
    var result = resourceEdgeService.saveNewResourceEdge(sourceId, TITLE, targetModel);

    // then
    assertThat(result.getSourceHash()).isEqualTo(sourceId);
    assertThat(result.getTargetHash()).isEqualTo(targetModel.getId());
    assertThat(result.getPredicateHash()).isEqualTo(TITLE.getHash());
  }

  @Test
  void deleteEdgesHavingPredicate_shouldDelegateToRepository() {
    // given
    var resourceId = randomLong();
    var predicate = PredicateDictionary.GENRE;
    var expectedDeletedCount = 2L;
    doReturn(expectedDeletedCount).when(resourceEdgeRepository)
      .deleteByIdSourceHashAndIdPredicateHash(resourceId, predicate.getHash());

    // when
    var result = resourceEdgeService.deleteEdgesHavingPredicate(resourceId, predicate);

    // then
    assertThat(result).isEqualTo(expectedDeletedCount);
    verify(resourceEdgeRepository).deleteByIdSourceHashAndIdPredicateHash(resourceId, predicate.getHash());
  }

  static Stream<Arguments> dataProvider() {
    return Stream.of(
      Arguments.of(getWork(), getEmptyWork(), List.of(
        DISSERTATION.getUri(), GENRE.getUri(), IS_PART_OF.getUri(),
        OTHER_EDITION.getUri(), OTHER_VERSION.getUri(), RELATED_WORK.getUri())),
      Arguments.of(getWorkWithIsPartOfPointingToSeries(), getEmptyWork(), List.of(
        DISSERTATION.getUri(), GENRE.getUri(),
        OTHER_EDITION.getUri(), OTHER_VERSION.getUri(), RELATED_WORK.getUri())),
      Arguments.of(getInstance(), getEmptyWork(), List.of()),
      Arguments.of(getWork(), getEmptyInstance(), List.of())
    );
  }

  @ParameterizedTest
  @MethodSource("dataProvider")
  void copyOutgoingEdges_shouldCopy_appropriateEdges(Resource from,
                                                     Resource to,
                                                     List<String> expectedPredicates) {
    //when
    resourceEdgeService.copyOutgoingEdges(from, to);

    //then
    assertThat(to.getOutgoingEdges()
      .stream()
      .map(ResourceEdge::getPredicate)
      .map(PredicateEntity::getUri)
      .toList()
    ).containsExactlyInAnyOrderElementsOf(expectedPredicates);
  }

  static Stream<Arguments> incomingDataProvider() {
    return Stream.of(
      Arguments.of(getWorkWithIncomingEdges(), getEmptyWork(), List.of(TITLE.getUri(), DISSERTATION.getUri())),
      Arguments.of(getInstanceWithIncomingEdges(), getEmptyInstance(), List.of(TITLE.getUri())),
      Arguments.of(getWorkWithIncomingEdges(), getEmptyInstance(), List.of())
    );
  }

  @ParameterizedTest
  @MethodSource("incomingDataProvider")
  void copyIncomingEdges_shouldCopy_appropriateEdges(Resource from,
                                                     Resource to,
                                                     List<String> expectedPredicates) {
    //when
    resourceEdgeService.copyIncomingEdges(from, to);

    //then
    assertThat(to.getIncomingEdges()
      .stream()
      .map(ResourceEdge::getPredicate)
      .map(PredicateEntity::getUri)
      .toList()
    ).containsExactlyInAnyOrderElementsOf(expectedPredicates);
  }

  private static Resource getEmptyInstance() {
    var instance = new Resource();
    instance.setTypes(Set.of(new ResourceTypeEntity(1L, ResourceTypeDictionary.INSTANCE.getUri(), "instance")));
    return instance;
  }

  private static Resource getInstance() {
    var instance = getEmptyInstance();
    instance.addOutgoingEdge(new ResourceEdge(instance, new Resource(), TITLE));
    return instance;
  }

  private static Resource getEmptyWork() {
    var work = new Resource();
    work.setTypes(Set.of(new ResourceTypeEntity(2L, ResourceTypeDictionary.WORK.getUri(), "work")));
    return work;
  }

  private static Resource getWork() {
    var work = getEmptyWork();
    work.addOutgoingEdge(new ResourceEdge(work, new Resource(), TITLE));
    work.addOutgoingEdge(new ResourceEdge(work, new Resource(), DISSERTATION));
    work.addOutgoingEdge(new ResourceEdge(work, new Resource(), GENRE));
    work.addOutgoingEdge(new ResourceEdge(work, new Resource(), IS_PART_OF));
    work.addOutgoingEdge(new ResourceEdge(work, new Resource(), OTHER_EDITION));
    work.addOutgoingEdge(new ResourceEdge(work, new Resource(), OTHER_VERSION));
    work.addOutgoingEdge(new ResourceEdge(work, new Resource(), RELATED_WORK));
    return work;
  }

  private static Resource getWorkWithIsPartOfPointingToSeries() {
    var work = getEmptyWork();
    work.addOutgoingEdge(new ResourceEdge(work, new Resource(), TITLE));
    work.addOutgoingEdge(new ResourceEdge(work, new Resource(), DISSERTATION));
    work.addOutgoingEdge(new ResourceEdge(work, new Resource(), GENRE));
    work.addOutgoingEdge(new ResourceEdge(work, getSeriesResource(), IS_PART_OF));
    work.addOutgoingEdge(new ResourceEdge(work, new Resource(), OTHER_EDITION));
    work.addOutgoingEdge(new ResourceEdge(work, new Resource(), OTHER_VERSION));
    work.addOutgoingEdge(new ResourceEdge(work, new Resource(), RELATED_WORK));
    return work;
  }

  private static Resource getSeriesResource() {
    var series = new Resource();
    series.setTypes(Set.of(new ResourceTypeEntity(3L, SERIES.getUri(), "series")));
    return series;
  }

  private static Resource getWorkWithIncomingEdges() {
    var work = getEmptyWork();
    work.addIncomingEdge(new ResourceEdge(new Resource(), work, TITLE));
    work.addIncomingEdge(new ResourceEdge(new Resource(), work, DISSERTATION));
    return work;
  }

  private static Resource getInstanceWithIncomingEdges() {
    var instance = getEmptyInstance();
    instance.addIncomingEdge(new ResourceEdge(new Resource(), instance, TITLE));
    return instance;
  }
}
