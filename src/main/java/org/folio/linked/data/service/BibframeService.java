package org.folio.linked.data.service;

import org.folio.linked.data.domain.dto.BibframeRequest;
import org.folio.linked.data.domain.dto.BibframeResponse;

public interface BibframeService {

  BibframeResponse createBibframe(String okapiTenant, BibframeRequest bibframeRequest);

  BibframeResponse getBibframeBySlug(String okapiTenant, String slug);
}
