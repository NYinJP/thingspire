package com.thingspire.thingspire.excel;

import com.thingspire.thingspire.audit.Audit;
import com.thingspire.thingspire.audit.AuditDTO;
import com.thingspire.thingspire.audit.AuditRepository;
import net.bytebuddy.asm.Advice;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/excel")
public class ExcelController {
    @Autowired
    private AuditRepository auditRepository;
    @Autowired
    private ExcelUtils excelUtils;

    @GetMapping("/downloadForm")
    public String excelDownloadForm() {
        return "excelDownloadForm";
    }

    @GetMapping("/download")
    public void excelDownload(@RequestParam(value = "fromDate",required = false, defaultValue = "20231001") @DateTimeFormat(pattern = "yyyyMMdd") LocalDate searchStartDate,
                              @RequestParam(value = "toDate", required = false, defaultValue = "20300101") @DateTimeFormat(pattern = "yyyyMMdd") LocalDate searchEndDate,  HttpServletResponse response) throws IOException {

        LocalDateTime fromDate = searchStartDate.atStartOfDay();
        LocalDateTime toDate = searchEndDate.atStartOfDay().plusDays(1).minusSeconds(1);

        List<Audit> auditList = yourDataRetrievalLogic();
        List<AuditDTO> auditDTOList = auditList.stream().map(s->s.toDTO()).collect(Collectors.toList());

        // 엑셀 다운로드 로직 실행
        excelUtils.auditExcelDownload(auditDTOList, fromDate, toDate, response);
    }
    private List<Audit> yourDataRetrievalLogic() {

        return auditRepository.findAll();
    }
}


