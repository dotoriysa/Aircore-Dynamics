package com.example.demo.controller;

import com.example.demo.service.ProcessDashboardService;
import com.example.demo.model.PowerData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/process_dashboard")
public class ProcessDashboardController {

    @Autowired
    private ProcessDashboardService processDashboardService;

    @GetMapping("/{process}")
    public Map<String, Object> getProcessDashboardSummary(@PathVariable("process") String processName) {

        // 요청된 공정 이름(processName)에 대한 데이터를 가져오기 위한 Map 객체 생성
        Map<String, Object> summary = new HashMap<>();

        // 서비스 메서드를 호출하여 각 항목의 값을 계산하고 Map에 추가
        // 이 때, @PathVariable로 받은 processName을 각 서비스 메서드에 전달합니다.
        summary.put("total_operation_rate", processDashboardService.getOperationRate(processName));
        summary.put("operating_machines", processDashboardService.getOperatingMachines(processName));
        summary.put("daily_total_production", processDashboardService.getDailyProduction(processName));
        summary.put("total_power_consumption", processDashboardService.getPowerConsumption(processName));
        summary.put("defect_rate", processDashboardService.getDefectRate(processName));

        // 결과 Map을 JSON 형태로 반환
        return summary;
    }
}