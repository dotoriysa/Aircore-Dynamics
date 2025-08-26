package com.example.demo.service;

import com.example.demo.mapper.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ProcessDashboardService {
    @Autowired
    private PowerMapper powerMapper;

    @Autowired
    private ProductionMapper productionMapper;

    @Autowired
    private MachineStatusMapper machineStatusMapper;

    @Autowired
    private MachineInfoMapper machineInfoMapper;


    /**
     * 특정 공정의 가동률을 계산하여 반환합니다.
     * 가동률 = (실제 가동시간 / 가동 가능한 총 시간) * 100
     * 기준 시간: 오늘 오전 6시부터 현재 시간까지
     * @param processName 가동률을 계산할 공정의 이름
     * @return 가동률 (퍼센트)
     */
    public double getOperationRate(String processName) {
        // 1. 특정 공정에 속한 장비 ID 목록을 조회
        List<String> pmIds = machineInfoMapper.getPmIdsByProcessName(processName);

        if (pmIds.isEmpty()) {
            return 0.0; // 해당 공정의 장비가 없으면 가동률은 0
        }

        double totalOperatingSeconds = 0.0;

        // 2. 각 장비별 실제 가동시간 합산
        for (String pmId : pmIds) {
            Double operatingSeconds = machineStatusMapper.selectGrandTotalOperatingSeconds(pmId);
            if (operatingSeconds != null) {
                totalOperatingSeconds += operatingSeconds;
            }
        }

        // 3. 가동 가능한 총 시간 계산 (오늘 오전 6시부터 현재까지)
        LocalDateTime startTime = LocalDate.now().atTime(6, 0, 0); // 오늘 오전 6시
        LocalDateTime currentTime = LocalDateTime.now();

        // 현재 시간이 오전 6시 이전이면, 어제 오전 6시부터 계산
        if (currentTime.isBefore(startTime)) {
            startTime = startTime.minusDays(1);
        }

        long totalPossibleSeconds = Duration.between(startTime, currentTime).getSeconds();

        // 4. 모든 장비 수만큼 곱하여 전체 가동 가능한 시간 계산
        long totalPossibleOperatingTime = totalPossibleSeconds * pmIds.size();

        // 5. 가동률 계산 (퍼센트)
        if (totalPossibleOperatingTime == 0) {
            return 0.0;
        }

        return (totalOperatingSeconds / totalPossibleOperatingTime) * 100.0;
    }


    /**
     * 특정 공정에서 가동 중인 장비 수를 계산하여 반환합니다.
     * @param processName 가동 중인 장비 수를 계산할 공정의 이름
     * @return 가동 중인 장비 수
     */
    public int getOperatingMachines(String processName) {
        // 1. 특정 공정 장비 목록을 가져옴
        List<String> pmIds = machineInfoMapper.getPmIdsByProcessName(processName);

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
     * 특정 공정의 일일 총 생산량을 합산하여 반환합니다.
     * @param processName 일일 총 생산량을 계산할 공정의 이름
     * @return 일일 총 생산량
     */
    public Double getDailyProduction(String processName) {
        List<String> pmIds = machineInfoMapper.getPmIdsByProcessName(processName);

        if (pmIds.isEmpty()) {
            return 0.0;
        }

        double totalProduction = 0.0;

        for (String pmId : pmIds) {
            Double dailyProduction = productionMapper.selectDailyTotalProduction(pmId);
            if (dailyProduction != null) {
                totalProduction += dailyProduction;
            }
        }
        return totalProduction;
    }

    /**
     * 특정 공정의 일일 총 전력량을 합산하여 반환합니다.
     * @param processName 일일 총 전력량을 계산할 공정의 이름
     * @return 전력량 (kWh)
     */
    public double getPowerConsumption(String processName) {
        List<String> pmIds = machineInfoMapper.getPmIdsByProcessName(processName);

        if (pmIds.isEmpty()) {
            return 0.0;
        }

        double totalPowerConsumption = 0.0;

        for (String pmId : pmIds) {
            Double powerConsumption = powerMapper.selectTotalPowerConsumptionByDateAndPmId(pmId);
            if (powerConsumption != null) {
                totalPowerConsumption += powerConsumption;
            }
        }
        return totalPowerConsumption;
    }

    /**
     * 특정 공정의 일일 불량품(NG) 총 개수를 합산하여 반환합니다.
     * @param processName 일일 불량품 총 개수를 계산할 공정의 이름
     * @return 일일 불량품 총 개수
     */
    public double getDailyNgCount(String processName) {
        List<String> pmIds = machineInfoMapper.getPmIdsByProcessName(processName);

        if (pmIds.isEmpty()) {
            return 0.0;
        }

        double totalNgCount = 0.0;

        for (String pmId : pmIds) {
            Double ngCount = productionMapper.selectNgCountByPmId(pmId);
            if (ngCount != null) {
                totalNgCount += ngCount;
            }
        }
        return totalNgCount;
    }

    /**
     * 특정 공정의 일일 불량률을 계산하여 반환합니다.
     * 불량률 = (불량품 개수 / 총 생산량) * 100
     * @param processName 불량률을 계산할 공정의 이름
     * @return 불량률 (%)
     */
    public double getDefectRate(String processName) {
        double totalProduction = getDailyProduction(processName);
        double ngCount = getDailyNgCount(processName);

        if (totalProduction <= 0) {
            return 0.0; // 총 생산량이 0이거나 음수일 경우 불량률은 0
        }

        return (ngCount / totalProduction) * 100.0;
    }
}