package com.thingspire.thingspire.audit;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Audit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    // 실행 시간
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
    private LocalDateTime activityTime;
    // 로그인 아이디
    private String loginId;
    // 본명
    private String name;
    // 서비스(계정관리? 로그인?)
    private String activityType;
    // 사용자 생성, 사용자 로그인 등
    private String detail;
    // 성공 여부
    private String success;

    public AuditDTO toDTO() {
        return new AuditDTO(id, activityTime, loginId, name, activityType, detail, success);
    }
}
