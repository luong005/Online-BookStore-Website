package com.BookShop_Backend.DTO.Production;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateQuantityCartItemDTO {
    private Long id;
    private Integer quantity;
}
