package org.folio.linked.data.service.resource.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.folio.ld.dictionary.PredicateDictionary.REPLACED_BY;
import static org.folio.ld.dictionary.PropertyDictionary.RESOURCE_PREFERRED;
import static org.folio.ld.dictionary.ResourceTypeDictionary.PERSON;
import static org.folio.linked.data.test.TestUtil.OBJECT_MAPPER;
import static org.folio.linked.data.test.TestUtil.randomLong;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Optional;
import java.util.UUID;
import org.folio.ld.dictionary.model.FolioMetadata;
import org.folio.linked.data.mapper.ResourceModelMapper;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceEdge;
import org.folio.linked.data.model.entity.event.ResourceCreatedEvent;
import org.folio.linked.data.model.entity.event.ResourceReplacedEvent;
import org.folio.linked.data.model.entity.event.ResourceUpdatedEvent;
import org.folio.linked.data.repo.FolioMetadataRepository;
import org.folio.linked.data.repo.ResourceRepository;
import org.folio.linked.data.service.resource.ResourceGraphService;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

@UnitTest
@ExtendWith(MockitoExtension.class)
class ResourceMarcAuthorityServiceImplTest {

  @InjectMocks
  private ResourceMarcAuthorityServiceImpl resourceMarcService;

  @Mock
  private ResourceModelMapper resourceModelMapper;
  @Mock
  private ResourceRepository resourceRepo;
  @Mock
  private ResourceGraphService resourceGraphService;
  @Mock
  private ApplicationEventPublisher applicationEventPublisher;
  @Mock
  private FolioMetadataRepository folioMetadataRepo;
  @Spy
  private ObjectMapper objectMapper = OBJECT_MAPPER;

  @Test
  void saveMarcAuthority_shouldCreateNewAuthority_ifGivenModelDoesNotExistsByIdAndSrsId() {
    // given
    var id = randomLong();
    var srsId = UUID.randomUUID().toString();
    var model = new org.folio.ld.dictionary.model.Resource().setId(id)
      .setFolioMetadata(new FolioMetadata().setSrsId(srsId));
    var mapped = new Resource().setId(id).addTypes(PERSON);
    mapped.setFolioMetadata(new org.folio.linked.data.model.entity.FolioMetadata(mapped).setSrsId(srsId));
    doReturn(mapped).when(resourceModelMapper).toEntity(model);
    doReturn(false).when(resourceRepo).existsById(id);
    doReturn(mapped).when(resourceGraphService).saveMergingGraph(mapped);

    // when
    var result = resourceMarcService.saveMarcResource(model);

    // then
    assertThat(result).isEqualTo(id);
    verify(resourceGraphService).saveMergingGraph(mapped);
    verify(applicationEventPublisher).publishEvent(new ResourceCreatedEvent(mapped));
  }

  @Test
  void saveMarcAuthority_shouldUpdateAuthority_ifGivenModelExistsById() {
    // given
    var id = randomLong();
    var srsId = UUID.randomUUID().toString();
    var model = new org.folio.ld.dictionary.model.Resource().setId(id)
      .setFolioMetadata(new FolioMetadata().setSrsId(srsId));
    var mapped = new Resource().setId(id).addTypes(PERSON);
    mapped.setFolioMetadata(new org.folio.linked.data.model.entity.FolioMetadata(mapped).setSrsId(srsId));
    doReturn(mapped).when(resourceModelMapper).toEntity(model);
    doReturn(true).when(resourceRepo).existsById(id);
    doReturn(mapped).when(resourceGraphService).saveMergingGraph(mapped);

    // when
    var result = resourceMarcService.saveMarcResource(model);

    // then
    assertThat(result).isEqualTo(id);
    verify(resourceGraphService).saveMergingGraph(mapped);
    verify(applicationEventPublisher).publishEvent(new ResourceUpdatedEvent(mapped));
  }

  @Test
  void saveMarcAuthority_shouldCreateNewAuthorityVersionAndMarkOldAsObsolete_ifGivenModelExistsBySrsIdButNotById() {
    // given
    var id = randomLong();
    var srsId = UUID.randomUUID().toString();
    var existed = new Resource().setId(id).setManaged(true);
    existed.setFolioMetadata(new org.folio.linked.data.model.entity.FolioMetadata(existed));
    doReturn(Optional.of(existed)).when(resourceRepo).findByFolioMetadataSrsId(srsId);
    doReturn(true).when(folioMetadataRepo).existsBySrsId(srsId);
    var model = new org.folio.ld.dictionary.model.Resource()
      .setId(id)
      .setFolioMetadata(new FolioMetadata().setSrsId(srsId));
    var mapped = new Resource().setId(id).addTypes(PERSON);
    mapped.setFolioMetadata(new org.folio.linked.data.model.entity.FolioMetadata(mapped).setSrsId(srsId));
    doReturn(mapped).when(resourceModelMapper).toEntity(model);
    doReturn(false).when(resourceRepo).existsById(id);
    doReturn(existed).when(resourceRepo).save(existed);

    doReturn(mapped).when(resourceGraphService).saveMergingGraph(mapped);

    // when
    var result = resourceMarcService.saveMarcResource(model);

    // then
    assertThat(result).isEqualTo(id);
    assertThat(existed.isActive()).isFalse();
    assertThat(existed.getDoc().get(RESOURCE_PREFERRED.getValue()).get(0).textValue()).isEqualTo("false");
    assertThat(existed.getFolioMetadata()).isNull();
    verify(resourceRepo).save(existed);
    verify(resourceGraphService).saveMergingGraph(mapped);
    verify(applicationEventPublisher).publishEvent(new ResourceReplacedEvent(existed, mapped));
    assertThat(mapped.getDoc().get(RESOURCE_PREFERRED.getValue()).get(0).textValue()).isEqualTo("true");
    assertThat(mapped.getIncomingEdges()).contains(new ResourceEdge(existed, mapped, REPLACED_BY));
  }
}
