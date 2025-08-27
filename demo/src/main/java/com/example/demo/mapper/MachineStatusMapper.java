package com.example.demo.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface MachineStatusMapper {

    /**
     * 특정 장비의 가장 최근 상태를 조회하여, 가동 중인 장비의 수를 반환합니다.
     * @param pmId 조회할 장비 ID
     * @return 가동 중인 장비 수 (0 또는 1)
     */
    Integer selectOperatingMachinesCount(@Param("pmId") String pmId);

    /**
     * 특정 장비의 총 가동시간을 초 단위로 계산하여 반환합니다.
     * @param pmId 조회할 장비 ID
     * @return 해당 장비의 총 가동시간 (초)
     */
    Double selectGrandTotalOperatingSeconds(@Param("pmId") String pmId);
}