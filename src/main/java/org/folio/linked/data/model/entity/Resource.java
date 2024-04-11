package org.folio.linked.data.model.entity;

import static jakarta.persistence.CascadeType.DETACH;
import static jakarta.persistence.CascadeType.REMOVE;
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
import jakarta.persistence.PostLoad;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import java.util.Arrays;
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
import org.springframework.data.domain.Persistable;

@Entity
@Data
@NoArgsConstructor
@Accessors(chain = true)
@Table(name = "resources")
@EqualsAndHashCode(of = "id")
public class Resource implements Persistable<Long> {

  @Id
  @Column(name = "resource_hash")
  private Long id;

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
  private Set<ResourceTypeEntity> types;

  @OrderBy
  @ToString.Exclude
  @OneToMany(mappedBy = "target", cascade = {DETACH, REMOVE}, orphanRemoval = true)
  private Set<ResourceEdge> incomingEdges;

  @OrderBy
  @ToString.Exclude
  @OneToMany(mappedBy = "source", cascade = {DETACH, REMOVE}, orphanRemoval = true)
  private Set<ResourceEdge> outgoingEdges;

  @Transient
  private boolean managed;

  public Resource(@NonNull Resource that) {
    this.id = that.id;
    this.label = that.label;
    this.doc = (JsonNode) ofNullable(that.getDoc()).map(JsonNode::deepCopy).orElse(null);
    this.inventoryId = that.inventoryId;
    this.srsId = that.srsId;
    this.indexDate = that.indexDate;
    this.types = new LinkedHashSet<>(that.getTypes());
    this.outgoingEdges = ofNullable(that.getOutgoingEdges())
      .map(outEdges -> outEdges.stream().map(ResourceEdge::new).collect(Collectors.toSet()))
      .orElse(null);
    this.incomingEdges = ofNullable(that.getIncomingEdges())
      .map(inEdges -> inEdges.stream()
        .map(
          ie -> this.getOutgoingEdges().stream().filter(oe -> oe.equals(ie)).findFirst().orElse(new ResourceEdge(ie)))
        .collect(Collectors.toSet()))
      .orElse(null);
  }

  public static Resource copyWithNoEdges(@NonNull Resource that) {
    return new Resource()
      .setId(that.id)
      .setLabel(that.label)
      .setDoc((JsonNode) ofNullable(that.getDoc()).map(JsonNode::deepCopy).orElse(null))
      .setInventoryId(that.inventoryId)
      .setSrsId(that.srsId)
      .setIndexDate(that.indexDate)
      .setTypes(new LinkedHashSet<>(that.getTypes()))
      .setIncomingEdges(new LinkedHashSet<>())
      .setOutgoingEdges(new LinkedHashSet<>());
  }

  @Override
  public boolean isNew() {
    return !managed;
  }

  @PostLoad
  @PrePersist
  void markManaged() {
    this.managed = true;
  }

  public Set<ResourceTypeEntity> getTypes() {
    if (isNull(types)) {
      types = new LinkedHashSet<>();
    }
    return types;
  }

  public Resource addType(@NonNull ResourceTypeEntity type) {
    getTypes().add(type);
    return this;
  }

  public Resource addTypes(@NonNull org.folio.ld.dictionary.ResourceTypeDictionary... typeDictionary) {
    Arrays.stream(typeDictionary)
      .map(type -> new ResourceTypeEntity(type.getHash(), type.getUri(), null))
      .forEach(this::addType);
    return this;
  }

  public Set<ResourceEdge> getOutgoingEdges() {
    if (isNull(outgoingEdges)) {
      outgoingEdges = new LinkedHashSet<>();
    }
    return outgoingEdges;
  }

  public Resource addOutgoingEdge(@NonNull ResourceEdge outgoingEdge) {
    getOutgoingEdges().add(outgoingEdge);
    return this;
  }

  public Set<ResourceEdge> getIncomingEdges() {
    if (isNull(incomingEdges)) {
      incomingEdges = new LinkedHashSet<>();
    }
    return incomingEdges;
  }

  public Resource addIncomingEdge(@NonNull ResourceEdge incomingEdge) {
    getIncomingEdges().add(incomingEdge);
    return this;
  }

}
