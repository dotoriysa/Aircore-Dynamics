package com.example.demo.util;

import java.util.Random;

/**
 * 생산 실적 데이터를 랜덤하게 생성하는 유틸리티 클래스
 */
public class RandomProductionDataGenerator {

    private static final Random random = new Random();

    /**
     * 랜덤한 품질 등급을 생성합니다.
     * A: 63% 확률, B: 18% 확률, C: 9% 확률, NG: 10% 확률
     * @return 품질 등급 (A, B, C, NG)
     */
    public static String generateQualityGrade() {
        int chance = random.nextInt(100);
        if (chance < 10) {
            return "NG";  // 10% 확률
        } else if (chance < 73) {
            return "A";   // 63% 확률 (73-10=63)
        } else if (chance < 91) {
            return "B";   // 18% 확률 (91-73=18)
        } else {
            return "C";   // 9% 확률 (100-91=9)
        }
    }
}