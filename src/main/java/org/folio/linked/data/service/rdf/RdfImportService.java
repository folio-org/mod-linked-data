package org.folio.linked.data.service.rdf;

import java.util.List;
import org.folio.ld.dictionary.model.Resource;
import org.folio.linked.data.domain.dto.ImportFileResponseDto;
import org.springframework.web.multipart.MultipartFile;

public interface RdfImportService {

  ImportFileResponseDto importFile(MultipartFile multipartFile);

  void saveImportEventResources(String ts, Long jobInstanceId, List<Resource> resources);

}
