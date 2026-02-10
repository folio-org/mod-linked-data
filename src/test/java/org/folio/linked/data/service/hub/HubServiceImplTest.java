package org.folio.linked.data.service.hub;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.folio.ld.dictionary.PropertyDictionary.LINK;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.folio.linked.data.domain.dto.ResourceResponseDto;
import org.folio.linked.data.exception.RequestProcessingException;
import org.folio.linked.data.mapper.dto.resource.hub.HubMapperUnit;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.service.rdf.RdfImportService;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@UnitTest
@ExtendWith(MockitoExtension.class)
class HubServiceImplTest {

  @InjectMocks
  private HubServiceImpl hubService;

  @Mock
  private HubMapperUnit hubMapperUnit;
  @Mock
  private RdfImportService rdfImportService;

  @Test
  void previewHub_shouldDownloadAndConvertHub() {
    // given
    var hubUri = "https://example.com/hub.json";
    var resource = new Resource();
    var objectMapper = new ObjectMapper();
    var doc = objectMapper.createObjectNode();
    var linkArray = objectMapper.createArrayNode().add(hubUri);
    doc.set(LINK.getValue(), linkArray);
    resource.setDoc(doc);
    var expectedResponse = new ResourceResponseDto();

    when(rdfImportService.importRdfUrl(hubUri, false)).thenReturn(resource);
    when(hubMapperUnit.toDto(eq(resource), any(ResourceResponseDto.class), any())).thenReturn(expectedResponse);

    // when
    var result = hubService.previewHub(hubUri);

    // then
    assertThat(result).isEqualTo(expectedResponse);
    verify(rdfImportService).importRdfUrl(hubUri, false);
  }

  @Test
  void previewHub_shouldThrowException_whenNoMatchingResource() {
    // given
    var hubUri = "https://example.com/hub.json";
    var expectedException = new RequestProcessingException(404, "code", null, "message");

    when(rdfImportService.importRdfUrl(hubUri, false)).thenThrow(expectedException);

    // when & then
    assertThatThrownBy(() -> hubService.previewHub(hubUri))
      .isEqualTo(expectedException);
    verify(rdfImportService).importRdfUrl(hubUri, false);
  }
}
