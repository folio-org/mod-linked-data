package org.folio.linked.data.service.rdf;

import org.folio.linked.data.domain.dto.ImportEventResult;
import org.folio.linked.data.domain.dto.ImportFileResponseDto;
import org.folio.linked.data.domain.dto.ImportOutputEvent;
import org.springframework.web.multipart.MultipartFile;

public interface RdfImportService {

  ImportFileResponseDto importFile(MultipartFile multipartFile);

  ImportEventResult importOutputEvent(ImportOutputEvent event);

}
