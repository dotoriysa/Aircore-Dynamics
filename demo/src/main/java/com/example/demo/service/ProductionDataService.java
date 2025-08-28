package com.example.demo.service;

import com.example.demo.mapper.MachineInfoMapper;
import com.example.demo.mapper.MachineStatusMapper;
import com.example.demo.mapper.ProductionMapper;
import com.example.demo.model.MachineStatusData;
import com.example.demo.model.ProcessMachineInfo;
import com.example.demo.model.ProductionData;
import com.example.demo.util.RandomProductionDataGenerator;
import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ProductionDataService {

    private final ProductionMapper productionMapper;
    private final MachineInfoMapper machineInfoMapper;
    private final MachineStatusMapper machineStatusMapper;

    public ProductionDataService(ProductionMapper productionMapper,
                                 MachineInfoMapper machineInfoMapper,
                                 MachineStatusMapper machineStatusMapper) {
        this.productionMapper = productionMapper;
        this.machineInfoMapper = machineInfoMapper;
        this.machineStatusMapper = machineStatusMapper;
    }

    /**
     * 5초마다 실행되어 가동 중인 모든 머신의 생산품 1개씩을 생성합니다.
     */
    @Scheduled(fixedRate = 5000) // 5초마다 실행
    public void generateProductionForActiveMachines() {
        try {
            // 모든 머신 정보 조회
            List<ProcessMachineInfo> allMachines = machineInfoMapper.getAllMachineInfos();

            if (allMachines == null || allMachines.isEmpty()) {
                return;
            }

            LocalDateTime currentTime = LocalDateTime.now();
            int activeProductionCount = 0;

            // 각 머신의 상태를 확인하여 가동 중인 머신만 생산품 생성
            for (ProcessMachineInfo machine : allMachines) {
                String pmId = machine.getPmId();

                // 머신의 최신 상태 확인
                MachineStatusData latestStatus = machineStatusMapper.selectLatestMachineStatus(pmId);
                Integer currentStatus = (latestStatus != null) ? latestStatus.getStatus() : 0;

                // 상태가 1(가동 중)인 경우에만 생산품 생성
                if (currentStatus == 1) {
                    String qualityGrade = RandomProductionDataGenerator.generateQualityGrade();

                    ProductionData productionData = new ProductionData();
                    productionData.setTimestamp(currentTime);
                    productionData.setPmId(pmId);
                    productionData.setCompletedQuantity(1);  // 1개 생산
                    productionData.setQualityGrade(qualityGrade);

                    productionMapper.insertProductionData(productionData);
                    activeProductionCount++;
                }
            }

            if (activeProductionCount > 0) {
                System.out.println("생산 실적 생성 완료 - " + activeProductionCount + "개 가동 머신에서 생산품 각 1개 생성");
            }

        } catch (Exception e) {
            System.out.println("생산 실적 생성 중 에러 발생: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 특정 장비의 생산 실적을 조회합니다.
     */
    public List<ProductionData> getProductionRecords(String pmId) {
        return productionMapper.selectAllProductionRecords(pmId);
    }
}