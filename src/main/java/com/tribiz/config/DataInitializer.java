package com.tribiz.config;

import com.tribiz.entity.*;
import com.tribiz.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

@Component
public class DataInitializer implements CommandLineRunner {

        private final UserRepository userRepository;
        private final ShopRepository shopRepository;
        private final ProductRepository productRepository;
        private final ServiceItemRepository serviceItemRepository;
        private final EventRepository eventRepository;
        private final PromotionRepository promotionRepository;
        private final PasswordEncoder passwordEncoder;

        public DataInitializer(UserRepository userRepository,
                        ShopRepository shopRepository,
                        ProductRepository productRepository,
                        ServiceItemRepository serviceItemRepository,
                        EventRepository eventRepository,
                        PromotionRepository promotionRepository,
                        PasswordEncoder passwordEncoder) {
                this.userRepository = userRepository;
                this.shopRepository = shopRepository;
                this.productRepository = productRepository;
                this.serviceItemRepository = serviceItemRepository;
                this.eventRepository = eventRepository;
                this.promotionRepository = promotionRepository;
                this.passwordEncoder = passwordEncoder;
        }

        @Override
        public void run(String... args) throws Exception {
                if (userRepository.count() == 0) {
                        seedData();
                }
        }

        private void seedData() {
                // --- 1. Create Users ---
                String encodedPassword = passwordEncoder.encode("password123");

                User john = User.builder()
                                .firstName("John").lastName("Seller").username("john_seller").email("seller@tribiz.com")
                                .password(encodedPassword)
                                .roles(Set.of(Role.SELLER)).location("Phnom Penh")
                                .dateOfBirth(LocalDate.of(1985, 5, 15)).build();
                userRepository.save(john);

                User sarah = User.builder()
                                .firstName("Sarah").lastName("Provider").username("sarah_pro").email("sarah@tribiz.com")
                                .password(encodedPassword)
                                .roles(Set.of(Role.SERVICE_PROVIDER)).location("Siem Reap")
                                .dateOfBirth(LocalDate.of(1992, 8, 20)).build();
                userRepository.save(sarah);

                User admin = User.builder()
                                .firstName("Admin").lastName("User").username("admin").email("cabad62310@flemist.com")
                                .password(passwordEncoder.encode("admin123"))
                                .roles(Set.of(Role.ADMIN)).location("California").dateOfBirth(LocalDate.of(1990, 1, 1))
                                .build();
                userRepository.save(admin);

                User mony = User.builder()
                                .firstName("Mony").lastName("Electronics").username("mony_electro")
                                .email("mony@khmer24.com").password(encodedPassword)
                                .roles(Set.of(Role.SELLER)).location("Phnom Penh").build();
                userRepository.save(mony);
        
                User leakhena = User.builder()
                                .firstName("Leakhena").lastName("Fashion").username("leak_style")
                                .email("leakhena@style.com").password(encodedPassword)
                                .roles(Set.of(Role.SELLER)).location("Battambang").build();
                userRepository.save(leakhena);

                // --- 2. Create Shops ---
                Shop techHub = Shop.builder().name("Mony Electronics Hub").location("Phnom Penh, St. 271")
                                .description("Premium electronics and gadgets from top global brands.")
                                .image("https://images.unsplash.com/photo-1567401893414-76b7b1e5a7a5?auto=format&fit=crop&q=80&w=800")
                                .contactInfo("012 345 678").isOpen(true).owner(mony).status("APPROVED").build();
                shopRepository.save(techHub);

                Shop royalFashion = Shop.builder().name("Royal Fashion & Beauty").location("Battambang Town")
                                .description("Your destination for high-end fashion and traditional elegance.")
                                .image("https://images.unsplash.com/photo-1441986300917-64674bd600d8?auto=format&fit=crop&q=80&w=800")
                                .contactInfo("098 765 432").isOpen(true).owner(leakhena).build();
                shopRepository.save(royalFashion);

                Shop spa = Shop.builder().name("Sarah Wellness Spa").location("Siem Reap, Near Pub Street")
                                .description("Relax and rejuvenate with our signature Khmer herbal treatments.")
                                .image("https://images.unsplash.com/photo-1544161515-4af6b1d46bdc?auto=format&fit=crop&q=80&w=800")
                                .contactInfo("011 223 344").isOpen(true).owner(sarah).status("PENDING").build();
                shopRepository.save(spa);

                Shop techFix = Shop.builder().name("TechFix Solutions").location("Phnom Penh, Ground Floor")
                                .description("Expert repair and maintenance for all your digital devices.")
                                .image("https://images.unsplash.com/photo-1597733336794-12d05021d510?auto=format&fit=crop&q=80&w=800")
                                .contactInfo("info@techfix.com").isOpen(true).owner(sarah).status("APPROVED").build();
                shopRepository.save(techFix);

                Shop urbanDecor = Shop.builder().name("The Urban Decor").location("Phnom Penh, Aeon Mall")
                                .description("Modern furniture and home accessories for the contemporary lifestyle.")
                                .image("https://images.unsplash.com/photo-1555041469-a586c61ea9bc?auto=format&fit=crop&q=80&w=800")
                                .contactInfo("015 999 888").isOpen(true).owner(john).status("PENDING").build();
                shopRepository.save(urbanDecor);

                // --- 3. Create Products (Electronics & Fashion) ---
                // Tech Hub
                productRepository.save(Product.builder().name("MacBook Pro M3 Max 14\"")
                                .description("Latest Apple Silicon, Professional performance.")
                                .price(new BigDecimal("3299.00")).quantity(5).category("Electronics")
                                .status("Available").shop(techHub)
                                .image("https://images.unsplash.com/photo-1517336714731-489689fd1ca8?auto=format&fit=crop&q=80&w=800")
                                .build());
                productRepository.save(Product.builder().name("iPhone 15 Pro Max")
                                .description("Titanium body, Advanced camera system.").price(new BigDecimal("1199.00"))
                                .quantity(10).category("Electronics").status("Available").shop(techHub)
                                .image("https://images.unsplash.com/photo-1510557880182-3d4d3cba35a5?auto=format&fit=crop&q=80&w=800")
                                .build());
                productRepository.save(Product.builder().name("Sony WH-1000XM5")
                                .description("Industry-leading noise canceling headphones.")
                                .price(new BigDecimal("399.00")).quantity(15).category("Accessories")
                                .status("Available").shop(techHub)
                                .image("https://images.unsplash.com/photo-1505740420928-5e560c06d30e?auto=format&fit=crop&q=80&w=800")
                                .build());

                // Royal Fashion
                productRepository.save(Product.builder().name("Rolex Submariner Date")
                                .description("Luxury diver watch, iconic ceramic bezel.")
                                .price(new BigDecimal("12500.00")).quantity(1).category("Fashion").status("Available")
                                .shop(royalFashion)
                                .image("https://images.unsplash.com/photo-1523275335684-37898b6baf30?auto=format&fit=crop&q=80&w=800")
                                .build());
                productRepository.save(Product.builder().name("Adidas Ultraboost Light")
                                .description("Maximum energy return, incredibly comfortable.")
                                .price(new BigDecimal("190.00")).quantity(25).category("Fashion").status("Available")
                                .shop(royalFashion)
                                .image("https://images.unsplash.com/photo-1542291026-7eec264c27ff?auto=format&fit=crop&q=80&w=800")
                                .build());
                productRepository.save(Product.builder().name("Ray-Ban Wayfarer Classic")
                                .description("Iconic sunglasses with G-15 lenses.").price(new BigDecimal("160.00"))
                                .quantity(20).category("Accessories").status("Available").shop(royalFashion)
                                .image("https://images.unsplash.com/photo-1572635196237-14b3f281503f?auto=format&fit=crop&q=80&w=800")
                                .build());

                // Urban Decor
                productRepository.save(Product.builder().name("Minimalist Floor Lamp")
                                .description("Warm LED lighting, matte black finish.").price(new BigDecimal("85.00"))
                                .quantity(15).category("Home").status("Available").shop(urbanDecor)
                                .image("https://images.unsplash.com/photo-1507473884658-c7a36a2490ec?auto=format&fit=crop&q=80&w=800")
                                .build());
                productRepository.save(Product.builder().name("Ergonomic Office Chair")
                                .description("Breathable mesh, adjustable support.").price(new BigDecimal("299.00"))
                                .quantity(8).category("Home").status("Available").shop(urbanDecor)
                                .image("https://images.unsplash.com/photo-1592078615290-033ee584e267?auto=format&fit=crop&q=80&w=800")
                                .build());

                // --- 4. Create Services ---
                serviceItemRepository.save(ServiceItem.builder().name("Signature Deep Tissue Massage")
                                .description("Firm pressure therapy for muscle recovery.")
                                .price(new BigDecimal("35.00")).durationMinutes(60).status("Available").shop(spa)
                                .image("https://images.unsplash.com/photo-1544161515-4ae6b91829d2?auto=format&fit=crop&q=80&w=800")
                                .build());
                serviceItemRepository.save(ServiceItem.builder().name("Traditional Khmer Herbal Spa")
                                .description("Full body treatment with traditional herbs.")
                                .price(new BigDecimal("50.00")).durationMinutes(90).status("Available").shop(spa)
                                .image("https://images.unsplash.com/photo-1600334129128-685c5582fd35?auto=format&fit=crop&q=80&w=800")
                                .build());
                serviceItemRepository.save(ServiceItem.builder().name("PC Maintenance & Cleaning")
                                .description("Hardware cleaning & thermal paste replacement.")
                                .price(new BigDecimal("20.00")).durationMinutes(45).status("Available").shop(techFix)
                                .image("https://images.unsplash.com/photo-1581092160562-40aa08e78837?auto=format&fit=crop&q=80&w=800")
                                .build());
                serviceItemRepository.save(ServiceItem.builder().name("IT Consulting")
                                .description("Professional network & security advice.").price(new BigDecimal("75.00"))
                                .durationMinutes(60).status("Available").shop(techFix)
                                .image("https://images.unsplash.com/photo-1454165205732-d01140e59b73?auto=format&fit=crop&q=80&w=800")
                                .build());

                // --- 5. Create Events ---
                eventRepository.save(Event.builder()
                                .title("Aura Spa Grand Opening")
                                .description("Join us for the grand opening of our new spa facility. Special discounts on all treatments!")
                                .startDate(LocalDateTime.now().plusDays(5))
                                .endDate(LocalDateTime.now().plusDays(5).plusHours(4))
                                .location("Aeon Mall Mean Chey, Level 2")
                                .shop(spa)
                                .image("https://images.unsplash.com/photo-1540555700478-4be289fbecee?auto=format&fit=crop&q=80&w=800")
                                .build());

                eventRepository.save(Event.builder()
                                .title("Tech Fix Workshop: PC Building")
                                .description("Learn how to build your own PC from scratch with our expert technicians.")
                                .startDate(LocalDateTime.now().plusDays(10))
                                .endDate(LocalDateTime.now().plusDays(10).plusHours(3))
                                .location("Tech Fix Main Office")
                                .shop(techFix)
                                .image("https://images.unsplash.com/photo-1591799264318-7e6ef8ddb7ea?auto=format&fit=crop&q=80&w=800")
                                .build());

                // --- 6. Create Promotions ---
                // Get some products for promotions
                Product macBook = productRepository.findAll().stream()
                                .filter(p -> p.getName().contains("MacBook"))
                                .findFirst().orElse(null);
                Product shoes = productRepository.findAll().stream()
                                .filter(p -> p.getName().contains("Adidas"))
                                .findFirst().orElse(null);

                // Electronics promotion - 15% off
                if (macBook != null) {
                        promotionRepository.save(Promotion.builder()
                                        .name("Back to School Tech Savings")
                                        .description("Get 15% off on premium laptops and electronics for back to school!")
                                        .type(Promotion.PromotionType.PERCENTAGE)
                                        .discountValue(new BigDecimal("15.00"))
                                        .startDate(LocalDateTime.now().minusDays(1))
                                        .endDate(LocalDateTime.now().plusDays(30))
                                        .status(Promotion.PromotionStatus.ACTIVE)
                                        .minimumPurchase(new BigDecimal("100.00"))
                                        .maxDiscountAmount(new BigDecimal("500.00"))
                                        .usageLimit(100)
                                        .perCustomerLimit(1)
                                        .promoCode("TECH15")
                                        .shop(techHub)
                                        .appliesToAll(true)
                                        .build());
                }

                // Fashion promotion - $50 off over $200
                if (shoes != null) {
                        promotionRepository.save(Promotion.builder()
                                        .name("Fashion Forward Sale")
                                        .description("Save $50 when you spend $200 or more on fashion items!")
                                        .type(Promotion.PromotionType.FIXED_AMOUNT)
                                        .discountValue(new BigDecimal("50.00"))
                                        .startDate(LocalDateTime.now().minusDays(2))
                                        .endDate(LocalDateTime.now().plusDays(20))
                                        .status(Promotion.PromotionStatus.ACTIVE)
                                        .minimumPurchase(new BigDecimal("200.00"))
                                        .usageLimit(50)
                                        .perCustomerLimit(1)
                                        .promoCode("FASHION50")
                                        .shop(royalFashion)
                                        .appliesToAll(true)
                                        .build());
                }

                // Product-specific promotion - Buy One Get One on headphones
                Product headphones = productRepository.findAll().stream()
                                .filter(p -> p.getName().contains("Sony"))
                                .findFirst().orElse(null);
                if (headphones != null) {
                        promotionRepository.save(Promotion.builder()
                                        .name("Audio Upgrade Special")
                                        .description("Buy one pair of premium headphones, get 50% off the second pair!")
                                        .type(Promotion.PromotionType.BOGO)
                                        .discountValue(new BigDecimal("50.00")) // 50% off second item
                                        .startDate(LocalDateTime.now())
                                        .endDate(LocalDateTime.now().plusDays(14))
                                        .status(Promotion.PromotionStatus.ACTIVE)
                                        .minimumPurchase(new BigDecimal("100.00"))
                                        .usageLimit(25)
                                        .perCustomerLimit(1)
                                        .promoCode("AUDIOBOGO")
                                        .shop(techHub)
                                        .product(headphones)
                                        .build());
                }

                // Spa services promotion - 20% off all services
                promotionRepository.save(Promotion.builder()
                                .name("Wellness Month Special")
                                .description("Treat yourself to 20% off all spa services this month!")
                                .type(Promotion.PromotionType.PERCENTAGE)
                                .discountValue(new BigDecimal("20.00"))
                                .startDate(LocalDateTime.now().minusDays(1))
                                .endDate(LocalDateTime.now().plusDays(30))
                                .status(Promotion.PromotionStatus.ACTIVE)
                                .minimumPurchase(new BigDecimal("25.00"))
                                .maxDiscountAmount(new BigDecimal("100.00"))
                                .usageLimit(200)
                                .perCustomerLimit(2)
                                .promoCode("WELLNESS20")
                                .shop(spa)
                                .appliesToAll(true)
                                .build());

                // Home decor promotion - Free shipping
                promotionRepository.save(Promotion.builder()
                                .name("Home Refresh Free Shipping")
                                .description("Free shipping on all furniture and home decor orders over $150!")
                                .type(Promotion.PromotionType.FIXED_AMOUNT)
                                .discountValue(new BigDecimal("15.00")) // Free shipping amount
                                .startDate(LocalDateTime.now())
                                .endDate(LocalDateTime.now().plusDays(45))
                                .status(Promotion.PromotionStatus.ACTIVE)
                                .minimumPurchase(new BigDecimal("150.00"))
                                .usageLimit(150)
                                .perCustomerLimit(1)
                                .promoCode("HOMESHIP")
                                .shop(urbanDecor)
                                .appliesToAll(true)
                                .build());

                System.out.println("Real-world demo data seeded successfully via DataInitializer!");
        }
}
