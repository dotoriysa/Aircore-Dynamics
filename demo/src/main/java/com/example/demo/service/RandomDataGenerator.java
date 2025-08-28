package com.example.demo.service;

import com.example.demo.mapper.FactoryMapper;
import com.example.demo.model.EnvironmentData;
import com.example.demo.util.RandomEnvironmentDataGenerator; // 변경된 부분
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.LocalDateTime;

@Service
public class RandomDataGenerator {

    private final FactoryMapper factoryMapper;

    // 생성자 주입 (권장 방식)
    public RandomDataGenerator(FactoryMapper factoryMapper) {
        this.factoryMapper = factoryMapper;
    }

    /**
     * 10분마다 실행되어 랜덤 온습도 데이터를 생성하고 저장합니다.
     */
    @Scheduled(fixedRate = 10000) // 10초 = 10,000ms
    // 또는 @Scheduled(cron = "0 */10 * * * *") // 매 10분마다
    public void generateAndSaveEnvironmentData() {
        float temperature = RandomEnvironmentDataGenerator.generateTemperature();
        float humidity = RandomEnvironmentDataGenerator.generateHumidity();

        EnvironmentData environmentData = new EnvironmentData();
        environmentData.setTimestamp(LocalDateTime.now());
        environmentData.setTemperature(temperature);
        environmentData.setHumidity(humidity);

        factoryMapper.insertEnvironmentData(environmentData);

        System.out.println("새로운 환경 데이터가 저장되었습니다: " + environmentData);
    }

    public EnvironmentData getLatestEnvironmentData() {
        return factoryMapper.selectLatestEnvironmentData();
    }
}
