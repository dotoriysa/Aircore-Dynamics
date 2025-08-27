package com.example.demo.service;

import com.example.demo.mapper.MachineInfoMapper;
import com.example.demo.mapper.PowerMapper;
import com.example.demo.model.PowerData;
import com.example.demo.model.ProcessMachineInfo;
import com.example.demo.util.RandomPowerDataGenerator;
import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class PowerDataService {

    private final PowerMapper powerMapper;
    private final MachineInfoMapper machineInfoMapper;

    public PowerDataService(PowerMapper powerMapper, MachineInfoMapper machineInfoMapper) {
        this.powerMapper = powerMapper;
        this.machineInfoMapper = machineInfoMapper;
    }

    /**
     * 10초마다 실행되어 모든 장비의 전력량 데이터를 생성하고 저장합니다.
     */
    @Scheduled(fixedRate = 10000) // 10초마다 실행
    public void generatePowerDataForAllMachines() {
        try {
            // 모든 머신 정보 조회
            List<ProcessMachineInfo> allMachines = machineInfoMapper.getAllMachineInfos();

            if (allMachines == null || allMachines.isEmpty()) {
                System.out.println("등록된 머신이 없습니다. (전력량 데이터 생성 중단)");
                return;
            }

            LocalDateTime currentTime = LocalDateTime.now();
            int totalGenerated = 0;

            // 모든 머신에 대해 전력량 데이터 생성
            for (ProcessMachineInfo machine : allMachines) {
                Float powerConsumption = RandomPowerDataGenerator.generatePowerConsumption();

                PowerData powerData = new PowerData();
                powerData.setTimestamp(currentTime);
                powerData.setPmId(machine.getPmId());
                powerData.setPowerConsumption(powerConsumption);

                powerMapper.insertPowerData(powerData);
                totalGenerated++;
            }

            System.out.println("전력량 데이터 생성 완료 - " + totalGenerated + "개 장비, 시간: " + currentTime);

        } catch (Exception e) {
            System.out.println("전력량 데이터 생성 중 에러 발생: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 특정 장비의 전력량 기록을 조회합니다.
     */
    public List<PowerData> getPowerDataRecords(String pmId) {
        return powerMapper.selectAllPowerDataRecords(pmId);
    }
}
