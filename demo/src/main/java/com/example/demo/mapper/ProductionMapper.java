package com.example.demo.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ProductionMapper {

    /**
     * 오늘 날짜의 장비에 대한 총 생산량을 조회합니다.
     * @param pmId 장비 ID
     * @return 오늘 날짜의 총 생산량 (개)
     */
    Double selectDailyTotalProduction(@Param("pmId") String pmId);

    /**
     * 특정 장비가 생산한 불량품(NG)의 총 개수를 조회합니다.
     * @param pmId 장비 ID
     * @return 불량품(NG) 총 개수
     */
    Double selectNgCountByPmId(@Param("pmId") String pmId);
}