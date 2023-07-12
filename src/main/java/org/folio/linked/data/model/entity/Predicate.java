package org.folio.linked.data.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

@Data
@Entity
@NoArgsConstructor
@Accessors(chain = true)
@RequiredArgsConstructor
@Table(name = "predicate_lookup")
@EqualsAndHashCode(of = "predicateHash")
public class Predicate {

  @Id
  private Long predicateHash;

  @NonNull
  @Column(name = "predicate", nullable = false)
  private String label;

}
