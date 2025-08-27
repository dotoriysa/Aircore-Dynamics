package com.example.demo.service;

import com.example.demo.mapper.MachineInfoMapper;  // 변경
import com.example.demo.mapper.MachineStatusMapper;
import com.example.demo.model.MachineStatusData;
import com.example.demo.model.ProcessMachineInfo;
import com.example.demo.util.RandomMachineStatusGenerator;
import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@Service
public class MachineStatusService {

    private final MachineStatusMapper machineStatusMapper;
    private final MachineInfoMapper machineInfoMapper;
    private final Random random = new Random();

    public MachineStatusService(MachineStatusMapper machineStatusMapper, MachineInfoMapper machineInfoMapper) {
        this.machineStatusMapper = machineStatusMapper;
        this.machineInfoMapper = machineInfoMapper;
    }

    @Scheduled(fixedRate = 5000)
    public void updateRandomMachineStatus() {
        try {
            List<ProcessMachineInfo> allMachines = machineInfoMapper.getAllMachineInfos();

            // 랜덤하게 머신 선택
            ProcessMachineInfo selectedMachine = allMachines.get(random.nextInt(allMachines.size()));
            String selectedPmId = selectedMachine.getPmId();

            // 선택된 장비의 최신 상태 조회
            MachineStatusData latestStatus = machineStatusMapper.selectLatestMachineStatus(selectedPmId);
            Integer currentStatus = (latestStatus != null) ? latestStatus.getStatus() : 0;

            System.out.println("선택된 머신: " + selectedPmId + " (" + selectedMachine.getMachineName() + "), 현재 상태: " + currentStatus);

            // 가동 중인 경우에만 정지 테스트
            if (currentStatus == 1) {
                System.out.println("가동 중인 머신 발견! 정지 확률 체크...");

                // 테스트를 위해 확률을 높임 (50% 확률로 테스트)
                if (random.nextInt(100) < 50) {
                    MachineStatusData newStatusData = new MachineStatusData();
                    newStatusData.setTimestamp(LocalDateTime.now());
                    newStatusData.setPmId(selectedPmId);
                    newStatusData.setStatus(0);

                    machineStatusMapper.insertMachineStatusData(newStatusData);

                    System.out.println("머신 정지 완료: " + selectedPmId);
                } else {
                    System.out.println("확률에 걸리지 않음, 계속 가동");
                }
            } else {
                System.out.println("정지 상태 머신, 아무 작업 안함");
            }

        } catch (Exception e) {
            System.out.println("에러 발생: " + e.getMessage());
            e.printStackTrace();
        }
    }
}