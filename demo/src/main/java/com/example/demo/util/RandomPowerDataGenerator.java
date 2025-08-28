package com.example.demo.util;

import java.util.Random;

/**
 * 전력량 데이터를 랜덤하게 생성하는 유틸리티 클래스
 */
public class RandomPowerDataGenerator {

    private static final Random random = new Random();
    private static final float MIN_POWER = 30.0f;
    private static final float MAX_POWER = 50.0f;

    /**
     * 30~50 사이의 랜덤한 전력량을 생성합니다.
     * @return 전력 소비량 (30.0 ~ 50.0)
     */
    public static Float generatePowerConsumption() {
        return MIN_POWER + (MAX_POWER - MIN_POWER) * random.nextFloat();
    }
}