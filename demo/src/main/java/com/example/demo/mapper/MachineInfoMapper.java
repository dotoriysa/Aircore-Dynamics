package com.example.demo.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface MachineInfoMapper {

    /**
     * 모든 장비의 pm_id 리스트를 조회
     * @return pm_id 리스트
     */
    List<String> getAllPmIds();

    /**
     * 특정 공정 장비의 pm_id 리스트를 조회
     * @param processName 공정 이름
     * @return pm_id 리스트
     */
    List<String> getPmIdsByProcessName(@Param("processName") String processName);
}