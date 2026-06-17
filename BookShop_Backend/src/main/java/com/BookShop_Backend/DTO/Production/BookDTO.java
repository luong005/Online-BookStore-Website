package com.BookShop_Backend.DTO.Production;

import com.alibaba.excel.annotation.ExcelProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookDTO {
    private Long id;
    private Integer status;


    @NotBlank
    @ExcelProperty("name")
    private String name;

    @NotBlank
    @ExcelProperty("content")
    private String content;

    @NotNull
    @ExcelProperty("price")
    private Double price;

    @NotNull
    @ExcelProperty("stock")
    private Integer stock;

    @NotBlank
    @ExcelProperty("imageUrl")
    private String imageUrl;

    @NotBlank
    @ExcelProperty("categoryName")
    private String categoryName;

    @NotBlank
    @Size(max = 20)
    @ExcelProperty("authorName")
    private String authorName;

    @NotBlank
    @ExcelProperty("publisher")
    private String publisher;

}
