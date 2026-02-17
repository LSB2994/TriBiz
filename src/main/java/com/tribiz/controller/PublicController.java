package com.tribiz.controller;

import com.tribiz.dto.response.EventResponse;
import com.tribiz.dto.response.ProductResponse;
import com.tribiz.dto.response.ServiceResponse;
import com.tribiz.dto.response.ShopResponse;
import com.tribiz.dto.response.UserResponse;
import com.tribiz.entity.Event;
import com.tribiz.entity.Product;
import com.tribiz.entity.Role;
import com.tribiz.entity.ServiceItem;
import com.tribiz.entity.Shop;
import com.tribiz.entity.User;
import com.tribiz.repository.EventRepository;
import com.tribiz.repository.ProductRepository;
import com.tribiz.repository.ServiceItemRepository;
import com.tribiz.repository.ShopRepository;
import com.tribiz.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/public")
public class PublicController {

    @Autowired
    ShopRepository shopRepository;

    @Autowired
    ProductRepository productRepository;

    @Autowired
    ServiceItemRepository serviceItemRepository;

    @Autowired
    EventRepository eventRepository;

    @Autowired
    UserRepository userRepository;

    @GetMapping("/shops")
    public List<ShopResponse> getAllShops() {
        return shopRepository.findAll().stream()
                .map(shop -> ShopResponse.builder()
                        .id(shop.getId())
                        .name(shop.getName())
                        .location(shop.getLocation())
                        .contactInfo(shop.getContactInfo())
                        .isOpen(shop.getIsOpen())
                        .build())
                .collect(Collectors.toList());
    }

    @GetMapping("/shops/{id}")
    public ShopResponse getShopById(@PathVariable("id") Long id) {
        Shop shop = shopRepository.findById(id).orElseThrow(() -> new RuntimeException("Error: Shop is not found."));
        return mapToShopResponse(shop);
    }

    @GetMapping("/shops/popular")
    public List<ShopResponse> getPopularShops() {
        return shopRepository.findTop10ByOrderByRatingDesc().stream()
                .map(this::mapToShopResponse)
                .collect(Collectors.toList());
    }

    @GetMapping("/shops/promotions")
    public List<ShopResponse> getPromotionShops() {
        return shopRepository.findDistinctByProductsDiscountGreaterThan(BigDecimal.valueOf(0.0)).stream()
                .map(this::mapToShopResponse)
                .collect(Collectors.toList());
    }

    @GetMapping("/shops/nearby")
    public List<ShopResponse> getNearbyShops(@RequestParam("lat") Double lat, @RequestParam("lng") Double lng) {
        // Simple in-memory sort for H2. In production with PostGIS, use a spatial
        // query.
        return shopRepository.findAll().stream()
                .sorted((s1, s2) -> {
                    double d1 = calculateDistance(lat, lng, s1.getLatitude(), s1.getLongitude());
                    double d2 = calculateDistance(lat, lng, s2.getLatitude(), s2.getLongitude());
                    return Double.compare(d1, d2);
                })
                .limit(10)
                .map(this::mapToShopResponse)
                .collect(Collectors.toList());
    }

    private double calculateDistance(Double lat1, Double lon1, Double lat2, Double lon2) {
        if (lat1 == null || lon1 == null || lat2 == null || lon2 == null)
            return Double.MAX_VALUE;
        double earthRadius = 6371; // km
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return earthRadius * c;
    }

    private ShopResponse mapToShopResponse(Shop shop) {
        return ShopResponse.builder()
                .id(shop.getId())
                .name(shop.getName())
                .location(shop.getLocation())
                .contactInfo(shop.getContactInfo())
                .isOpen(shop.getIsOpen())
                .rating(shop.getRating())
                .reviewCount(shop.getReviewCount())
                .latitude(shop.getLatitude())
                .longitude(shop.getLongitude())
                .build();
    }

    @GetMapping("/products")
    public List<ProductResponse> getAllProducts() {
        return productRepository.findAll().stream()
                .map(this::mapToProductResponse)
                .collect(Collectors.toList());
    }

    @GetMapping("/products/{id}")
    public ProductResponse getProductById(@PathVariable("productId") Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Error: Product is not found."));
        return mapToProductResponse(product);
    }

    @GetMapping("/products/shop/{shopId}")
    public List<ProductResponse> getProductsByShop(@PathVariable("shopId") Long shopId) {
        return productRepository.findByShopId(shopId).stream()
                .map(this::mapToProductResponse)
                .collect(Collectors.toList());
    }

    @GetMapping("/products/category/{category}")
    public List<ProductResponse> getProductsByCategory(@PathVariable("category") String category) {
        return productRepository.findByCategory(category).stream()
                .map(this::mapToProductResponse)
                .collect(Collectors.toList());
    }

