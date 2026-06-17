package com.BookShop_Backend.models;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table( name = "cart_items",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"cart_id", "book_id"})
        })
@Data // to string
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CartItemEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer quantity;

    @ManyToOne
    private CartEntity cart;

    @ManyToOne
    private BookEntity book;
}
