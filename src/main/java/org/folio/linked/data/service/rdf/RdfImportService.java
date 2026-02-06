package org.folio.linked.data.service.rdf;

import java.time.OffsetDateTime;
import java.util.Set;
import org.folio.linked.data.domain.dto.ImportFileResponseDto;
import org.folio.linked.data.domain.dto.ImportOutputEvent;
import org.folio.linked.data.model.entity.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface RdfImportService {

  ImportFileResponseDto importFile(MultipartFile multipartFile);

  void importOutputEvent(ImportOutputEvent event, OffsetDateTime startTime);

  Set<Resource> importRdfJsonString(String rdfJson, Boolean save);

}
