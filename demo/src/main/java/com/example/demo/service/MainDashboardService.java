package com.example.demo.service;

import com.example.demo.mapper.*;
import com.example.demo.model.ProcessMachineInfo;
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
     * 전체 가동률을 계산합니다.
     * 가동률 = (실제 가동시간 / 가동 가능한 총 시간) * 100
     * 기준 시간: 오늘 오전 6시부터 현재 시간까지
     * @return 가동률 (퍼센트)
     */
    public double getOperationRate() {
        // 모든 장비 정보 목록을 조회
        List<ProcessMachineInfo> machineInfos = machineInfoMapper.getAllMachineInfos();

        if (machineInfos.isEmpty()) {
            return 0.0;
        }

        double totalOperatingSeconds = 0.0;

        // 각 장비별 실제 가동시간 합산
        for (ProcessMachineInfo machineInfo : machineInfos) {
            Double operatingSeconds = machineStatusMapper.selectGrandTotalOperatingSeconds(machineInfo.getPmId());
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
        long totalPossibleOperatingTime = totalPossibleSeconds * machineInfos.size();

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
        List<ProcessMachineInfo> machineInfos = machineInfoMapper.getAllMachineInfos();

        int totalOperatingCount = 0;

        // 2. 각 장비별로 가동 상태 확인
        for (ProcessMachineInfo machineInfo : machineInfos) {
            Integer operatingCount = machineStatusMapper.selectOperatingMachinesCount(machineInfo.getPmId());
            if (operatingCount != null) {
                totalOperatingCount += operatingCount;
            }
        }

        return totalOperatingCount;
    }

    /**
     * 전체 장비의 일일 총 생산량을 합산하여 반환합니다.
     * @return 일일 총 생산량
     */
    public Double getDailyProduction() {
        List<ProcessMachineInfo> machineInfos = machineInfoMapper.getAllMachineInfos();

        if (machineInfos.isEmpty()) {
            return 0.0;
        }

        double totalProduction = 0.0;

        for (ProcessMachineInfo machineInfo : machineInfos) {
            Double dailyProduction = productionMapper.selectDailyTotalProduction(machineInfo.getPmId());
            if (dailyProduction != null) {
                totalProduction += dailyProduction;
            }
        }
        return totalProduction;
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
     * 전체 장비의 일일 총 전력량을 합산하여 반환합니다.
     * @return 전력량 (kWh)
     */
    public double getPowerConsumption() {
        List<ProcessMachineInfo> machineInfos = machineInfoMapper.getAllMachineInfos();

        if (machineInfos.isEmpty()) {
            return 0.0;
        }

        double totalPowerConsumption = 0.0;

        for (ProcessMachineInfo machineInfo : machineInfos) {
            Double powerConsumption = powerMapper.selectTotalPowerConsumptionByDateAndPmId(machineInfo.getPmId());
            if (powerConsumption != null) {
                totalPowerConsumption += powerConsumption;
            }
        }
        return totalPowerConsumption;
    }
    /**
     * 전체 장비의 일일 불량품(NG) 총 개수를 합산하여 반환합니다.
     * @return 일일 불량품 총 개수
     */
    public double getDailyNgCount() {
        List<ProcessMachineInfo> machineInfos = machineInfoMapper.getAllMachineInfos();

        if (machineInfos.isEmpty()) {
            return 0.0;
        }

        double totalNgCount = 0.0;

        for (ProcessMachineInfo machineInfo : machineInfos) {
            Double ngCount = productionMapper.selectNgCountByPmId(machineInfo.getPmId());
            if (ngCount != null) {
                totalNgCount += ngCount;
            }
        }
        return totalNgCount;
    }

    /**
     * 전체 장비의 일일 불량률을 계산하여 반환합니다.
     * 불량률 = (불량품 개수 / 총 생산량) * 100
     * @return 불량률 (%)
     */
    public double getDefectRate() {
        double totalProduction = getDailyProduction();
        double ngCount = getDailyNgCount();

        if (totalProduction <= 0) {
            return 0.0; // 총 생산량이 0이거나 음수일 경우 불량률은 0
        }

        return (ngCount / totalProduction) * 100.0;
    }
}