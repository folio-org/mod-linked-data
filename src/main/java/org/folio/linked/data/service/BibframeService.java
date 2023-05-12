package org.folio.linked.data.service;

import java.util.UUID;
import org.folio.linked.data.domain.dto.BibframeCreateRequest;
import org.folio.linked.data.domain.dto.BibframeResponse;

public interface BibframeService {

  BibframeResponse createBibframe(String okapiTenant, BibframeCreateRequest bibframeCreateRequest);

  BibframeResponse getBibframeById(UUID id);
}
