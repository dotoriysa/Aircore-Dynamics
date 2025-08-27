package com.example.demo.mapper;

import com.example.demo.model.PowerData;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface PowerMapper {

    /**
     * 전력량 데이터를 데이터베이스에 삽입합니다.
     * @param powerData 삽입할 전력량 데이터
     */
    void insertPowerData(PowerData powerData);

    /**
     * 특정 장비의 모든 전력 소비 기록을 시간 순으로 조회합니다.
     * @param pmId 조회할 장비 ID
     * @return 장비 전력 소비 기록 목록
     */
    List<PowerData> selectAllPowerDataRecords(@Param("pmId") String pmId);
}