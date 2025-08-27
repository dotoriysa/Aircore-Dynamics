package com.example.demo.mapper;

import com.example.demo.model.EnvironmentData;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface FactoryMapper {

    /**
     * 공장 온습도 테이블에서 가장 최근 온습도 데이터를 조회합니다.
     * @return 최신 환경 데이터 객체
     */
    EnvironmentData selectLatestEnvironmentData();

    /**
     * 새로운 온습도 데이터를 테이블에 추가합니다.
     * @param data 추가할 환경 데이터 객체
     */
    void insertEnvironmentData(EnvironmentData data);
}