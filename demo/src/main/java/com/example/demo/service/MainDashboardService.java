package com.example.demo.service;

import com.example.demo.mapper.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class MainDashboardService {
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
     * 전체 가동률을 고정 값으로 반환합니다.
     * @return 가동률 (%)

    public double getOperationRate() {
        return machineStatusMapper.selectGrandTotalOperatingSeconds();
    }
     */

    /**
     * 전체 가동률을 계산합니다.
     * @return 가동률 (%)
     */
    /**
     * 모든 장비의 가동률을 계산하여 반환합니다.
     * 가동률 = (실제 가동시간 / 가동 가능한 총 시간) * 100
     * 기준 시간: 오늘 오전 6시부터 현재 시간까지
     * @return 가동률 (퍼센트)
     */
    public double getOperationRate() {
        List<String> pmIds = machineInfoMapper.getAllPmIds();

        if (pmIds.isEmpty()) {
            return 0.0;
        }

        double totalOperatingSeconds = 0.0;

        // 각 장비별 실제 가동시간 합산
        for (String pmId : pmIds) {
            Double operatingSeconds = machineStatusMapper.selectGrandTotalOperatingSeconds(pmId);
            if (operatingSeconds != null) {
                totalOperatingSeconds += operatingSeconds;
            }
        }

        // 가동 가능한 총 시간 계산 (오늘 오전 6시부터 현재까지)
        LocalDateTime startTime = LocalDate.now().atTime(6, 0, 0); // 오늘 오전 6시
        LocalDateTime currentTime = LocalDateTime.now();

        // 만약 현재 시간이 오전 6시 이전이면, 어제 오전 6시부터 계산
        if (currentTime.isBefore(startTime)) {
            startTime = startTime.minusDays(1);
        }

        long totalPossibleSeconds = Duration.between(startTime, currentTime).getSeconds();

        // 모든 장비 수만큼 곱함
        long totalPossibleOperatingTime = totalPossibleSeconds * pmIds.size();

        // 가동률 계산 (퍼센트)
        if (totalPossibleOperatingTime == 0) {
            return 0.0;
        }

        return (totalOperatingSeconds / totalPossibleOperatingTime) * 100.0;
    }




    /**
     * 가동 중인 장비 수를 계산하여 반환합니다.
     * 모든 장비를 조회하여 각각의 가동 상태를 확인합니다.
     * @return 가동 중인 장비 수
     */
    public int getOperatingMachines() {
        // 1. 장비 목록을 가져옴
        List<String> pmIds = machineInfoMapper.getAllPmIds();

        int totalOperatingCount = 0;

        // 2. 각 장비별로 가동 상태 확인
        for (String pmId : pmIds) {
            Integer operatingCount = machineStatusMapper.selectOperatingMachinesCount(pmId);
            if (operatingCount != null) {
                totalOperatingCount += operatingCount;
            }
        }

        return totalOperatingCount;
    }

    /**
     * 일일 총 생산량을 고정 값으로 반환합니다.
     * @return 일일 총 생산량
     */
    public Double getDailyProduction() {
        return productionMapper.selectDailyTotalProduction();
    }

    /**
     * 공장 온도를 고정 값으로 반환합니다.
     * @return 온도 (°C)
     */
    public double getTemperature() {
        return factoryMapper.selectLatestTemperature();
    }

    /**
     * 공장 습도를 고정 값으로 반환합니다.
     * @return 습도 (%)
     */
    public double getHumidity() {
        return factoryMapper.selectLatestHumidity();
    }

    /**
     * 전체 전력량을 고정 값으로 반환합니다.
     * @return 전력량 (kWh)
     */
    public double getPowerConsumption() {
        return powerMapper.selectTotalPowerConsumptionByDate();
    }
}