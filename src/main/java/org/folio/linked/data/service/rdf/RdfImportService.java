package org.folio.linked.data.service.rdf;

import java.util.Set;
import org.springframework.web.multipart.MultipartFile;

public interface RdfImportService {

  Set<Long> importFile(MultipartFile multipartFile);

}
