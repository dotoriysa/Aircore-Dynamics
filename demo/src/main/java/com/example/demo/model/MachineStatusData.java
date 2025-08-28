package com.example.demo.model;

import java.time.LocalDateTime;

/**
 * 장비 가동 상태 모델 클래스
 * 10초 간격으로 측정된 장비별 가동 상태 정보를 저장
 */
public class MachineStatusData {

    private Integer id;
    private LocalDateTime timestamp;
    private String pmId;
    private Integer status;
    private String errorCode;  // 추가된 필드

    // 기본 생성자
    public MachineStatusData() {
    }

    // 기본 필드 생성자 (id 제외)
    public MachineStatusData(LocalDateTime timestamp, String pmId, Integer status) {
        this.timestamp = timestamp;
        this.pmId = pmId;
        this.status = status;
    }

    // 전체 필드 포함 생성자 (id 제외)
    public MachineStatusData(LocalDateTime timestamp, String pmId, Integer status, String errorCode) {
        this.timestamp = timestamp;
        this.pmId = pmId;
        this.status = status;
        this.errorCode = errorCode;
    }

    // Getter/Setter 메서드들
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getPmId() {
        return pmId;
    }

    public void setPmId(String pmId) {
        this.pmId = pmId;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getErrorCode() {  // 추가
        return errorCode;
    }

    public void setErrorCode(String errorCode) {  // 추가
        this.errorCode = errorCode;
    }
}