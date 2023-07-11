package org.folio.linked.data.mapper;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.doReturn;

import java.util.Iterator;
import org.folio.linked.data.domain.dto.BibframeRequest;
import org.folio.linked.data.mapper.resource.common.ProfiledMapper;
import org.folio.linked.data.model.entity.Predicate;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceEdge;
import org.folio.spring.test.type.UnitTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@UnitTest
@ExtendWith(MockitoExtension.class)
class ResourceMapperTest {

  private ResourceMapper resourceMapper;
  @Mock
  private ProfiledMapper profiledMapper;

  @BeforeEach
  void setUp() {
    resourceMapper = new ResourceMapperImpl();
    ReflectionTestUtils.setField(resourceMapper, "profiledMapper", profiledMapper);
  }

  @Test
  void map_shouldFillEdgesPk() {
    // given
    var dto = new BibframeRequest();
    var re1 = new ResourceEdge(new Resource().setResourceHash(111L), new Resource().setResourceHash(222L),
      new Predicate().setPredicateHash(333L));
    var re2 = new ResourceEdge(new Resource().setResourceHash(444L), new Resource().setResourceHash(555L),
      new Predicate().setPredicateHash(666L));
    var re3 = new ResourceEdge(new Resource().setResourceHash(777L), new Resource().setResourceHash(888L),
      new Predicate().setPredicateHash(999L));
    re2.getTarget().getOutgoingEdges().add(re3);

    var expectedResource = new Resource();
    expectedResource.getOutgoingEdges().add(re1);
    expectedResource.getOutgoingEdges().add(re2);
    doReturn(expectedResource).when(profiledMapper).toEntity(dto);

    // when
    Resource resource = resourceMapper.map(dto);

    // then
    Iterator<ResourceEdge> resourceEdgeIterator = resource.getOutgoingEdges().iterator();
    ResourceEdge result1 = resourceEdgeIterator.next();
    assertThat(result1.getId().getSourceHash()).isEqualTo(re1.getSource().getResourceHash());
    assertThat(result1.getId().getTargetHash()).isEqualTo(re1.getTarget().getResourceHash());
    assertThat(result1.getId().getPredicateHash()).isEqualTo(re1.getPredicate().getPredicateHash());
    ResourceEdge result2 = resourceEdgeIterator.next();
    assertThat(result2.getId().getSourceHash()).isEqualTo(re2.getSource().getResourceHash());
    assertThat(result2.getId().getTargetHash()).isEqualTo(re2.getTarget().getResourceHash());
    assertThat(result2.getId().getPredicateHash()).isEqualTo(re2.getPredicate().getPredicateHash());
    ResourceEdge result3 = result2.getTarget().getOutgoingEdges().iterator().next();
    assertThat(result3.getId().getSourceHash()).isEqualTo(re3.getSource().getResourceHash());
    assertThat(result3.getId().getTargetHash()).isEqualTo(re3.getTarget().getResourceHash());
    assertThat(result3.getId().getPredicateHash()).isEqualTo(re3.getPredicate().getPredicateHash());
  }

}
