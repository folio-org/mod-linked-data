package org.folio.linked.data.service.rdf;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Set;
import org.folio.linked.data.exception.RequestProcessingException;
import org.folio.linked.data.exception.RequestProcessingExceptionBuilder;
import org.folio.linked.data.mapper.ResourceModelMapper;
import org.folio.linked.data.model.entity.event.ResourceCreatedEvent;
import org.folio.linked.data.repo.ResourceRepository;
import org.folio.linked.data.service.resource.graph.ResourceGraphService;
import org.folio.linked.data.service.resource.meta.MetadataService;
import org.folio.rdf4ld.service.Rdf4LdService;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.multipart.MultipartFile;

@UnitTest
@ExtendWith(MockitoExtension.class)
class RdfImportServiceTest {

  @InjectMocks
  private RdfImportServiceImpl rdfImportService;
  @Mock
  private Rdf4LdService rdf4LdService;
  @Mock
  private MetadataService metadataService;
  @Mock
  private ResourceRepository resourceRepo;
  @Mock
  private ResourceModelMapper resourceModelMapper;
  @Mock
  private ResourceGraphService resourceGraphService;
  @Mock
  private ApplicationEventPublisher applicationEventPublisher;
  @Mock
  private RequestProcessingExceptionBuilder exceptionBuilder;

  @Test
  void importFile_savesResourcesAndPublishesEvents_whenValidFileProvided() throws IOException {
    // given
    var multipartFile = mock(MultipartFile.class);
    var inputStream = mock(InputStream.class);
    var resources = Set.of(mock(org.folio.ld.dictionary.model.Resource.class));
    var entity = mock(org.folio.linked.data.model.entity.Resource.class);
    when(multipartFile.getInputStream()).thenReturn(inputStream);
    when(rdf4LdService.mapToLdInstance(inputStream, multipartFile.getContentType())).thenReturn(resources);
    when(resourceModelMapper.toEntity(any())).thenReturn(entity);
    when(resourceRepo.existsById(anyLong())).thenReturn(false);
    when(resourceGraphService.saveMergingGraph(entity)).thenReturn(entity);

    // when
    var result = rdfImportService.importFile(multipartFile);

    // then
    assertThat(result.getResources()).hasSize(1);
    verify(metadataService).ensure(entity);
    verify(applicationEventPublisher).publishEvent(any(ResourceCreatedEvent.class));
  }

  @Test
  void importFile_ignoresExistingResourceAndNotPublishEvent_whenResourceAlreadyExists() throws IOException {
    // given
    var multipartFile = mock(MultipartFile.class);
    var inputStream = mock(InputStream.class);
    var resources = Set.of(mock(org.folio.ld.dictionary.model.Resource.class));
    var mappedEntity = mock(org.folio.linked.data.model.entity.Resource.class);
    when(multipartFile.getInputStream()).thenReturn(inputStream);
    when(rdf4LdService.mapToLdInstance(inputStream, multipartFile.getContentType())).thenReturn(resources);
    when(resourceModelMapper.toEntity(any())).thenReturn(mappedEntity);
    when(resourceRepo.existsById(anyLong())).thenReturn(true);

    // when
    var result = rdfImportService.importFile(multipartFile);

    // then
    assertThat(result.getResources()).isEmpty();
    verify(resourceGraphService, never()).saveMergingGraph(any());
    verify(applicationEventPublisher, never()).publishEvent(any());
  }

  @Test
  void importFile_throwsException_whenFileCannotBeRead() throws IOException {
    // given
    var multipartFile = mock(MultipartFile.class);
    when(multipartFile.getInputStream()).thenThrow(IOException.class);
    var expectedException = new RequestProcessingException(400, "code", new HashMap<>(), "message");
    when(exceptionBuilder.badRequestException(any(), any())).thenReturn(expectedException);

    // when
    assertThatThrownBy(() -> rdfImportService.importFile(multipartFile))
      // then
      .isEqualTo(expectedException);
  }

}
