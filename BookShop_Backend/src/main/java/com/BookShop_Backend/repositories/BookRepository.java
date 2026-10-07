package com.BookShop_Backend.repositories;

import com.BookShop_Backend.models.BookEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
public interface BookRepository extends JpaRepository<BookEntity, Long> {
    @Query("""
            SELECT b
            FROM BookEntity b
            WHERE (:name IS NULL OR LOWER(b.name) LIKE LOWER(CONCAT('%', :name, '%')))
              AND (:category IS NULL OR LOWER(b.category.name) LIKE LOWER(CONCAT('%', :category, '%')))
              AND (:minPrice IS NULL OR b.price >= :minPrice)
              AND (:maxPrice IS NULL OR b.price <= :maxPrice)
            ORDER BY b.createdAt DESC, b.id DESC
            """)
    List<BookEntity> searchBooks(
            @Param("name") String name,
            @Param("category") String category,
            @Param("minPrice") Double minPrice,
            @Param("maxPrice") Double maxPrice
    );

    // Coalesce(value,default): value=null ->value = default
    @Query("""
            SELECT b
            FROM BookEntity b
            WHERE b.id = :id
              AND COALESCE(b.status, 1) = 1   
            """)
    Optional<BookEntity> findActiveById(@Param("id") Long id);


    // ktra sach theo ten co status=1 ton tai chua
    @Query("""
            SELECT CASE WHEN COUNT(b) > 0 THEN true ELSE false END
            FROM BookEntity b
            WHERE LOWER(b.name) = LOWER(:name)
              AND COALESCE(b.status, 1) = 1
            """)
    boolean existsActiveByName(@Param("name") String name);
}
