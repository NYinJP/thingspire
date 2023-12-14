package com.thingspire.thingspire.user;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.thingspire.thingspire.audit.BaseEntity;
import com.thingspire.thingspire.audit.BaseTimeEntity;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@Getter
@Setter
@EntityListeners(value = {AuditingEntityListener.class})
public class Member extends BaseEntity implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long memberId;

    // 아이디
    @Column(length = 254, unique = true)
    private String loginId;

    // 패스워드
    @JsonIgnore
    @NotNull
    @Column(length = 100, nullable = false)
    private String password;

    // 이름
    @Size(max = 50)
    @Column(name = "name", length = 50)
    private String name;

    // 전화번호
    @Size(max = 20)
    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    // 이메일
    @Email
    @Column
    private String email;

    // 휴면 여부
    @NotNull
    @Column(nullable = false)
    private boolean activated = false;

    // 회사명
    @Size(max = 100)
    @Column(name="position", length = 50)
    private String department;

    // 권한 (ADMIN, USER)
    @ElementCollection(fetch = FetchType.EAGER)
    @Column(nullable = false)
    private List<String> roles = new ArrayList<>();

    // 회사 코드(factory code)
    @Column
    private String factoryCode;

    @Column
    private String authority;

    @Column
    private String createdBy;

    @Column
    private String modifiedBy;

    // "elec" 혹은 "solar" 으로 저장
    @Column
    private String departmentType;

    // 엔티티가 삭제되기 직전에 수행됩니다. 일반적으로 값을 반환하지 않습니다.
//    @PreRemove
//    private void beforeRemove() {
//        System.out.println("Member is about to be removed...");
//
//
//
//    }
//
//    public String getDeletedMemberName() {
//        return deletedMemberName;
//    }
}
