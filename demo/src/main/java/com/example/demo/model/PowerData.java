package com.example.demo.model;

import java.time.LocalDateTime;

/**
 * 전력량 모델 클래스
 * 1분 간격으로 측정된 장비별 전력 소비 정보를 저장
 */
public class PowerData {

    private Integer id;
    private LocalDateTime timestamp;
    private String pmId;
    private Float powerConsumption;

    // 기본 생성자
    public PowerData() {
    }

    // 모든 필드를 포함한 생성자 (id 제외)
    public PowerData(LocalDateTime timestamp, String pmId, Float powerConsumption) {
        this.timestamp = timestamp;
        this.pmId = pmId;
        this.powerConsumption = powerConsumption;
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

    public Float getPowerConsumption() {
        return powerConsumption;
    }

    public void setPowerConsumption(Float powerConsumption) {
        this.powerConsumption = powerConsumption;
    }
}
