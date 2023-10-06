package org.folio.linked.data.mapper.marc;

import org.folio.linked.data.model.entity.Resource;

public interface Marc2BibframeMapper {

  Resource map(String marc);
}
