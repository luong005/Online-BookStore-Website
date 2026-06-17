package com.BookShop_Backend.repositories;

import com.BookShop_Backend.models.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity,Long> {
    Optional<UserEntity> findByPhoneNumber(String phoneNumber);
    boolean existsByPhoneNumber(String phoneNumber);
    long countByRole_Id(Long roleId);

    @Query("""
            SELECT u
            FROM UserEntity u
            WHERE (:address IS NULL OR LOWER(u.address) LIKE LOWER(CONCAT('%', :address, '%')))
              AND (:roleId IS NULL OR u.role.id = :roleId)
            """)
    List<UserEntity> searchUsers(
            @Param("address") String address,
            @Param("roleId") Long roleId
    );
}
