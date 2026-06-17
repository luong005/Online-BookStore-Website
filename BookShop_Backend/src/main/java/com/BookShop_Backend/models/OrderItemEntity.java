package com.BookShop_Backend.models;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(  name = "order_items",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"order_id", "book_id"})
        })
@Data // to string
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderItemEntity extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer quantity;

    private Double price; // giá tại thời điểm mua

    // nhiều item thuộc 1 order
    @ManyToOne
    @JoinColumn(name = "order_id")
    private OrderEntity order;

    // mỗi item là 1 book
    @ManyToOne
    @JoinColumn(name = "book_id")
    private BookEntity book;
}
