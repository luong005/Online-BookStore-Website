package com.BookShop_Backend.repositories;

import com.BookShop_Backend.models.RoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<RoleEntity, Long> {
}
