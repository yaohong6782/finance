package com.rundown.financeTracking.repository;

import com.rundown.financeTracking.entity.Categories;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Categories, Long> {


    @Query("SELECT c FROM Categories c WHERE LOWER(c.type) = LOWER(:type)")
    Optional<Categories> findByType(String type);
}
