package org.folio.linked.data.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.experimental.Accessors;
import org.hibernate.annotations.Immutable;

@Data
@Entity
@Table(name = "export_resources")
@Immutable
@Accessors(chain = true)
public class ResourceSubgraphView {

  @Id
  @Column(name = "resource_hash")
  private Long resourceHash;

  @Column(name = "inventory_id")
  private String inventoryId;

  @Column(name = "resource_subgraph")
  private String resourceSubgraph;
}
