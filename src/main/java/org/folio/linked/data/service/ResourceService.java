package org.folio.linked.data.service;

import org.folio.linked.data.domain.dto.Bibframe2Request;
import org.folio.linked.data.domain.dto.Bibframe2Response;
import org.folio.linked.data.domain.dto.BibframeRequest;
import org.folio.linked.data.domain.dto.BibframeResponse;
import org.folio.linked.data.domain.dto.BibframeShortInfoPage;

public interface ResourceService {

  BibframeResponse createBibframe(BibframeRequest bibframeRequest);

  BibframeResponse getBibframeById(Long id);

  BibframeResponse updateBibframe(Long id, BibframeRequest bibframeRequest);

  void deleteBibframe(Long id);

  BibframeShortInfoPage getBibframeShortInfoPage(String type, Integer pageNumber, Integer pageSize);

  Bibframe2Response createBibframe2(Bibframe2Request bibframeRequest);

  Bibframe2Response getBibframe2ById(Long id);

  Bibframe2Response updateBibframe2(Long id, Bibframe2Request bibframeUpdateRequest);

  BibframeShortInfoPage getBibframe2ShortInfoPage(Integer pageNumber, Integer pageSize);
}
