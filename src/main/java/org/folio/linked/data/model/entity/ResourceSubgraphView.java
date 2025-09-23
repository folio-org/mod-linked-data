package org.folio.linked.data.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "export_resources")
public class ResourceSubgraphView {

  @Id
  @Column(name = "resource_hash")
  private Long resourceHash;

  @Column(name = "inventory_id")
  private String inventoryId;

  @Column(name = "resource_subgraph")
  private String resourceSubgraph;

  public Long getResourceHash() {
    return resourceHash;
  }

  public void setResourceHash(Long resourceHash) {
    this.resourceHash = resourceHash;
  }

  public String getInventoryId() {
    return inventoryId;
  }

  public void setInventoryId(String inventoryId) {
    this.inventoryId = inventoryId;
  }

  public String getResourceSubgraph() {
    return resourceSubgraph;
  }

  public void setResourceSubgraph(String resourceSubgraph) {
    this.resourceSubgraph = resourceSubgraph;
  }
}

