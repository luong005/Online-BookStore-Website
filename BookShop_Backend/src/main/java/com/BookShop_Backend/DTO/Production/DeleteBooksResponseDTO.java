package com.BookShop_Backend.DTO.Production;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class DeleteBooksResponseDTO {
    private List<Long> deletedIds;
    private List<Long> notFoundIds;
}

