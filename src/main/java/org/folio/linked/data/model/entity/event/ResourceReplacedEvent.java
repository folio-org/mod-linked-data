package org.folio.linked.data.model.entity.event;

import org.folio.linked.data.model.entity.Resource;

public record ResourceReplacedEvent(Resource previous, Long currentResourceId) implements ResourceEvent {
}
