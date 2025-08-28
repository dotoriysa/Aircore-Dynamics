package com.example.demo.service;

import com.example.demo.mapper.MachineInfoMapper;
import com.example.demo.mapper.MachineStatusMapper;
import com.example.demo.model.MachineStatusData;
import com.example.demo.model.ProcessMachineInfo;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Service
public class NonOperationalDashboardService {

    private final MachineStatusMapper machineStatusMapper;
    private final MachineInfoMapper machineInfoMapper;

    public NonOperationalDashboardService(MachineStatusMapper machineStatusMapper, MachineInfoMapper machineInfoMapper) {
        this.machineStatusMapper = machineStatusMapper;
        this.machineInfoMapper = machineInfoMapper;
    }

    /**
     * 비가동 상태인 모든 머신 목록을 조회합니다.
     * @return 비가동 머신 목록 (상태 0, 2)
     */
    public List<Map<String, Object>> getNonOperationalMachines() {
        try {
            List<ProcessMachineInfo> allMachines = machineInfoMapper.getAllMachineInfos();
            List<Map<String, Object>> nonOperationalList = new ArrayList<>();

            for (ProcessMachineInfo machine : allMachines) {
                MachineStatusData latestStatus = machineStatusMapper.selectLatestMachineStatus(machine.getPmId());

                // 상태가 0(정지) 또는 2(점검) 또는 데이터가 없는 경우
                if (latestStatus == null || latestStatus.getStatus() == 0 || latestStatus.getStatus() == 2) {
                    Map<String, Object> machineInfo = new HashMap<>();
                    machineInfo.put("pmId", machine.getPmId());
                    machineInfo.put("machineName", machine.getMachineName());
                    machineInfo.put("processName", machine.getProcessName());
                    machineInfo.put("status", latestStatus != null ? latestStatus.getStatus() : 0);
                    machineInfo.put("statusText", getStatusText(latestStatus != null ? latestStatus.getStatus() : 0));
                    machineInfo.put("errorCode", latestStatus != null ? latestStatus.getErrorCode() : null);
                    machineInfo.put("lastUpdate", latestStatus != null ? latestStatus.getTimestamp() : null);

                    nonOperationalList.add(machineInfo);
                }
            }

            return nonOperationalList;
        } catch (Exception e) {
            System.out.println("비가동 머신 조회 중 에러 발생: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * 특정 머신을 점검 상태로 변경합니다.
     * @param pmId 머신 ID
     * @param errorCode 점검 내용
     * @return 성공 여부
     */
    public boolean setMachineToMaintenance(String pmId, String errorCode) {
        try {
            // 현재 상태 확인
            MachineStatusData currentStatus = machineStatusMapper.selectLatestMachineStatus(pmId);

            // 상태가 0(정지)인 경우에만 점검 상태로 변경
            if (currentStatus != null && currentStatus.getStatus() == 0) {
                machineStatusMapper.updateMachineStatusToMaintenance(pmId, errorCode);
                System.out.println("머신 " + pmId + "를 점검 상태로 변경: " + errorCode);
                return true;
            } else {
                System.out.println("머신 " + pmId + "는 정지 상태가 아니므로 점검 상태로 변경할 수 없습니다.");
                return false;
            }
        } catch (Exception e) {
            System.out.println("머신 상태 변경 중 에러 발생: " + e.getMessage());
            return false;
        }
    }

    /**
     * 상태 코드를 텍스트로 변환합니다.
     */
    private String getStatusText(Integer status) {
        if (status == null) return "알 수 없음";
        switch (status) {
            case 0: return "정지";
            case 1: return "가동";
            case 2: return "점검";
            default: return "알 수 없음";
        }
    }
}
