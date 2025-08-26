package com.example.demo.model;

import java.time.LocalDateTime;

/**
 * 환경 데이터 (온습도) 모델 클래스
 * 10분 간격으로 측정된 온습도 정보를 저장
 */
public class EnvironmentData {

    private Integer id;             // 고유 ID
    private LocalDateTime timestamp;  // 측정 시간
    private Float temperature;      // 온도 (°C)
    private Float humidity;         // 습도 (%)

    // 기본 생성자
    public EnvironmentData() {
    }

    // 모든 필드를 포함한 생성자 (id 제외)
    public EnvironmentData(LocalDateTime timestamp, Float temperature, Float humidity) {
        this.timestamp = timestamp;
        this.temperature = temperature;
        this.humidity = humidity;
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

    public Float getTemperature() {
        return temperature;
    }

    public void setTemperature(Float temperature) {
        this.temperature = temperature;
    }

    public Float getHumidity() {
        return humidity;
    }

    public void setHumidity(Float humidity) {
        this.humidity = humidity;
    }
}
