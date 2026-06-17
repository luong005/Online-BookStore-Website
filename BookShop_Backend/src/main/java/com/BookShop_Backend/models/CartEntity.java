package com.BookShop_Backend.models;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "cart")
@Data // to string
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CartEntity extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    private UserEntity user;
}
