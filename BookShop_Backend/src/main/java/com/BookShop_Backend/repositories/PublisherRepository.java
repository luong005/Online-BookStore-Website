package com.BookShop_Backend.repositories;

import com.BookShop_Backend.models.PublisherEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PublisherRepository extends JpaRepository<PublisherEntity, Long> {
    Optional<PublisherEntity> findByNameIgnoreCase(String name);
}
