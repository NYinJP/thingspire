package com.thingspire.thingspire.audit;

import net.bytebuddy.asm.Advice;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/audits")
public class AuditController {
    @Autowired
    private AuditService auditService;

    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @GetMapping
    public ResponseEntity<List<Audit>> getAllAudits(@RequestParam(value = "fromDate", required = false, defaultValue = "20230101") @DateTimeFormat(pattern = "yyyyMMdd") LocalDate searchStartDate,
                                                    @RequestParam(value = "toDate", required = false, defaultValue = "20300101") @DateTimeFormat(pattern = "yyyyMMdd") LocalDate searchEndDate){


        LocalDateTime fromDate = searchStartDate.atStartOfDay();
        LocalDateTime toDate = searchEndDate.atStartOfDay().plusDays(1).minusSeconds(1);

        List<Audit> audits = auditService.findAuditsByDateRange(fromDate, toDate);
        return new ResponseEntity<>(audits, HttpStatus.OK);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @GetMapping("/{loginId}")
    public ResponseEntity<List<Audit>> getAuditsByLoginId(@PathVariable String loginId,
                                                          @RequestParam(value = "fromDate", required = false, defaultValue = "20230101") @DateTimeFormat(pattern = "yyyyMMdd") LocalDate searchStartDate,
                                                          @RequestParam(value = "toDate", required = false, defaultValue = "20300101") @DateTimeFormat(pattern = "yyyyMMdd") LocalDate searchEndDate){
        LocalDateTime fromDate = searchStartDate.atStartOfDay();
        LocalDateTime toDate = searchEndDate.atStartOfDay().plusDays(1).minusSeconds(1);

        List<Audit> audits = auditService.findAuditsByLoginIdAndDateRange(loginId, fromDate, toDate);
        return new ResponseEntity<>(audits, HttpStatus.OK);
    }

}
