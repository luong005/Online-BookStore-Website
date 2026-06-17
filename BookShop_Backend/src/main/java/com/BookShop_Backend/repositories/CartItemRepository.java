package com.BookShop_Backend.repositories;

import com.BookShop_Backend.models.CartItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CartItemRepository extends JpaRepository<CartItemEntity,Long> {
    List<CartItemEntity> findAllByCart_Id(Long cartId);
    void deleteAllByBook_Id(Long bookId);
}
