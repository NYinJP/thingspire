package com.thingspire.thingspire.excel;

import com.thingspire.thingspire.audit.Audit;
import com.thingspire.thingspire.audit.AuditDTO;
import com.thingspire.thingspire.audit.AuditRepository;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
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
    public void excelDownload(HttpServletResponse response) throws IOException {
        List<Audit> auditList = yourDataRetrievalLogic();
        List<AuditDTO> auditDTOList = auditList.stream().map(s->s.toDTO()).collect(Collectors.toList());

        // 엑셀 다운로드 로직 실행
        excelUtils.auditExcelDownload(auditDTOList, response);
    }
    private List<Audit> yourDataRetrievalLogic() {

        return auditRepository.findAll();
    }
}


