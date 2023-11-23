package com.thingspire.thingspire.audit;

import com.thingspire.thingspire.excel.ExcelColumnName;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuditDTO {
    @ExcelColumnName(headerName = "번호")
    private Long id;
    @ExcelColumnName(headerName = "실행시간")
    private LocalDateTime activityTime;
    @ExcelColumnName(headerName = "로그인 아이디")
    private String loginId;
    @ExcelColumnName(headerName = "이름")
    private String name;
    @ExcelColumnName(headerName = "서비스")
    private String activityType;
    @ExcelColumnName(headerName = "내용")
    private String detail;
    @ExcelColumnName(headerName = "성공여부")
    private String success;
}
