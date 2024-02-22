package org.folio.linked.data.model.entity;

import static jakarta.persistence.CascadeType.ALL;
import static jakarta.persistence.FetchType.EAGER;
import static java.util.Objects.isNull;
import static java.util.Optional.ofNullable;

import com.fasterxml.jackson.databind.JsonNode;
import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.hibernate.annotations.Type;

@Entity
@Data
@NoArgsConstructor
@Accessors(chain = true)
@Table(name = "resources")
@EqualsAndHashCode(of = "resourceHash")
public class Resource {

  @Id
  private Long resourceHash;

  @NonNull
  @Column(nullable = false)
  private String label;

  @Column(columnDefinition = "json")
  @Type(JsonBinaryType.class)
  private JsonNode doc;

  private UUID inventoryId;

  private UUID srsId;

  private Date indexDate;

  @OrderBy
  @ManyToMany(fetch = EAGER)
  @JoinTable(
    name = "resource_type_map",
    joinColumns = @JoinColumn(name = "resource_hash"),
    inverseJoinColumns = @JoinColumn(name = "type_hash")
  )
  private Set<ResourceTypeEntity> types = new LinkedHashSet<>();

  @OrderBy
  @ToString.Exclude
  @OneToMany(mappedBy = "target", cascade = ALL, orphanRemoval = true)
  private Set<ResourceEdge> incomingEdges = new LinkedHashSet<>();

  @OrderBy
  @ToString.Exclude
  @OneToMany(mappedBy = "source", cascade = ALL, orphanRemoval = true)
  private Set<ResourceEdge> outgoingEdges = new LinkedHashSet<>();

  public Resource(@NonNull Resource that) {
    this.resourceHash = that.resourceHash;
    this.label = that.label;
    this.doc = (JsonNode) ofNullable(that.getDoc()).map(JsonNode::deepCopy).orElse(null);
    this.inventoryId = that.inventoryId;
    this.srsId = that.srsId;
    this.indexDate = that.indexDate;
    this.types = new LinkedHashSet<>(that.getTypes());
    this.outgoingEdges = that.getOutgoingEdges().stream().map(ResourceEdge::new).collect(Collectors.toSet());
    this.incomingEdges = that.getIncomingEdges().stream()
      .map(ie -> this.getOutgoingEdges().stream()
        .filter(oe -> oe.equals(ie))
        .findFirst()
        .orElse(new ResourceEdge(ie)))
      .collect(Collectors.toSet());
  }

  public Resource addType(@NonNull ResourceTypeEntity type) {
    if (isNull(types)) {
      types = new LinkedHashSet<>();
    }
    types.add(type);
    return this;
  }

  public Resource addType(@NonNull org.folio.ld.dictionary.ResourceTypeDictionary typeDictionary) {
    this.addType(new ResourceTypeEntity(typeDictionary.getHash(), typeDictionary.getUri(), null));
    return this;
  }

}
