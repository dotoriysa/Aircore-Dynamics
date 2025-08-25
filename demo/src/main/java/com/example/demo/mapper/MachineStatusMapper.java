package com.example.demo.mapper;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MachineStatusMapper {

    /**
     * 모든 장비의 가장 최근 상태를 조회하여, 가동 중인 장비의 수를 반환합니다.
     * @return 가동 중인 장비 수
     */
    Integer selectOperatingMachinesCount();

    /**
     * 모든 장비의 총 가동시간을 초 단위로 계산하여 반환합니다.
     * @return 모든 장비의 총 가동시간 (초)
     */
    Double selectGrandTotalOperatingSeconds();
}