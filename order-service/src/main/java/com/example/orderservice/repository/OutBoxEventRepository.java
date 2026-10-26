package com.example.orderservice.repository;

import com.example.orderservice.entity.OutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface OutBoxEventRepository extends JpaRepository<OutboxEvent, String> {

    @Query(value = """
        SELECT * FROM outbox_event
        WHERE status = 'PENDING' and retry_count <= 3
        ORDER BY created_at
        LIMIT :limit
        FOR UPDATE SKIP LOCKED
        """, nativeQuery = true)
    List<OutboxEvent> lockPendingBatch(@Param("limit") int limit);
}
