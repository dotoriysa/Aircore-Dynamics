package com.example.demo.util;

import java.util.Random;

/**
 * 랜덤 환경 데이터를 생성하는 유틸리티 클래스
 * 이전 값을 참조하여 온습도를 랜덤하게 증감시킵니다.
 */
public class RandomEnvironmentDataGenerator {

    private static final Random random = new Random();

    // 마지막으로 생성된 온도와 습도를 저장하는 static 변수
    // 초기값은 범위의 중간값으로 설정하여 안정성을 높입니다.
    private static float lastTemperature = 25.0f;
    private static float lastHumidity = 55.0f;

    /**
     * 20.0 ~ 30.0 °C 범위 내에서 이전 값에 1.0 °C 내외로 랜덤 변화를 적용하여 온도를 생성합니다.
     *
     * @return 랜덤 온도 값
     */
    public static float generateTemperature() {
        // -1.0 ~ 1.0 사이의 랜덤 변화량
        float delta = (random.nextFloat() * 2.0f) - 1.0f;
        float newTemperature = lastTemperature + delta;

        // 범위(20.0 ~ 30.0)를 벗어나면 경계값으로 고정
        if (newTemperature < 20.0f) {
            newTemperature = 20.0f;
        } else if (newTemperature > 30.0f) {
            newTemperature = 30.0f;
        }

        // 소수점 둘째 자리까지 반올림하고 다음 연산을 위해 저장
        lastTemperature = Math.round(newTemperature * 100.0f) / 100.0f;
        return lastTemperature;
    }

    /**
     * 40.0 ~ 70.0 % 범위 내에서 이전 값에 1.0 % 내외로 랜덤 변화를 적용하여 습도를 생성합니다.
     *
     * @return 랜덤 습도 값
     */
    public static float generateHumidity() {
        // -1.0 ~ 1.0 사이의 랜덤 변화량
        float delta = (random.nextFloat() * 2.0f) - 1.0f;
        float newHumidity = lastHumidity + delta;

        // 범위(40.0 ~ 70.0)를 벗어나면 경계값으로 고정
        if (newHumidity < 40.0f) {
            newHumidity = 40.0f;
        } else if (newHumidity > 70.0f) {
            newHumidity = 70.0f;
        }

        // 소수점 둘째 자리까지 반올림하고 다음 연산을 위해 저장
        lastHumidity = Math.round(newHumidity * 100.0f) / 100.0f;
        return lastHumidity;
    }
}