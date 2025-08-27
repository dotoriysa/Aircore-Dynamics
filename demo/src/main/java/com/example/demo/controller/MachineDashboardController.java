package com.example.demo.controller;

import com.example.demo.service.MachineDashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/machine-dashboard")
public class MachineDashboardController {

    private final MachineDashboardService machineDashboardService;

    @Autowired
    public MachineDashboardController(MachineDashboardService machineDashboardService) {
        this.machineDashboardService = machineDashboardService;
    }

    /**
     * 특정 장비의 모든 대시보드 지표를 조회합니다.
     * GET /api/machine-dashboard/{pmId}
     */
    @GetMapping("/{pmId}")
    public ResponseEntity<Map<String, Object>> getAllMachineMetrics(@PathVariable String pmId) {
        Map<String, Object> metrics = new HashMap<>();

        metrics.put("pmId", pmId);
        metrics.put("status", machineDashboardService.getMachineStatusByPmId(pmId));
        metrics.put("dailyProduction", machineDashboardService.getDailyProductionByPmId(pmId));
        metrics.put("operationRate",String.format("%.2f", machineDashboardService.getOperationRateByPmId(pmId)));
        metrics.put("powerConsumption", machineDashboardService.getPowerConsumptionByPmId(pmId));
        metrics.put("defectRate",String.format("%.2f", machineDashboardService.getDefectRateByPmId(pmId)));

        return ResponseEntity.ok(metrics);
    }
}