package org.folio.linked.data.model.entity;

import static jakarta.persistence.CascadeType.ALL;
import static jakarta.persistence.CascadeType.DETACH;
import static jakarta.persistence.CascadeType.REMOVE;
import static jakarta.persistence.FetchType.EAGER;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.Optional.ofNullable;
import static org.folio.ld.dictionary.ResourceTypeDictionary.INSTANCE;

import com.fasterxml.jackson.databind.JsonNode;
import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.OrderBy;
import jakarta.persistence.PostLoad;
import jakarta.persistence.PostRemove;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.persistence.Version;
import java.sql.Timestamp;
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
import org.folio.ld.dictionary.ResourceTypeDictionary;
import org.folio.linked.data.configuration.audit.LinkedDataAuditEntityListener;
import org.folio.linked.data.validation.PrimaryTitleConstraint;
import org.folio.marc4ld.util.ResourceKind;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.domain.Persistable;

@Entity
@Data
@NoArgsConstructor
@PrimaryTitleConstraint
@Accessors(chain = true)
@Table(name = "resources")
@EqualsAndHashCode(of = "id")
@SuppressWarnings("javaarchitecture:S7027")
@EntityListeners(LinkedDataAuditEntityListener.class)
public class Resource implements Persistable<Long> {

  @Id
  @NonNull
  @Column(name = "resource_hash")
  private Long id;

  @NonNull
  @Column(nullable = false)
  private String label = "";

  @Column(columnDefinition = "json")
  @Type(JsonBinaryType.class)
  private JsonNode doc;

  private Date indexDate;

  private boolean active = true;

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

  @OneToOne(cascade = ALL, mappedBy = "resource", orphanRemoval = true)
  @PrimaryKeyJoinColumn
  private FolioMetadata folioMetadata;

  @Column(name = "created_date", updatable = false, nullable = false)
  private Timestamp createdDate;

  @UpdateTimestamp
  @Column(name = "updated_date", nullable = false)
  private Timestamp updatedDate;

  @CreatedBy
  @Column(name = "created_by")
  private UUID createdBy;

  @LastModifiedBy
  @Column(name = "updated_by")
  private UUID updatedBy;

  @Version
  @Column(name = "version", nullable = false)
  private long version;

  @Transient
  private boolean managed;

  public Resource(@NonNull Resource that) {
    this.id = that.id;
    this.label = that.label;
    this.doc = (JsonNode) ofNullable(that.getDoc()).map(JsonNode::deepCopy).orElse(null);
    this.folioMetadata = that.folioMetadata;
    this.indexDate = that.indexDate;
    this.types = new LinkedHashSet<>(that.getTypes());
    this.createdDate = that.createdDate;
    this.createdBy = that.createdBy;
    this.updatedDate = that.updatedDate;
    this.updatedBy = that.updatedBy;
    this.version = that.version;
    this.outgoingEdges = ofNullable(that.getOutgoingEdges())
      .map(outEdges -> outEdges.stream().map(ResourceEdge::new).collect(Collectors.toSet()))
      .orElse(null);
    this.incomingEdges = ofNullable(that.getIncomingEdges())
      .map(inEdges -> inEdges.stream().map(ResourceEdge::new).collect(Collectors.toSet()))
      .orElse(null);
  }

  protected void setId(Long id) {
    this.id = id;
  }

  public Resource setLabel(String label) {
    if (nonNull(label)) {
      this.label = label;
    }
    return this;
  }

  @Override
  public boolean isNew() {
    return !managed;
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

  public boolean isNotOfType(ResourceTypeDictionary type) {
    return !isOfType(type);
  }

  public boolean isOfType(ResourceTypeDictionary type) {
    return getTypes().stream().anyMatch(at -> at.getUri().equals(type.getUri()));
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

  @PostLoad
  void postLoad() {
    this.managed = true;
  }

  @PrePersist
  void prePersist() {
    this.managed = true;
    if (nonNull(folioMetadata) && !isOfType(INSTANCE) && !isAuthority()) {
      throw new IllegalStateException("Cannot save resource [" + id + "] with types " + types + ". "
        + "Folio metadata can be set only for instance and authority resources");
    }
    if (isNull(this.createdDate)) {
      this.createdDate = new Timestamp(System.currentTimeMillis());
    }
  }

  @PostRemove
  void postRemove() {
    this.managed = false;
  }

  public boolean isAuthority() {
    return ResourceKind.AUTHORITY
      .stream()
      .anyMatch(this::isOfType);
  }

  public Resource setIdAndRefreshEdges(@NonNull Long id) {
    this.id = id;

    if (nonNull(outgoingEdges)) {
      this.outgoingEdges = new LinkedHashSet<>(outgoingEdges);
    }

    if (nonNull(incomingEdges)) {
      this.incomingEdges = new LinkedHashSet<>(incomingEdges);
    }

    return this;
  }
}
