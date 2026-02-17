package com.tribiz.repository;

import com.tribiz.entity.Shop;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface ShopRepository extends JpaRepository<Shop, Long> {
    List<Shop> findByOwnerId(Long userId);

    List<Shop> findByLocationContaining(String location);

    List<Shop> findTop10ByOrderByRatingDesc();

    // Requires joining with Product entity which has 'discount' field
    // Assuming Shop has a OneToMany relationship 'products' or Product has
    // ManyToOne 'shop'
    // Spring Data JPA can derive this if the relationship is properly mapped
    List<Shop> findDistinctByProductsDiscountGreaterThan(BigDecimal products_discount);
}
