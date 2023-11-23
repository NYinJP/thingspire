package com.thingspire.thingspire.audit;

import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
@Repository
public interface AuditRepository extends JpaRepository<Audit, Long> {
    List<Audit> findByActivityTimeBetween(LocalDateTime fromDate, LocalDateTime toDate);
}
