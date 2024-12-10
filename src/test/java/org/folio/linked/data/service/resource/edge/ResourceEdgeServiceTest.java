package org.folio.linked.data.service.resource.edge;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.folio.ld.dictionary.PredicateDictionary.TITLE;
import static org.folio.linked.data.test.TestUtil.randomLong;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import org.folio.ld.dictionary.model.Resource;
import org.folio.ld.dictionary.model.ResourceEdge;
import org.folio.linked.data.mapper.ResourceModelMapper;
import org.folio.linked.data.repo.ResourceEdgeRepository;
import org.folio.linked.data.repo.ResourceRepository;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@UnitTest
@ExtendWith(MockitoExtension.class)
class ResourceEdgeServiceTest {

  @InjectMocks
  private ResourceEdgeServiceImpl resourceEdgeService;

  @Mock
  private ResourceRepository resourceRepository;
  @Mock
  private ResourceModelMapper resourceModelMapper;
  @Mock
  private ResourceEdgeRepository resourceEdgeRepository;

  @Test
  void saveNewResourceEdge_shouldSaveMappedEdgeResourceWithReferenceToSource() {
    // given
    var sourceId = randomLong();
    var edgeModel = new ResourceEdge(new Resource().setId(sourceId), new Resource().setId(randomLong()), TITLE);
    var mappedEdgeResource = new org.folio.linked.data.model.entity.Resource().setId(edgeModel.getTarget().getId());
    doReturn(mappedEdgeResource).when(resourceModelMapper).toEntity(edgeModel.getTarget());
    doReturn(mappedEdgeResource).when(resourceRepository).save(mappedEdgeResource);
    when(resourceEdgeRepository.save(any(org.folio.linked.data.model.entity.ResourceEdge.class)))
      .thenAnswer(i -> i.getArguments()[0]);

    // when
    var result = resourceEdgeService.saveNewResourceEdge(sourceId, edgeModel);

    // then
    assertThat(result.getSourceHash()).isEqualTo(edgeModel.getSource().getId());
    assertThat(result.getTargetHash()).isEqualTo(edgeModel.getTarget().getId());
    assertThat(result.getPredicateHash()).isEqualTo(edgeModel.getPredicate().getHash());
  }

}
