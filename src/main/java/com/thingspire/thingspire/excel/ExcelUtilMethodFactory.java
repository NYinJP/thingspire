package com.thingspire.thingspire.excel;

import com.thingspire.thingspire.audit.AuditDTO;
import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

public interface ExcelUtilMethodFactory {
    void auditExcelDownload(List<AuditDTO> data, LocalDateTime searchStartDate, LocalDateTime searchEndDate, HttpServletResponse response);

    void renderAuditExcelBody(List<AuditDTO> data, Sheet sheet, Row row, Cell cell);
}
