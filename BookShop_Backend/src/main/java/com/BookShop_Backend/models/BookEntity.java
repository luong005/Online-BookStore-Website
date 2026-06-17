package com.BookShop_Backend.models;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "books")
@Data // to string
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BookEntity extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Builder.Default
    @Column(name = "status")
    private Integer status = 1;

    private String name;

    private String content;

    private Double price;

    private Integer stock;

    private String imageUrl;

    // ===== RELATIONSHIP =====

    @ManyToOne
    @JoinColumn(name = "author_id")
    private AuthorEntity author;

    @ManyToOne
    @JoinColumn(name = "publisher_id")
    private PublisherEntity publisher;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private CategoryEntity category;

    @PrePersist
    private void prePersistStatus() {
        if (status == null) {
            status = 1;
        }
    }

    @PostLoad
    private void postLoadStatus() {
        if (status == null) {
            status = 1;
        }
    }
}
