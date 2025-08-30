package com.example.sellify.repository;

import com.example.sellify.entity.Product;
import com.example.sellify.entity.enums.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product,Long> {
    List<Product> findByActiveFalse();

    @Query(value = "SELECT * FROM products WHERE category = :category ORDER BY RANDOM() LIMIT 20", nativeQuery = true)
    List<Product> findRandom20ByCategory(@Param("category") Category category);
}
