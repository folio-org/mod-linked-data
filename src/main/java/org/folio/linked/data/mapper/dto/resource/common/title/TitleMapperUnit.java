package org.folio.linked.data.mapper.dto.resource.common.title;

import static java.util.stream.Collectors.joining;
import static org.folio.linked.data.util.ResourceUtils.getFirstValue;

import java.util.Set;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;
import org.folio.linked.data.domain.dto.BaseTitle;
import org.folio.linked.data.domain.dto.HubRequest;
import org.folio.linked.data.domain.dto.HubResponse;
import org.folio.linked.data.domain.dto.InstanceRequest;
import org.folio.linked.data.domain.dto.InstanceResponse;
import org.folio.linked.data.domain.dto.WorkRequest;
import org.folio.linked.data.domain.dto.WorkResponse;
import org.folio.linked.data.mapper.dto.resource.base.SingleResourceMapperUnit;

public abstract class TitleMapperUnit implements SingleResourceMapperUnit {

  private static final Set<Class<?>> SUPPORTED_PARENTS = Set.of(
    InstanceRequest.class,
    InstanceResponse.class,
    WorkRequest.class,
    WorkResponse.class,
    HubRequest.class,
    HubResponse.class
  );

  @Override
  public Set<Class<?>> supportedParents() {
    return SUPPORTED_PARENTS;
  }

  protected String getLabel(BaseTitle title) {
    var mainTitle = getFirstValue(title::getMainTitle);
    var subTitle = getFirstValue(title::getSubTitle);
    var partNumber = getFirstValue(title::getPartNumber);
    var partName = getFirstValue(title::getPartName);

    return Stream.of(mainTitle, subTitle, partNumber, partName)
      .filter(StringUtils::isNotBlank)
      .collect(joining(" "));
  }
}
