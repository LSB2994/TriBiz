package com.tribiz.controller;

import com.tribiz.dto.request.CommentRequest;
import com.tribiz.dto.request.EventRequest;
import com.tribiz.dto.response.CommentResponse;
import com.tribiz.dto.response.MessageResponse;
import com.tribiz.entity.Comment;
import com.tribiz.entity.Event;
import com.tribiz.entity.Shop;
import com.tribiz.entity.User;
import com.tribiz.repository.CommentRepository;
import com.tribiz.repository.EventRepository;
import com.tribiz.repository.ShopRepository;
import com.tribiz.repository.UserRepository;
import com.tribiz.security.services.UserDetailsImpl;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/events")
public class EventController {

    @Autowired
    CommentRepository commentRepository;

    @Autowired
    EventRepository eventRepository;

    @Autowired
    ShopRepository shopRepository;

    @Autowired
    UserRepository userRepository;

    @PostMapping
    @PreAuthorize("hasRole('SELLER') or hasRole('SERVICE_PROVIDER')")
    public ResponseEntity<?> createEvent(@Valid @RequestBody EventRequest eventRequest) {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication()
                .getPrincipal();
        
        List<Shop> userShops = shopRepository.findByOwnerId(userDetails.getId());
        if (userShops.isEmpty()) {
            throw new RuntimeException("Error: You don't have any shops!");
        }
        
        // Use the first shop for now - in a real app, you might want to let the user choose which shop
        Shop shop = userShops.get(0);

        Event event = Event.builder()
                .title(eventRequest.getTitle())
                .description(eventRequest.getDescription())
                .image(eventRequest.getImage())
                .startDate(eventRequest.getStartDate())
                .endDate(eventRequest.getEndDate())
                .location(eventRequest.getLocation())
                .shop(shop)
                .build();

        eventRepository.save(event);
        return ResponseEntity.ok(new MessageResponse("Event created successfully!"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SELLER') or hasRole('SERVICE_PROVIDER')")
    public ResponseEntity<?> updateEvent(@PathVariable("id") Long id, @Valid @RequestBody EventRequest eventRequest) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Error: Event not found!"));

        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication()
                .getPrincipal();
        if (!event.getShop().getOwner().getId().equals(userDetails.getId())) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: You don't own this event!"));
        }

        event.setTitle(eventRequest.getTitle());
        event.setDescription(eventRequest.getDescription());
        event.setImage(eventRequest.getImage());
        event.setStartDate(eventRequest.getStartDate());
        event.setEndDate(eventRequest.getEndDate());
        event.setLocation(eventRequest.getLocation());

        eventRepository.save(event);
        return ResponseEntity.ok(new MessageResponse("Event updated successfully!"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SELLER') or hasRole('SERVICE_PROVIDER')")
    public ResponseEntity<?> deleteEvent(@PathVariable("id") Long id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Error: Event not found!"));

        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication()
                .getPrincipal();
        if (!event.getShop().getOwner().getId().equals(userDetails.getId())) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: You don't own this event!"));
        }

        eventRepository.delete(event);
        return ResponseEntity.ok(new MessageResponse("Event deleted successfully!"));
    }

    @GetMapping
    public ResponseEntity<List<Event>> getAllEvents() {
        return ResponseEntity.ok(eventRepository.findAll());
    }

    @GetMapping("/shop/{shopId}")
    public ResponseEntity<List<Event>> getShopEvents(@PathVariable("shopId") Long shopId) {
        return ResponseEntity.ok(eventRepository.findByShopId(shopId));
    }

    /**
     * Get current user's events
     */
    @GetMapping("/my-events")
    @PreAuthorize("hasRole('SELLER') or hasRole('SERVICE_PROVIDER')")
    public ResponseEntity<List<Event>> getMyEvents() {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication()
                .getPrincipal();

        List<Shop> userShops = shopRepository.findByOwnerId(userDetails.getId());
        if (userShops.isEmpty()) {
            throw new RuntimeException("Error: You don't have any shops!");
        }

        List<Event> allEvents = new ArrayList<>();
        for (Shop shop : userShops) {
            List<Event> shopEvents = eventRepository.findByShopId(shop.getId());
            allEvents.addAll(shopEvents);
        }

        return ResponseEntity.ok(allEvents);
    }

    /**
     * Get upcoming events for current user
     */
    @GetMapping("/my-events/upcoming")
    @PreAuthorize("hasRole('SELLER') or hasRole('SERVICE_PROVIDER')")
    public ResponseEntity<List<Event>> getMyUpcomingEvents() {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication()
                .getPrincipal();

        List<Shop> userShops = shopRepository.findByOwnerId(userDetails.getId());
        if (userShops.isEmpty()) {
            throw new RuntimeException("Error: You don't have any shops!");
        }

        List<Event> allEvents = new ArrayList<>();
        for (Shop shop : userShops) {
            List<Event> shopEvents = eventRepository.findByShopId(shop.getId());
            allEvents.addAll(shopEvents);
        }

        List<Event> upcomingEvents = allEvents.stream()
                .filter(e -> e.getEndDate().isAfter(java.time.LocalDateTime.now()))
                .toList();

        return ResponseEntity.ok(upcomingEvents);
    }

    @GetMapping("/{eventId}/comments")
    public ResponseEntity<List<CommentResponse>> getEventComments(@PathVariable Long eventId) {
        List<Comment> comments = commentRepository.findByEventIdOrderByCreatedAtDesc(eventId);
        List<CommentResponse> responses = comments.stream()
                .map(comment -> CommentResponse.builder()
                        .id(comment.getId())
                        .content(comment.getContent())
                        .authorName(comment.getUser().getFirstName() + " " + comment.getUser().getLastName())
                        .authorAvatar(null) // Can add avatar later
                        .createdAt(comment.getCreatedAt())
                        .build())
                .toList();
        return ResponseEntity.ok(responses);
    }

    @PostMapping("/{eventId}/comments")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CommentResponse> addComment(@PathVariable Long eventId,
                                                      @Valid @RequestBody CommentRequest request) {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication()
                .getPrincipal();

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found"));

        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Comment comment = Comment.builder()
                .content(request.getContent())
                .user(user)
                .event(event)
                .build();

        comment = commentRepository.save(comment);

        CommentResponse response = CommentResponse.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .authorName(user.getFirstName() + " " + user.getLastName())
                .authorAvatar(null)
                .createdAt(comment.getCreatedAt())
                .build();

        return ResponseEntity.ok(response);
    }
}
