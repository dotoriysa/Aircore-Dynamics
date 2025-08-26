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

    // 기본 생성자
    public MachineStatusData() {
    }

    // 모든 필드를 포함한 생성자 (id 제외)
    public MachineStatusData(LocalDateTime timestamp, String pmId, Integer status) {
        this.timestamp = timestamp;
        this.pmId = pmId;
        this.status = status;
    }

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
}
