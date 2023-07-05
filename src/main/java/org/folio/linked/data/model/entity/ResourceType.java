package org.folio.linked.data.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.experimental.Accessors;

@Data
@Entity
@NoArgsConstructor
@Accessors(chain = true)
@Table(name = "type_lookup")
@EqualsAndHashCode(of = "typeHash")
public class ResourceType {

  @Id
  private Long typeHash;

  @NonNull
  @Column(nullable = false)
  private String typeUri;

  private String simpleLabel;

}
