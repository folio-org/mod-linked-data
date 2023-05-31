package org.folio.linked.data.model.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

@Entity
@Table(name = "type_lookup")
@Getter
@Setter
public class ResourceType {

  @Id
  private Long typeHash;

  @NonNull
  private String typeUri;

  @NonNull
  private String simpleLabel;
}
