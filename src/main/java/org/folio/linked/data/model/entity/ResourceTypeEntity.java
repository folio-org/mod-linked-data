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
import lombok.experimental.Accessors;
import org.folio.ld.dictionary.model.ResourceType;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Table(name = "type_lookup")
@EqualsAndHashCode(of = "hash")
public class ResourceTypeEntity implements ResourceType {

  @Id
  @Column(name = "type_hash")
  private Long hash;

  @NonNull
  @Column(name = "type_uri", nullable = false)
  private String uri;

  private String simpleLabel;

}
