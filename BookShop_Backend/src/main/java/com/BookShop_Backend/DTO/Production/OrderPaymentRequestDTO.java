package com.BookShop_Backend.DTO.Production;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderPaymentRequestDTO {
    @JsonProperty("shipping_address")
    @JsonAlias("shippingAddress")
    @NotBlank(message = "shippingAddress is required")
    private String shippingAddress;

    @JsonProperty("phone_number")
    @JsonAlias({"phoneNumber", "phone"})
    @NotBlank(message = "phoneNumber is required")
    private String phoneNumber;

    @JsonProperty("return_url")
    @JsonAlias("returnUrl")
    private String returnUrl;

    @JsonProperty("cancel_url")
    @JsonAlias("cancelUrl")
    private String cancelUrl;
}
