package com.tribiz.repository;

import com.tribiz.entity.ServiceItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

import java.math.BigDecimal;

@Repository
public interface ServiceItemRepository extends JpaRepository<ServiceItem, Long> {
    List<ServiceItem> findByShopId(Long shopId);

    List<ServiceItem> findTop10ByOrderByRatingDesc();

    List<ServiceItem> findByDiscountGreaterThan(BigDecimal discount);
}
