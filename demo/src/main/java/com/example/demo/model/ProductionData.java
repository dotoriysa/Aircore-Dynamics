package com.example.demo.model;

import java.time.LocalDateTime;

/**
 * 생산 실적 모델 클래스
 * 완료된 제품의 수량, 품질 등의 정보를 저장
 */
public class ProductionData {

    private Integer id;
    private LocalDateTime timestamp;
    private String pmId;
    private Integer completedQuantity;
    private String qualityGrade;

    // 기본 생성자
    public ProductionData() {
    }

    // 필수 필드만 포함한 생성자 (id 제외)
    public ProductionData(LocalDateTime timestamp, String pmId, Integer completedQuantity, String qualityGrade) {
        this.timestamp = timestamp;
        this.pmId = pmId;
        this.completedQuantity = completedQuantity;
        this.qualityGrade = qualityGrade;
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

    public Integer getCompletedQuantity() {
        return completedQuantity;
    }

    public void setCompletedQuantity(Integer completedQuantity) {
        this.completedQuantity = completedQuantity;
    }

    public String getQualityGrade() {
        return qualityGrade;
    }

    public void setQualityGrade(String qualityGrade) {
        this.qualityGrade = qualityGrade;
    }
}
