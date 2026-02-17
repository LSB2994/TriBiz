package com.tribiz.repository;

import com.tribiz.entity.Comment;
import com.tribiz.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByEventIdOrderByCreatedAtDesc(Long eventId);
}
