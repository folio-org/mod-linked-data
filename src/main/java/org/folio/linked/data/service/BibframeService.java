package org.folio.linked.data.service;

import org.folio.linked.data.domain.dto.BibframeCreateRequest;
import org.folio.linked.data.domain.dto.BibframeResponse;
import org.folio.linked.data.domain.dto.BibframeShortInfoPage;
import org.folio.linked.data.domain.dto.BibframeUpdateRequest;

public interface BibframeService {

  BibframeResponse createBibframe(String okapiTenant, BibframeCreateRequest bibframeCreateRequest);

  BibframeResponse getBibframeBySlug(String okapiTenant, String slug);

  BibframeResponse updateBibframe(String okapiTenant, String slug, BibframeUpdateRequest bibframeUpdateRequest);

  void deleteBibframe(String okapiTenant, String slug);

  BibframeShortInfoPage getBibframeShortInfoPage(String okapiTenant, Integer pageNumber, Integer pageSize);
}
