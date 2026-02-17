package com.tribiz.repository;

import com.tribiz.entity.Promotion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PromotionRepository extends JpaRepository<Promotion, Long> {

    /**
     * Find all promotions for a specific shop
     */
    List<Promotion> findByShopId(Long shopId);

    /**
     * Find all promotions for a specific product
     */
    List<Promotion> findByProductId(Long productId);

    /**
     * Find all promotions for a specific service
     */
    List<Promotion> findByServiceId(Long serviceId);

    /**
     * Find all promotions by status
     */
    List<Promotion> findByStatus(Promotion.PromotionStatus status);

    /**
     * Find active promotions for a shop that haven't expired
     */
    @Query("SELECT p FROM Promotion p WHERE p.shop.id = :shopId AND p.status = 'ACTIVE' AND p.endDate > :now")
    List<Promotion> findActivePromotionsByShop(@Param("shopId") Long shopId, @Param("now") LocalDateTime now);

    /**
     * Find promotions that apply to all items in a shop
     */
    List<Promotion> findByShopIdAndAppliesToAllTrue(Long shopId);

    /**
     * Find active promotions by promo code
     */
    @Query("SELECT p FROM Promotion p WHERE p.promoCode = :code AND p.status = 'ACTIVE' AND p.startDate <= :now AND p.endDate > :now")
    List<Promotion> findActiveByPromoCode(@Param("code") String code, @Param("now") LocalDateTime now);

    /**
     * Find promotions by type
     */
    List<Promotion> findByType(Promotion.PromotionType type);

    /**
     * Find expired promotions that are still marked as ACTIVE
     */
    @Query("SELECT p FROM Promotion p WHERE p.status = 'ACTIVE' AND p.endDate < :now")
    List<Promotion> findExpiredActivePromotions(@Param("now") LocalDateTime now);

    /**
     * Find scheduled promotions that should become active
     */
    @Query("SELECT p FROM Promotion p WHERE p.status = 'SCHEDULED' AND p.startDate <= :now AND p.endDate > :now")
    List<Promotion> findPromotionsToActivate(@Param("now") LocalDateTime now);

    /**
     * Count active promotions for a shop
     */
    long countByShopIdAndStatus(Long shopId, Promotion.PromotionStatus status);
}
