package com.BookShop_Backend.DTO.Production;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImportBooksResponseDTO {
    private int insertedCount;
    private int failedCount;
    private String errorFilePath;
}
