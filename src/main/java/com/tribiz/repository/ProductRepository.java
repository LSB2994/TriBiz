package com.tribiz.repository;

import com.tribiz.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

import java.math.BigDecimal;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByShopId(Long shopId);

    List<Product> findByCategory(String category);

    List<Product> findByNameContaining(String name);

    List<Product> findTop10ByOrderByRatingDesc();

    List<Product> findByDiscountGreaterThan(BigDecimal discount);
}
