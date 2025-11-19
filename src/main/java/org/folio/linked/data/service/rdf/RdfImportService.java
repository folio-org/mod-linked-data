package org.folio.linked.data.service.rdf;

import java.time.LocalDateTime;
import org.folio.linked.data.domain.dto.ImportFileResponseDto;
import org.folio.linked.data.domain.dto.ImportOutputEvent;
import org.springframework.web.multipart.MultipartFile;

public interface RdfImportService {

  ImportFileResponseDto importFile(MultipartFile multipartFile);

  void importOutputEvent(ImportOutputEvent event, LocalDateTime startTime);

}
