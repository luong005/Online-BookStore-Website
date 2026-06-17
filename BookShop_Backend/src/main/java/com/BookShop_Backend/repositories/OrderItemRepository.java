package com.BookShop_Backend.repositories;

import com.BookShop_Backend.enums.PaymentStatus;
import com.BookShop_Backend.models.OrderItemEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItemEntity, Long> {

    @Query("SELECT oi.book.id, SUM(oi.quantity) " +
            "FROM OrderItemEntity oi " +
            "WHERE oi.order.paymentStatus = :paymentStatus " +
            "GROUP BY oi.book.id " +
            "ORDER BY SUM(oi.quantity) DESC")
    List<Object[]> findTop10BestSellingBookIds(@Param("paymentStatus") PaymentStatus paymentStatus, Pageable pageable);

    List<OrderItemEntity> findAllByOrder_Id(Long orderId);

}
