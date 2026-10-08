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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponentsBuilder;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@RestController
@RequestMapping("${api.prefix}")
@RequiredArgsConstructor
public class BookController {
    private final IBookService bookService;
    private final IOrderService orderService;

    @Value("${app.public-base-url:}")
    private String publicBaseUrl;

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
        attachImportErrorDownloadUrl(result);
        return ResponseEntity.ok(ApiResponse.success("Import sach thanh cong", result));
    }

    @GetMapping("/books/import-errors/{fileName}")
    public ResponseEntity<Resource> downloadImportErrors(@PathVariable String fileName) {
        if (!fileName.matches("books_import_errors_\\d{8}_\\d{6}\\.xlsx")) {
            return ResponseEntity.notFound().build();
        }
        Path directory = Paths.get("import-errors").toAbsolutePath().normalize();
        Path file = directory.resolve(fileName).normalize();
        if (!file.startsWith(directory) || !Files.isRegularFile(file)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(new FileSystemResource(file));
    }

    private void attachImportErrorDownloadUrl(ImportBooksResponseDTO result) {
        String fileName = result.getErrorFilePath();
        if (fileName == null || fileName.isBlank()) {
            return;
        }
        result.setErrorFilePath(buildImportErrorDownloadUrl(fileName));
    }

    private String buildImportErrorDownloadUrl(String fileName) {
        if (publicBaseUrl != null && !publicBaseUrl.isBlank()) {
            String normalizedBaseUrl = publicBaseUrl.trim().replaceAll("/+$", "");
            return UriComponentsBuilder.fromUriString(normalizedBaseUrl)
                    .path("/api/books/import-errors/")
                    .path(fileName)
                    .toUriString();
        }

        return ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/books/import-errors/")
                .path(fileName)
                .toUriString();
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
