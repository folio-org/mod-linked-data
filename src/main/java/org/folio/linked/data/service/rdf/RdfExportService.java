package org.folio.linked.data.service.rdf;

import org.folio.linked.data.domain.dto.RdfResourceDto;

public interface RdfExportService {

  RdfResourceDto exportInstanceToRdf(Long instanceId);

}
