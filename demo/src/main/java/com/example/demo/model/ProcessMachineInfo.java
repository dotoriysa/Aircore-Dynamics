package com.example.demo.model;

/**
 * 공정 장비 정보 모델 클래스
 * 각 장비의 고유 정보와 표준 주기 시간 등을 저장하는 고정 테이블
 */
public class ProcessMachineInfo {

    private String pmId;
    private String processName;
    private String machineName;
    private Integer standardCycleTime;
    private String description;

    // 기본 생성자
    public ProcessMachineInfo() {
    }

    // 모든 필드를 포함한 생성자
    public ProcessMachineInfo(String pmId, String processName, String machineName, Integer standardCycleTime, String description) {
        this.pmId = pmId;
        this.processName = processName;
        this.machineName = machineName;
        this.standardCycleTime = standardCycleTime;
        this.description = description;
    }

    public String getPmId() {
        return pmId;
    }

    public void setPmId(String pmId) {
        this.pmId = pmId;
    }

    public String getProcessName() {
        return processName;
    }

    public void setProcessName(String processName) {
        this.processName = processName;
    }

    public String getMachineName() {
        return machineName;
    }

    public void setMachineName(String machineName) {
        this.machineName = machineName;
    }

    public Integer getStandardCycleTime() {
        return standardCycleTime;
    }

    public void setStandardCycleTime(Integer standardCycleTime) {
        this.standardCycleTime = standardCycleTime;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
