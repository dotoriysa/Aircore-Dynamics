package com.example.demo.mapper;

import com.example.demo.model.PowerData;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface PowerMapper {

    /**
     * 특정 장비의 모든 전력 소비 기록을 시간 순으로 조회합니다.
     * @param pmId 조회할 장비 ID
     * @return 장비 전력 소비 기록 목록
     */
    List<PowerData> selectAllPowerDataRecords(@Param("pmId") String pmId);
}