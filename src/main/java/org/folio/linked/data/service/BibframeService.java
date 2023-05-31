package org.folio.linked.data.service;

import org.folio.linked.data.domain.dto.BibframeCreateRequest;
import org.folio.linked.data.domain.dto.BibframeResponse;
import org.folio.linked.data.domain.dto.BibframeShortInfoPage;
import org.folio.linked.data.domain.dto.BibframeUpdateRequest;

public interface BibframeService {

  BibframeResponse createBibframe(String okapiTenant, BibframeCreateRequest bibframeCreateRequest);

  BibframeResponse getBibframeById(String okapiTenant, Long id);

  BibframeResponse updateBibframe(String okapiTenant, Long id, BibframeUpdateRequest bibframeUpdateRequest);

  void deleteBibframe(String okapiTenant, Long id);

  BibframeShortInfoPage getBibframeShortInfoPage(String okapiTenant, Integer pageNumber, Integer pageSize);
}
