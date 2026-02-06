package org.folio.linked.data.service.hub;

import static org.folio.linked.data.mapper.dto.resource.base.SingleResourceMapperUnit.ResourceMappingContext;

import lombok.RequiredArgsConstructor;
import org.folio.linked.data.domain.dto.ResourceResponseDto;
import org.folio.linked.data.mapper.dto.resource.hub.HubMapperUnit;
import org.folio.linked.data.service.rdf.RdfImportService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class HubServiceImpl implements HubService {

  private final HubMapperUnit hubMapperUnit;
  private final RdfImportService rdfImportService;

  @Override
  public ResourceResponseDto previewHub(String hubUri) {
    return importHub(hubUri, false);
  }

  @Override
  public ResourceResponseDto saveHub(String hubUri) {
    return importHub(hubUri, true);
  }

  private ResourceResponseDto importHub(String hubUri, boolean save) {
    var hubResource = rdfImportService.importRdfFromUrl(hubUri, save);
    var rmc = new ResourceMappingContext(null, null);
    return hubMapperUnit.toDto(hubResource, new ResourceResponseDto(), rmc);
  }

}
