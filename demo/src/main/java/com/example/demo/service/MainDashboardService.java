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

        // 기준 시간 설정 (오늘 오전 6시부터 현재까지)
        LocalDateTime startTime = LocalDate.now().atTime(6, 0, 0); // 오늘 오전 6시
        LocalDateTime currentTime = LocalDateTime.now();

        // 만약 현재 시간이 오전 6시 이전이면, 어제 오전 6시부터 계산
        if (currentTime.isBefore(startTime)) {
            startTime = startTime.minusDays(1);
        }

        double totalOperatingSeconds = 0.0;

        // 각 장비별 실제 가동시간 합산
        for (ProcessMachineInfo machineInfo : machineInfos) {
            // 1. 해당 장비의 모든 상태 기록을 가져옵니다.
            List<MachineStatusData> statusRecords = machineStatusMapper.selectAllMachineStatusRecords(machineInfo.getPmId());

            // 2. 가져온 상태 기록을 바탕으로 가동 시간을 계산합니다.
            double operatingSeconds = calculateOperatingSeconds(statusRecords, startTime, currentTime);
            totalOperatingSeconds += operatingSeconds;
        }

        // 가동 가능한 총 시간 계산
        long totalPossibleSeconds = Duration.between(startTime, currentTime).getSeconds();
        long totalPossibleOperatingTime = totalPossibleSeconds * machineInfos.size();

        // 가동률 계산 (퍼센트)
        if (totalPossibleOperatingTime == 0) {
            return 0.0;
        }

        return (totalOperatingSeconds / totalPossibleOperatingTime) * 100.0;
    }

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

        // 시간 순으로 정렬
        statusRecords.sort((a, b) -> a.getTimestamp().compareTo(b.getTimestamp()));

        double operatingSeconds = 0.0;

        // 시작 시간 이전의 마지막 상태 확인 (초기 상태 설정)
        Integer currentStatus = null;
        for (MachineStatusData record : statusRecords) {
            if (record.getTimestamp().isBefore(startTime)) {
                currentStatus = record.getStatus();
            } else {
                break;
            }
        }

        LocalDateTime currentTime = startTime;

        // 시작 시간 이후의 상태 변경 기록들을 처리
        for (MachineStatusData record : statusRecords) {
            // 계산 범위를 벗어난 기록은 무시
            if (record.getTimestamp().isBefore(startTime)) {
                continue;
            }
            if (record.getTimestamp().isAfter(endTime)) {
                break;
            }

            // 이전 상태가 가동(1)이었다면 해당 기간을 가동 시간에 추가
            if (currentStatus != null && currentStatus == 1) {
                Duration duration = Duration.between(currentTime, record.getTimestamp());
                operatingSeconds += duration.getSeconds();
            }

            // 상태와 시간 업데이트
            currentStatus = record.getStatus();
            currentTime = record.getTimestamp();
        }

        // 마지막 상태가 가동(1)이고 종료 시간까지 지속된다면 해당 시간 추가
        if (currentStatus != null && currentStatus == 1) {
            Duration duration = Duration.between(currentTime, endTime);
            operatingSeconds += duration.getSeconds();
        }

        return operatingSeconds;
    }




    /**
     * 가동 중인 장비 수를 계산하여 반환합니다.
     * 모든 장비를 조회하여 각각의 가동 상태를 확인합니다.
     * @return 가동 중인 장비 수
     */
    public int getOperatingMachines() {
        // 1. 모든 장비 목록을 가져옵니다.
        List<ProcessMachineInfo> machineInfos = machineInfoMapper.getAllMachineInfos();

        int totalOperatingCount = 0;

        // 2. 각 장비별로 최신 상태를 확인하여 가동 중인 장비의 수를 셉니다.
        for (ProcessMachineInfo machineInfo : machineInfos) {
            MachineStatusData machineStatusData = machineStatusMapper.selectLatestMachineStatus(machineInfo.getPmId());

            // 3. 최신 상태 데이터가 존재하고, 그 상태(status) 값이 1일 경우에만 카운트를 증가시킵니다.
            if (machineStatusData != null && machineStatusData.getStatus() == 1) {
                totalOperatingCount++;
            }
        }

        return totalOperatingCount;
    }

    /**
     * 일일 완성품(포장 공정)의 총 생산량을 반환합니다.
     * @return 일일 총 완성품 생산량
     */
    public Double getDailyProduction() {
        // '포장' 공정의 장비 정보 목록을 직접 조회
        List<ProcessMachineInfo> machineInfos = machineInfoMapper.getMachineInfosByProcessName("포장");

        if (machineInfos.isEmpty()) {
            return 0.0;
        }

        double totalProduction = 0.0;

        for (ProcessMachineInfo machineInfo : machineInfos) {
            List<ProductionData> productionRecords = productionMapper.selectAllProductionRecords(machineInfo.getPmId());
            double machineDailyProduction = calculateDailyProduction(productionRecords);
            totalProduction += machineDailyProduction;
        }
        return totalProduction;
    }

