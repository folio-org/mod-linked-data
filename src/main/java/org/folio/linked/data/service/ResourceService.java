package org.folio.linked.data.service;

import org.folio.linked.data.domain.dto.Bibframe2Request;
import org.folio.linked.data.domain.dto.Bibframe2Response;
import org.folio.linked.data.domain.dto.BibframeShortInfoPage;

public interface ResourceService {

  Bibframe2Response createBibframe2(Bibframe2Request bibframeRequest);

  Bibframe2Response getBibframe2ById(Long id);

  Bibframe2Response updateBibframe2(Long id, Bibframe2Request bibframeUpdateRequest);

  void deleteBibframe2(Long id);

  BibframeShortInfoPage getBibframe2ShortInfoPage(Integer pageNumber, Integer pageSize);

}
