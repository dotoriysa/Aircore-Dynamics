package com.example.demo.mapper;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ProductionMapper {

    /**
     * 오늘 날짜의 PM008 장비에 대한 총 생산량을 조회합니다.
     * @return 오늘 날짜의 총 생산량 (개)
     */
    Double selectDailyTotalProduction();
}