//---

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
     * 공장 온도를 가져옵니다.
     * @return 온도 (°C)
     */
    public double getTemperature() {
        EnvironmentData data = factoryMapper.selectLatestEnvironmentData();
        if (data != null) {
            return data.getTemperature();
        }
        return 0.0; // 또는 적절한 예외 처리
    }

    /**
     * 공장 습도를 가져옵니다.
     * @return 습도 (%)
     */
    public double getHumidity() {
        EnvironmentData data = factoryMapper.selectLatestEnvironmentData();
        if (data != null) {
            return data.getHumidity();
        }
        return 0.0; // 또는 적절한 예외 처리
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
            // 1. 각 장비의 모든 전력 기록을 가져옵니다.
            List<PowerData> powerRecords = powerMapper.selectAllPowerDataRecords(machineInfo.getPmId());

            // 2. 가져온 기록을 바탕으로 해당 장비의 총 전력량을 계산합니다.
            double machineTotalPower = calculateTotalPower(powerRecords);
            totalPowerConsumption += machineTotalPower;
        }

        return totalPowerConsumption;
    }

    /**
     * 전력 기록 목록을 바탕으로 총 전력량을 합산하는 헬퍼 함수
     * @param powerRecords 전력 기록 데이터 목록
     * @return 총 전력량 (kWh)
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
    /**
     * 일일 완성품(포장 공정)의 총 불량품(NG) 개수를 합산하여 반환합니다.
     * @return 일일 불량품 총 개수
     */
    public double getDailyFinishedGoodsNgCount() {
        List<ProcessMachineInfo> machineInfos = machineInfoMapper.getMachineInfosByProcessName("포장");

        if (machineInfos.isEmpty()) {
            return 0.0;
        }

        double totalNgCount = 0.0;

        for (ProcessMachineInfo machineInfo : machineInfos) {
            List<ProductionData> productionRecords = productionMapper.selectAllProductionRecords(machineInfo.getPmId());
            double machineDailyNgCount = calculateDailyNgCount(productionRecords);
            totalNgCount += machineDailyNgCount;
        }
        return totalNgCount;
    }

    /**
     * 일일 완성품(포장 공정)의 불량률을 계산하여 반환합니다.
     * @return 불량률 (%)
     */
    public double getDefectRate() {
        // getDailyFinishedGoodsProduction() 메서드가 필요합니다.
        // 이 메서드는 이전 답변에서 제공되었으니, 해당 코드를 여기에 추가하세요.
        double totalProduction = getDailyProduction();
        double ngCount = getDailyFinishedGoodsNgCount();

        if (totalProduction <= 0) {
            return 0.0;
        }

        return (ngCount / totalProduction) * 100.0;
    }

    //---

    /**
     * 생산 기록 목록을 바탕으로 오늘 날짜의 불량품 총 개수를 합산하는 헬퍼 함수
     */
    private double calculateDailyNgCount(List<ProductionData> productionRecords) {
        if (productionRecords == null || productionRecords.isEmpty()) {
            return 0.0;
        }

        double dailyNgTotal = 0.0;
        LocalDate today = LocalDate.now();

        for (ProductionData data : productionRecords) {
            if (data.getTimestamp() != null && data.getTimestamp().toLocalDate().isEqual(today)) {
                if ("NG".equals(data.getQualityGrade()) && data.getCompletedQuantity() != null) {
                    dailyNgTotal += data.getCompletedQuantity();
                }
            }
        }
        return dailyNgTotal;
    }
}