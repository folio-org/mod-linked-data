package org.folio.linked.data.service.hub;

import static org.apache.commons.lang3.StringUtils.substringAfterLast;
import static org.apache.commons.lang3.StringUtils.substringBefore;
import static org.folio.ld.dictionary.PropertyDictionary.LINK;
import static org.folio.linked.data.mapper.dto.resource.base.SingleResourceMapperUnit.ResourceMappingContext;
import static org.folio.linked.data.util.ResourceUtils.getPropertyValues;

import lombok.RequiredArgsConstructor;
import org.folio.linked.data.domain.dto.ResourceResponseDto;
import org.folio.linked.data.exception.RequestProcessingExceptionBuilder;
import org.folio.linked.data.integration.http.HttpClient;
import org.folio.linked.data.mapper.dto.resource.hub.HubMapperUnit;
import org.folio.linked.data.service.rdf.RdfImportService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class HubServiceImpl implements HubService {

  private final HttpClient httpClient;
  private final HubMapperUnit hubMapperUnit;
  private final RdfImportService rdfImportService;
  private final RequestProcessingExceptionBuilder requestProcessingExceptionBuilder;

  @Override
  public ResourceResponseDto previewHub(String hubUri) {
    return importHub(hubUri, false);
  }

  @Override
  public ResourceResponseDto saveHub(String hubUri) {
    return importHub(hubUri, true);
  }

  private ResourceResponseDto importHub(String hubUri, boolean save) {
    var jsonString = httpClient.downloadString(hubUri);
    var imported = rdfImportService.importRdfJsonString(jsonString, save);
    var id = substringBefore(substringAfterLast(hubUri, "/"), ".");
    return imported.stream()
      .filter(r -> getPropertyValues(r, LINK).stream().anyMatch(p -> p.contains(id)))
      .map(hubResource -> {
        var rmc = new ResourceMappingContext(null, null);
        return hubMapperUnit.toDto(hubResource, new ResourceResponseDto(), rmc);
      })
      .findFirst()
      .orElseThrow(() -> requestProcessingExceptionBuilder.notFoundHubByUriException(hubUri));
  }

}
