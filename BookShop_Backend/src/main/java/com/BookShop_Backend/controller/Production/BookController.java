package com.BookShop_Backend.controller.Production;

import com.BookShop_Backend.DTO.Production.BookDTO;
import com.BookShop_Backend.DTO.Production.DeleteBooksResponseDTO;
import com.BookShop_Backend.DTO.Production.ImportBooksResponseDTO;
import com.BookShop_Backend.DTO.Production.UpdateBookRequestDTO;

import com.BookShop_Backend.services.IBookService;
import com.BookShop_Backend.services.IOrderService;
import com.BookShop_Backend.type.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("${api.prefix}")
@RequiredArgsConstructor
public class BookController {
    private final IBookService bookService;
    private final IOrderService orderService;

    @GetMapping("/books/best-selling-{top}")
    public ResponseEntity<?> getBestSellingBooks(@PathVariable Integer top) {
        List<BookDTO> books = orderService.getBestSeller(Math.min(Math.max(top, 1), 20)).stream()
                .map(bestSeller -> bestSeller.getBookDTOS())
                .toList();
        return ResponseEntity.ok(ApiResponse.success(books));
    }

    @GetMapping("/books/search")
    public ResponseEntity<?> searchBooks(
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "minPrice", required = false) Double minPrice,
            @RequestParam(value = "maxPrice", required = false) Double maxPrice
    ) {
        List<BookDTO> books = bookService.searchBooks(name, category, minPrice, maxPrice);
        return ResponseEntity.ok(ApiResponse.success(books));
    }

    @GetMapping("/book-{id}")
    public ResponseEntity<?> getBook(@PathVariable("id") Long id) {
        BookDTO bookDTO = bookService.searchBook(id);
        return ResponseEntity.ok(ApiResponse.success(bookDTO));
    }

    @PostMapping("/books")
    public ResponseEntity<?> insertBooks(@RequestParam("file") MultipartFile file) {
        ImportBooksResponseDTO result = bookService.insertBooks(file);
        return ResponseEntity.ok(ApiResponse.success("Import sach thanh cong", result));
    }

    @PostMapping("/book")
    public ResponseEntity<?> insertBook(@RequestBody BookDTO bookDTO){
        bookService.insertBook(bookDTO);
        return ResponseEntity.ok("Them thanh cong");
    }

    @PatchMapping("/book-{id}")
    public ResponseEntity<?> updateBook(@PathVariable("id") Long id, @Valid @RequestBody UpdateBookRequestDTO updateBookRequestDTO) {
        bookService.updateBook(id, updateBookRequestDTO);
        return ResponseEntity.ok("Cap nhat thanh cong");
    }

    @DeleteMapping("/book-{id}")
    public ResponseEntity<?> deleteBook(@PathVariable Long id) {
        bookService.deleteBook(id);
        return ResponseEntity.ok("Xoa thanh cong");
    }

    @DeleteMapping("/books-{ids}")
    public ResponseEntity<?> deleteBooks(@PathVariable List<Long> ids) {
        DeleteBooksResponseDTO result = bookService.deleteBooks(ids);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

}
