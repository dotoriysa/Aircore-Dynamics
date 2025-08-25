package com.example.demo.mapper;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface FactoryMapper {

    /**
     * 공장 온습도 테이블에서 가장 최근 온도를 조회합니다.
     * @return 최신 온도
     */
    Double selectLatestTemperature();

    /**
     * 공장 온습도 테이블에서 가장 최근 습도를 조회합니다.
     * @return 최신 습도
     */
    Double selectLatestHumidity();
}