package org.folio.linked.data.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "predicate_lookup")
@RequiredArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Predicate {

  @Id
  private Long predicateHash;

  @NonNull
  @Column(name = "predicate", nullable = false)
  private String label;

}
