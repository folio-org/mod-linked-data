package org.folio.linked.data.service.rdf;

import java.time.OffsetDateTime;
import org.folio.linked.data.domain.dto.ImportOutputEvent;
import org.folio.linked.data.domain.dto.ImportResponseDto;
import org.folio.linked.data.model.entity.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface RdfImportService {

  ImportResponseDto importFile(MultipartFile multipartFile);

  ImportResponseDto importUrl(String url);

  void importOutputEvent(ImportOutputEvent event, OffsetDateTime startTime);

  Resource importRdfUrl(String rdfUrl, boolean save);

}
