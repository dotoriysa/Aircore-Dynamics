package com.example.demo.mapper;

import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface MachineInfoMapper {

    /**
     * 모든 장비의 pm_id 리스트를 조회
     * @return pm_id 리스트
     */
    List<String> getAllPmIds();
}
