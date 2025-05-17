package org.folio.linked.data.service.rdf;

import java.util.List;
import org.springframework.web.multipart.MultipartFile;

public interface RdfImportService {

  List<Long> importFile(MultipartFile multipartFile);

}
