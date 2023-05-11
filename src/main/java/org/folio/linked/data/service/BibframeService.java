package org.folio.linked.data.service;

import org.folio.linked.data.domain.dto.BibframeCreateRequest;
import org.folio.linked.data.domain.dto.BibframeResponse;

public interface BibframeService {

  BibframeResponse createBibframe(String okapiTenant, BibframeCreateRequest bibframeCreateRequest);

  BibframeResponse getBibframeBySlug(String slug);
}
