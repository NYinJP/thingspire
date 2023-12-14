package com.thingspire.thingspire.audit;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditRepository extends JpaRepository<Audit, Long> {
    List<Audit> findByActivityTimeBetween(LocalDateTime fromDate, LocalDateTime toDate);

    List<Audit> findByActivityTimeBetweenOrderByActivityTimeDesc(LocalDateTime fromDate, LocalDateTime toDate);

    List<Audit> findByLoginIdAndActivityTimeBetweenOrderByActivityTimeDesc(String LoginId, LocalDateTime fromDate, LocalDateTime toDate);
}
