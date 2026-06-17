package com.BookShop_Backend.DTO.Production;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponseDTO {
    private Long id;
    private String shipping_address;
    private String phone_number;
    private Double total_price;
    private String payment_status;
    private String order_code;
    private LocalDateTime order_date;
}
