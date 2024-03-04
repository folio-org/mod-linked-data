package org.folio.linked.data.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import org.folio.ld.dictionary.model.Predicate;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@RequiredArgsConstructor
@Table(name = "predicate_lookup")
@EqualsAndHashCode(of = "hash")
public class PredicateEntity implements Predicate {

  @Id
  @Column(name = "predicate_hash")
  private Long hash;

  @NonNull
  @Column(name = "predicate", nullable = false)
  private String uri;

}
