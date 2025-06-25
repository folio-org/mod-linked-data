package org.folio.linked.data.service.rdf;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Optional;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.folio.ld.dictionary.ResourceTypeDictionary;
import org.folio.linked.data.exception.RequestProcessingException;
import org.folio.linked.data.exception.RequestProcessingExceptionBuilder;
import org.folio.linked.data.mapper.ResourceModelMapper;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.repo.ResourceRepository;
import org.folio.rdf4ld.service.Rdf4LdService;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@UnitTest
@ExtendWith(MockitoExtension.class)
class RdfExportServiceTest {

  @InjectMocks
  private RdfExportServiceImpl rdfExportService;
  @Mock
  private Rdf4LdService rdf4LdService;
  @Mock
  private ResourceRepository resourceRepository;
  @Mock
  private ResourceModelMapper resourceModelMapper;
  @Mock
  private RequestProcessingExceptionBuilder exceptionBuilder;

  @Test
  void throwsNotFoundException_ifResourceNotFoundById() {
    // given
    var id = 123L;
    when(resourceRepository.findById(id)).thenReturn(Optional.empty());
    var expectedException = mock(RequestProcessingException.class);
    when(exceptionBuilder.notFoundLdResourceByIdException(any(), any())).thenReturn(expectedException);

    // when
    var thrown = assertThrows(RequestProcessingException.class, () -> rdfExportService.exportInstanceToRdf(id));

    // then
    assertThat(thrown).isEqualTo(expectedException);
  }

  @Test
  void throwsBadRequestException_ifResourceIsNotInstance() {
    // given
    var id = 123L;
    var resource = new Resource().setId(id);
    when(resourceRepository.findById(id)).thenReturn(Optional.of(resource));
    var expectedException = mock(RequestProcessingException.class);
    when(exceptionBuilder.badRequestException(any(), any())).thenReturn(expectedException);

    // when
    var thrown = assertThrows(RequestProcessingException.class, () -> rdfExportService.exportInstanceToRdf(id));

    // then
    assertThat(thrown).isEqualTo(expectedException);
  }

  @Test
  void returnsRdfResourceDto_ifResourceIsInstance() throws IOException {
    // given
    var id = 123L;
    var instance = new Resource().setId(id).addTypes(ResourceTypeDictionary.INSTANCE);
    when(resourceRepository.findById(id)).thenReturn(Optional.of(instance));
    var instanceModel = new org.folio.ld.dictionary.model.Resource().setId(id);
    when(resourceModelMapper.toModel(instance)).thenReturn(instanceModel);
    var exportedValue = "exported";
    var exportedStream = new ByteArrayOutputStream();
    exportedStream.write(exportedValue.getBytes());
    when(rdf4LdService.mapLdToBibframe2Rdf(instanceModel, RDFFormat.JSONLD)).thenReturn(exportedStream);

    // when
    var result = rdfExportService.exportInstanceToRdf(id);

    // then
    assertThat(result).isEqualTo(exportedValue);
  }
}
