package com.BookShop_Backend.DTO.Production;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class BookImportData {
    private int rowNumber;
    private BookDTO bookDTO;
}
