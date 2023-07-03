package org.folio.linked.data.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.experimental.Accessors;

@Data
@Entity
@Table(name = "type_lookup")
@NoArgsConstructor
@Accessors(chain = true)
public class ResourceType {

  @Id
  private Long typeHash;

  @NonNull
  @Column(nullable = false)
  private String typeUri;

  private String simpleLabel;

}
