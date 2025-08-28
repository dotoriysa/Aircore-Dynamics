package com.example.demo.util;

import java.util.Random;

/**
 * 머신 상태 데이터를 랜덤하게 생성하는 유틸리티 클래스
 */
public class RandomMachineStatusGenerator {

    private static final Random random = new Random();

    /**
     * 현재 상태를 기반으로 다음 상태를 생성합니다.
     * - 현재 상태가 1이면: 1% 확률로 0(정지), 99% 확률로 1 유지
     * - 현재 상태가 0이면: 계속 0 유지 (정지 상태 유지)
     * @param currentStatus 현재 상태
     * @return 새로운 상태
     */
    public static Integer generateNextStatus(Integer currentStatus) {
        if (currentStatus == null || currentStatus == 0) {
            // 현재 상태가 0이면 계속 0 유지
            return 0;
        } else {
            // 현재 상태가 1이면 1% 확률로 정지
            return random.nextInt(100) < 1 ? 0 : 1;
        }
    }
}
