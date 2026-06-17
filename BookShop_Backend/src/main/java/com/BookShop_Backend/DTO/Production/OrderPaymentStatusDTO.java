package com.BookShop_Backend.DTO.Production;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderPaymentStatusDTO {
    private Long orderId;

    @JsonProperty("order_code")
    private Long orderCode;

    @JsonProperty("payment_status")
    private String paymentStatus;

    @JsonProperty("payos_status")
    private String payosStatus;

    private String message;
}
