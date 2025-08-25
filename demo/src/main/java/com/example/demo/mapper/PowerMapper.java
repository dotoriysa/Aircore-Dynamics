package com.example.demo.mapper;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PowerMapper {

    /**
     * 전력 소비량 테이블의 모든 전력 소비량을 합산하여 조회합니다.
     * @return 전체 전력 소비량 (kWh)
     */
    Double selectTotalPowerConsumptionByDate();
}
