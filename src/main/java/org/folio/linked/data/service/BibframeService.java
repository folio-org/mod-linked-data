package org.folio.linked.data.service;

import org.folio.linked.data.domain.dto.BibframeCreateRequest;
import org.folio.linked.data.domain.dto.BibframeResponse;
import org.folio.linked.data.domain.dto.BibframeShortInfoPage;
import org.folio.linked.data.domain.dto.BibframeUpdateRequest;

public interface BibframeService {

  BibframeResponse createBibframe(BibframeCreateRequest bibframeCreateRequest, String okapiTenant);

  BibframeResponse getBibframeBySlug(String slug, String okapiTenant);

  BibframeResponse updateBibframe(String slug, BibframeUpdateRequest bibframeUpdateRequest, String okapiTenant);

  void deleteBibframe(String slug, String okapiTenant);

  BibframeShortInfoPage getBibframeShortInfoPage(Integer pageNumber, Integer pageSize);
}
