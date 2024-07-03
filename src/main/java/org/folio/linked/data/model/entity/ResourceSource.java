package org.folio.linked.data.model.entity;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum ResourceSource {
  LINKED_DATA("LINKED_DATA"),
  MARC("MARC");

  private final String value;
}
