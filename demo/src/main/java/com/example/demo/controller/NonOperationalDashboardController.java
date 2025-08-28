package com.example.demo.controller;

import com.example.demo.service.NonOperationalDashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/non-operational")
// @CrossOrigin(origins = "*") <- 이 줄 제거
public class NonOperationalDashboardController {

    private final NonOperationalDashboardService nonOperationalDashboardService;

    public NonOperationalDashboardController(NonOperationalDashboardService nonOperationalDashboardService) {
        this.nonOperationalDashboardService = nonOperationalDashboardService;
    }

    @GetMapping("/machines")
    public ResponseEntity<List<Map<String, Object>>> getNonOperationalMachines() {
        try {
            List<Map<String, Object>> nonOperationalMachines = nonOperationalDashboardService.getNonOperationalMachines();
            return ResponseEntity.ok(nonOperationalMachines);
        } catch (Exception e) {
            System.out.println("비가동 머신 목록 조회 API 에러: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/maintenance")
    public ResponseEntity<Map<String, Object>> setMachineToMaintenance(@RequestBody Map<String, String> request) {
        try {
            String pmId = request.get("pmId");
            String errorCode = request.get("errorCode");

            boolean success = nonOperationalDashboardService.setMachineToMaintenance(pmId, errorCode);

            Map<String, Object> response = Map.of(
                    "success", success,
                    "message", success ? "머신이 점검 상태로 변경되었습니다." : "머신 상태 변경에 실패했습니다.",
                    "pmId", pmId,
                    "errorCode", errorCode
            );

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.out.println("머신 점검 상태 변경 API 에러: " + e.getMessage());
            Map<String, Object> errorResponse = Map.of(
                    "success", false,
                    "message", "서버 오류가 발생했습니다: " + e.getMessage()
            );
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
}