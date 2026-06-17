package com.BookShop_Backend.repositories;

import com.BookShop_Backend.enums.PaymentStatus;
import com.BookShop_Backend.models.OrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<OrderEntity, Long> {

    @Query("SELECT o FROM OrderEntity o WHERE FUNCTION('MONTH', o.createdAt) = :month AND o.paymentStatus = :paymentStatus")
    List<OrderEntity> findAllByMonthAndPaymentStatus(@Param("month") Integer month, @Param("paymentStatus") PaymentStatus paymentStatus);

    List<OrderEntity> findAllByUser_Id(Long userId);

    Optional<OrderEntity> findByOrderCode(Long orderCode);
}
