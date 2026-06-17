package com.BookShop_Backend.DTO.Production;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartCheckOutDTO {
    private List<CartItemDTO> cartItemDTOS;
    private Double totalPrice;
}
