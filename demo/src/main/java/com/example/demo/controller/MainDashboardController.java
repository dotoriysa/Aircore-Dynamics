package com.example.demo.controller;

import com.example.demo.service.MainDashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 메인 대시보드 통계 REST API 컨트롤러
 * (서비스와 연결된 버전)
 */
@RestController
@RequestMapping("/api/main-dashboard")
public class MainDashboardController {

    @Autowired
    private MainDashboardService dashboardService;

    /**
     * 메인 대시보드 요약 정보 조회
     * GET /api/main-dashboard/summary
     */
    @GetMapping("/summary")
    public ResponseEntity<Map<String, Object>> getMainDashboardSummary() {
        Map<String, Object> summary = new HashMap<>();

        // 서비스로부터 각 데이터를 가져와 Map에 추가
        summary.put("total_operation_rate", dashboardService.getOperationRate());
        summary.put("operating_machines", dashboardService.getOperatingMachines());
        summary.put("daily_total_production", dashboardService.getDailyProduction());
        summary.put("factory_temperature", dashboardService.getTemperature() + "°C");
        summary.put("factory_humidity", dashboardService.getHumidity() + "%");
        summary.put("total_power_consumption", dashboardService.getPowerConsumption());
        summary.put("defect_rate", dashboardService.getDefectRate());

        // 메시지 및 상태 정보는 그대로 유지
        summary.put("message", "메인 대시보드 데이터가 정상적으로 조회되었습니다.");
        summary.put("status", "OK");

        return ResponseEntity.ok(summary);
    }
}