package com.thingspire.thingspire.excel;

import com.thingspire.thingspire.audit.AuditDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.Charset;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class ExcelUtils implements ExcelUtilMethodFactory {
    @Override
    public void auditExcelDownload(List<AuditDTO> data, LocalDateTime searchStartDate , LocalDateTime searchEndDate, HttpServletResponse response) {
        Workbook workbook = getXSSFWorkBook();
        Sheet sheet = workbook.createSheet("첫 번째 시트");
        Cell cell = null;
        Row row = null;
        List<String> excelHeaderList = getHeaderName(getClass(data));
        row = sheet.createRow(0);
        List<AuditDTO> filteredData = filterDataByDate(data, searchStartDate, searchEndDate);
        for(int i=0; i<excelHeaderList.size(); i++) {

            // 열을 만들어준다.
            cell = row.createCell(i);

            // 열에 헤더이름(컬럼 이름)을 넣어준다.
            cell.setCellValue(excelHeaderList.get(i));
        }

        renderAuditExcelBody(filteredData, sheet, row, cell);

        DateTimeFormatter fileNameFormatter = DateTimeFormatter.ofPattern("yyyyMMdd_HH:mm:ss");
        String currentDateTime = LocalDateTime.now().format(fileNameFormatter);

        // response에 UTF-8 설정 추가
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment;filename = Audit " + currentDateTime + ".xlsx");

        try {
            // 엑셀 파일을 다운로드 하기 위해 write() 메서드를 사용한다.
            workbook.write(response.getOutputStream());
        } catch (IOException e) {
            // checked 예외를 사용하면 추후 의존이나 예외 누수 문제가 생길 수 있으므로
            // RuntimeException으로 한번 감싸서, cause가 나올 수 있게 발생한 예외를 넣어준다.
            log.error("Workbook write 수행 중 IOException 발생!");
            throw new RuntimeException(e);
        } finally {
            // 파일 입출력 스트림을 사용한 후에는 예외 발생 여부와 관계없이 반드시 닫아 주어야 한다.
            closeWorkBook(workbook);
        }
    }
    // startDate와 endDate에 따라 데이터를 필터링하는 로직 추가
    private List<AuditDTO> filterDataByDate(List<AuditDTO> data, LocalDateTime searchStartDate, LocalDateTime searchEndDate) {
        return data.stream()
                .filter(audit -> audit.getActivityTime().isAfter(searchStartDate) && audit.getActivityTime().isBefore(searchEndDate))
                .collect(Collectors.toList());
    }

    @Override
    public void renderAuditExcelBody(List<AuditDTO> data, Sheet sheet, Row row, Cell cell) {
        int rowCount = 1;
        for (AuditDTO audit : data) {
            row = sheet.createRow(rowCount++);
            row.createCell(0).setCellValue(audit.getId());

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss a");
            String formattedDateTime = audit.getActivityTime().format(formatter);
            row.createCell(1).setCellValue(formattedDateTime);
            // row.createCell(1).setCellValue(audit.getActivityTime().toString());

            row.createCell(2).setCellValue(audit.getLoginId());
            row.createCell(3).setCellValue(audit.getName());
            row.createCell(4).setCellValue(audit.getActivityType());
            row.createCell(5).setCellValue(audit.getDetail());
            row.createCell(6).setCellValue(audit.getSuccess());
        }
    }

    private XSSFWorkbook getXSSFWorkBook() {
        return new XSSFWorkbook();
    }
    private List<String> getHeaderName(Class<?> type) {

        // 스트림으로 엑셀 헤더 이름들을 리스트로 반환
        // 1. 매개변수로 전달된 클래스의 필드들을 배열로 받아, 스트림을 생성
        // 2. @ExcelColumn 애너테이션이 붙은 필드만 수집
        // 3. @ExcelColumn 애너테이션이 붙은 필드에서 애너테이션의 값을 매핑
        // 4. LinkedList로 반환
        List<String> excelHeaderNameList =  Arrays.stream(type.getDeclaredFields())
                .filter(s -> s.isAnnotationPresent(ExcelColumnName.class))
                .map(s -> s.getAnnotation(ExcelColumnName.class).headerName())
                .collect(Collectors.toCollection(LinkedList::new));

        // 헤더의 이름을 담은 List가 비어있을 경우, 헤더 이름이 지정되지 않은 것이므로, 예외를 발생시킨다.
        if(CollectionUtils.isEmpty(excelHeaderNameList)) {
            log.error("헤더 이름이 조회되지 않아 예외 발생!");
            throw new IllegalStateException("헤더 이름이 없습니다.");
        }

        return excelHeaderNameList;
    }

    private Class<?> getClass(List<?> data) {
        // List가 비어있지 않다면 List가 가지고 있는 모든 DTO는 같은 필드를 가지고 있으므로,
        // 맨 마지막 DTO만 빼서 클래스 정보를 반환한다.
        if(!CollectionUtils.isEmpty(data)) {
            return data.get(data.size()-1).getClass();
        } else {
            log.error("리스트가 비어 있어서 예외 발생!");
            throw new IllegalStateException("조회된 리스트가 비어 있습니다. 확인 후 다시 진행해주시기 바랍니다.");
        }
    }
    private void closeWorkBook(Workbook workbook) {
        try {
            if(workbook != null) {
                workbook.close();
            }
        } catch (IOException e) {
            // checked 예외를 사용하면 추후 의존이나 예외 누수 문제가 생길 수 있으므로
            // RuntimeException으로 한번 감싸서, cause가 나올 수 있게 발생한 예외를 넣어준다.
            throw new RuntimeException(e);
        }
    }
}
