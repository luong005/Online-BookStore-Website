package com.BookShop_Backend.repositories;

import com.BookShop_Backend.models.AuthorEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AuthorRepository extends JpaRepository<AuthorEntity, Long> {
    Optional<AuthorEntity> findByNameIgnoreCase(String name);
}
