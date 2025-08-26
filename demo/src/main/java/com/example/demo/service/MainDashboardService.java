package com.example.demo.service;

import com.example.demo.mapper.FactoryMapper;
import com.example.demo.mapper.MachineStatusMapper;
import com.example.demo.mapper.PowerMapper;
import com.example.demo.mapper.ProductionMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;

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
     * 전체 가동률을 계산합니다.
     * @return 가동률 (%)
     */
    public double getOperationRate() {
        // DB에서 모든 장비의 총 가동 시간(초 단위)을 가져옴
        double operatingSeconds = machineStatusMapper.selectGrandTotalOperatingSeconds();

        LocalDateTime startTime = LocalDate.now().atTime(6, 0);
        LocalDateTime now = LocalDateTime.now();

        // 현재 시간이 23:00 이후면 23:00 고정
        LocalDateTime endTime = now.isAfter(now.withHour(23).withMinute(0))
                ? now.withHour(23).withMinute(0)
                : now;

        // 총 기준 시간(초) = 06:00 ~ endTime
        long totalSeconds = Duration.between(startTime, endTime).getSeconds();

        if (totalSeconds <= 0) {
            return 0.0;
        }

        // 가동률 계산
        return (operatingSeconds / (double) totalSeconds) * 100.0;
    }




    /**
     * 가동 중인 장비 수를 고정 값으로 반환합니다.
     * @return 가동 중인 장비 수
     */
    public int getOperatingMachines() {
        return machineStatusMapper.selectOperatingMachinesCount();
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