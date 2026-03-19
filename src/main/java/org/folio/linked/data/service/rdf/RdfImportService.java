package org.folio.linked.data.service.rdf;

import java.time.OffsetDateTime;
import org.folio.linked.data.domain.dto.ImportOutputEvent;
import org.folio.linked.data.domain.dto.ImportResponseDto;
import org.folio.linked.data.model.entity.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface RdfImportService {

  ImportResponseDto importFile(String filterType, MultipartFile multipartFile);

  ImportResponseDto importUrl(String url, String filterType, String defaultWorkType);

  void importOutputEvent(ImportOutputEvent event, OffsetDateTime startTime);

  Resource importRdfUrl(String rdfUrl, boolean save);

}
