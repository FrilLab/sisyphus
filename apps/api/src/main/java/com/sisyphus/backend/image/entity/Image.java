package com.sisyphus.backend.image.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;


@Getter
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "image_type")
@Entity
@NoArgsConstructor
@SQLRestriction("deleted_at IS NULL") // deletedAt이 있는 image는 제외
public abstract class Image {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String url;

    @Column(name = "origin_name", nullable = false)
    private String originName;

    @Column(name = "extension", nullable = false)
    private String extension;

    @Column(name = "size", nullable = false)
    private Long size;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void prePersist() {
        if (this.createdAt == null) this.createdAt = LocalDateTime.now();
    }

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    protected Image(String url, String originName, String extension, Long size) {
        this.url = url;
        this.originName = originName;
        this.extension = extension;
        this.size = size;
    }

    public void update(String url, String originName, String extension, Long size) {
        this.url = url;
        this.originName = originName;
        this.extension = extension;
        this.size = size;
    }
}
