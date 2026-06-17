package com.BookShop_Backend.services;

import com.BookShop_Backend.DTO.Production.BookDTO;
import com.BookShop_Backend.DTO.Production.UpdateBookRequestDTO;
import com.BookShop_Backend.DTO.Production.DeleteBooksResponseDTO;
import com.BookShop_Backend.DTO.Production.ImportBooksResponseDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface IBookService {
    List<BookDTO> searchBooks(String name, String category, Double minPrice, Double maxPrice);

    BookDTO searchBook(Long id);

    void updateBook(Long id, UpdateBookRequestDTO updateBookRequestDTO);

    void deleteBook(Long id);

    DeleteBooksResponseDTO deleteBooks(List<Long> ids);

    ImportBooksResponseDTO insertBooks(MultipartFile file);

    void insertBook(BookDTO bookDTO);
}