    @GetMapping("/products/popular")
    public List<ProductResponse> getPopularProducts() {
        return productRepository.findTop10ByOrderByRatingDesc().stream()
                .map(this::mapToProductResponse)
                .collect(Collectors.toList());
    }

    @GetMapping("/products/promotions")
    public List<ProductResponse> getPromotionProducts() {
        return productRepository.findByDiscountGreaterThan(java.math.BigDecimal.ZERO).stream()
                .map(this::mapToProductResponse)
                .collect(Collectors.toList());
    }

    @GetMapping("/services")
    public List<ServiceResponse> getAllServices() {
        return serviceItemRepository.findAll().stream()
                .map(this::mapToServiceResponse)
                .collect(Collectors.toList());
    }

    @GetMapping("/services/{id}")
    public ServiceResponse getServiceById(@PathVariable("id") Long id) {
        ServiceItem service = serviceItemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Error: Service is not found."));
        return mapToServiceResponse(service);
    }

    @GetMapping("/services/shop/{shopId}")
    public List<ServiceResponse> getServicesByShopId(@PathVariable("shopId") Long shopId) {
        return serviceItemRepository.findByShopId(shopId).stream()
                .map(this::mapToServiceResponse)
                .collect(Collectors.toList());
    }

    @GetMapping("/services/popular")
    public List<ServiceResponse> getPopularServices() {
        return serviceItemRepository.findTop10ByOrderByRatingDesc().stream()
                .map(this::mapToServiceResponse)
                .collect(Collectors.toList());
    }

    @GetMapping("/services/promotions")
    public List<ServiceResponse> getPromotionServices() {
        return serviceItemRepository.findByDiscountGreaterThan(java.math.BigDecimal.ZERO).stream()
                .map(this::mapToServiceResponse)
                .collect(Collectors.toList());
    }

    @GetMapping("/events")
    public List<EventResponse> getAllEvents() {
        return eventRepository.findAll().stream()
                .map(this::mapToEventResponse)
                .collect(Collectors.toList());
    }

    @GetMapping("/events/shop/{shopId}")
    public List<EventResponse> getEventsByShop(@PathVariable(name = "shopId") Long shopId) {
        return eventRepository.findByShopId(shopId).stream()
                .map(this::mapToEventResponse)
                .collect(Collectors.toList());
    }

    @GetMapping("/events/{id}")
    public EventResponse getEventById(@PathVariable Long id) {
        Event event = eventRepository.findById(id).orElseThrow(() -> new RuntimeException("Error: Event is not found."));
        return mapToEventResponse(event);
    }

    @GetMapping("/users/me")
    public UserResponse getCurrentUser(Authentication authentication) {
        if (authentication == null) {
            throw new RuntimeException("User not authenticated");
        }
        
        String email;
        User user;
        
        // Handle both OAuth2 and JWT authentication
        if (authentication.getPrincipal() instanceof OAuth2User) {
            // OAuth2 user
            OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
            email = oauth2User.getAttribute("email");
        } else {
            // JWT user
            email = authentication.getName();
        }
        
        user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        return mapToUserResponse(user);
    }

    private UserResponse mapToUserResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setFirstName(user.getFirstName());
        response.setLastName(user.getLastName());
        response.setRoles(user.getRoles() != null ? 
            user.getRoles().stream().map(Role::name).collect(java.util.stream.Collectors.toList()) : 
            java.util.Collections.singletonList("CUSTOMER"));
        return response;
    }

    private ProductResponse mapToProductResponse(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .quantity(product.getQuantity())
                .image(product.getImage())
                .status(product.getStatus())
                .category(product.getCategory())
                .discount(product.getDiscount())
                .buyOneGetOne(product.getBuyOneGetOne())
                .shopId(product.getShop() != null ? product.getShop().getId() : null)
                .shopName(product.getShop() != null ? product.getShop().getName() : null)
                .rating(product.getRating())
                .reviewCount(product.getReviewCount())
                .build();
    }

    private ServiceResponse mapToServiceResponse(ServiceItem service) {
        return ServiceResponse.builder()
                .id(service.getId())
                .name(service.getName())
                .description(service.getDescription())
                .price(service.getPrice())
                .durationMinutes(service.getDurationMinutes())
                .status(service.getStatus())
                .shopId(service.getShop() != null ? service.getShop().getId() : null)
                .shopName(service.getShop() != null ? service.getShop().getName() : null)
                .rating(service.getRating())
                .reviewCount(service.getReviewCount())
                .discount(service.getDiscount())
                .build();
    }

    private EventResponse mapToEventResponse(Event event) {
        return EventResponse.builder()
                .id(event.getId())
                .title(event.getTitle())
                .description(event.getDescription())
                .image(event.getImage())
                .startDate(event.getStartDate())
                .endDate(event.getEndDate())
                .location(event.getLocation())
                .shopId(event.getShop() != null ? event.getShop().getId() : null)
                .shopName(event.getShop() != null ? event.getShop().getName() : null)
                .build();
    }
}   