package com.example.demo.service;

import com.example.demo.mapper.*;
import com.example.demo.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MachineDashboardService {
    @Autowired
    private PowerMapper powerMapper;

    @Autowired
    private ProductionMapper productionMapper;

    @Autowired
    private FactoryMapper factoryMapper;

    @Autowired
    private MachineStatusMapper machineStatusMapper;

    @Autowired
    private MachineInfoMapper machineInfoMapper;

    /**
     * 특정 장비의 일일 총 생산량을 반환합니다.
     * @param pmId 장비 ID
     * @return 일일 총 생산량
     */
    public Double getDailyProductionByPmId(String pmId) {
        List<ProductionData> productionRecords = productionMapper.selectAllProductionRecords(pmId);
        return calculateDailyProduction(productionRecords);
    }

    /**
     * 특정 장비의 가동률을 계산합니다.
     * @param pmId 장비 ID
     * @return 가동률 (퍼센트)
     */
    public double getOperationRateByPmId(String pmId) {
        // 기준 시간 설정 (오늘 오전 6시부터 현재까지)
        LocalDateTime startTime = LocalDate.now().atTime(6, 0, 0);
        LocalDateTime currentTime = LocalDateTime.now();

        if (currentTime.isBefore(startTime)) {
            startTime = startTime.minusDays(1);
        }

        List<MachineStatusData> statusRecords = machineStatusMapper.selectAllMachineStatusRecords(pmId);
        double operatingSeconds = calculateOperatingSeconds(statusRecords, startTime, currentTime);

        long totalPossibleSeconds = Duration.between(startTime, currentTime).getSeconds();

        if (totalPossibleSeconds == 0) {
            return 0.0;
        }

        return (operatingSeconds / totalPossibleSeconds) * 100.0;
    }

    /**
     * 특정 장비의 일일 총 전력량을 합산하여 반환합니다.
     * @param pmId 장비 ID
     * @return 전력량 (kWh)
     */
    public double getPowerConsumptionByPmId(String pmId) {
        List<PowerData> powerRecords = powerMapper.selectAllPowerDataRecords(pmId);
        return calculateTotalPower(powerRecords);
    }


    /**
     * 특정 장비의 일일 불량률을 계산하여 반환합니다.
     * @param pmId 장비 ID
     * @return 불량률 (%)
     */
    public double getDefectRateByPmId(String pmId) {
        List<ProductionData> productionRecords = productionMapper.selectAllProductionRecords(pmId);

        double totalProduction = 0.0;
        double ngCount = 0.0;
        LocalDate today = LocalDate.now();

        for (ProductionData data : productionRecords) {
            if (data.getTimestamp() != null && data.getTimestamp().toLocalDate().isEqual(today)) {
                if (data.getCompletedQuantity() != null) {
                    totalProduction += data.getCompletedQuantity();
                }
                if ("NG".equals(data.getQualityGrade())) {
                    ngCount += data.getCompletedQuantity();
                }
            }
        }

        if (totalProduction <= 0) {
            return 0.0;
        }

        return (ngCount / totalProduction) * 100.0;
    }

    /**
     * 특정 장비의 현재 상태를 반환합니다.
     * @param pmId 장비 ID
     * @return 장비 상태 (0: 비가동, 1: 가동)
     */
    public int getMachineStatusByPmId(String pmId) {
        MachineStatusData machineStatusData = machineStatusMapper.selectLatestMachineStatus(pmId);
        if (machineStatusData != null && machineStatusData.getStatus() != null) {
            return machineStatusData.getStatus();
        }
        return 0; // 데이터가 없으면 비가동 상태로 간주
    }


    // --------------------------------------------------------------------------------------
    // 헬퍼 함수들 (Helper Functions)
    // --------------------------------------------------------------------------------------

    /**
     * 장비 상태 기록 목록을 바탕으로 총 가동 시간을 계산하는 헬퍼 함수
     * @param statusRecords 장비 상태 데이터 목록
     * @param startTime 계산 시작 시간 (오전 6시)
     * @param endTime 계산 종료 시간 (현재 시간)
     * @return 총 가동 시간 (초)
     */
    private double calculateOperatingSeconds(List<MachineStatusData> statusRecords,
                                             LocalDateTime startTime, LocalDateTime endTime) {
        if (statusRecords == null || statusRecords.isEmpty()) {
            return 0.0;
        }

        statusRecords.sort((a, b) -> a.getTimestamp().compareTo(b.getTimestamp()));

        double operatingSeconds = 0.0;

        Integer currentStatus = null;
        for (MachineStatusData record : statusRecords) {
            if (record.getTimestamp().isBefore(startTime)) {
                currentStatus = record.getStatus();
            } else {
                break;
            }
        }

        LocalDateTime currentTime = startTime;

        for (MachineStatusData record : statusRecords) {
            if (record.getTimestamp().isBefore(startTime)) {
                continue;
            }
            if (record.getTimestamp().isAfter(endTime)) {
                break;
            }

            if (currentStatus != null && currentStatus == 1) {
                Duration duration = Duration.between(currentTime, record.getTimestamp());
                operatingSeconds += duration.getSeconds();
            }

            currentStatus = record.getStatus();
            currentTime = record.getTimestamp();
        }

        if (currentStatus != null && currentStatus == 1) {
            Duration duration = Duration.between(currentTime, endTime);
            operatingSeconds += duration.getSeconds();
        }

        return operatingSeconds;
    }

    /**
     * 생산 기록 목록을 바탕으로 오늘 날짜의 총 생산량을 합산하는 헬퍼 함수
     */
    private double calculateDailyProduction(List<ProductionData> productionRecords) {
        if (productionRecords == null || productionRecords.isEmpty()) {
            return 0.0;
        }

        double dailyTotal = 0.0;
        LocalDate today = LocalDate.now();

        for (ProductionData data : productionRecords) {
            if (data.getTimestamp() != null && data.getTimestamp().toLocalDate().isEqual(today)) {
                if (data.getCompletedQuantity() != null) {
                    dailyTotal += data.getCompletedQuantity();
                }
            }
        }
        return dailyTotal;
    }

    /**
     * 전력 기록 목록을 바탕으로 총 전력량을 합산하는 헬퍼 함수
     */
    private double calculateTotalPower(List<PowerData> powerRecords) {
        if (powerRecords == null || powerRecords.isEmpty()) {
            return 0.0;
        }

        double total = 0.0;
        for (PowerData data : powerRecords) {
            if (data.getPowerConsumption() != null) {
                total += data.getPowerConsumption();
            }
        }
        return total;
    }
}