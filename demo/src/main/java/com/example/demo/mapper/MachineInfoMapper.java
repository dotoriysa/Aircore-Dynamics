package com.example.demo.mapper;

import com.example.demo.model.ProcessMachineInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface MachineInfoMapper {

    /**
     * 모든 장비 정보를 조회
     * @return ProcessMachineInfo 객체 리스트
     */
    List<ProcessMachineInfo> getAllMachineInfos();

    /**
     * 특정 공정 장비의 정보를 조회
     * @param processName 공정 이름
     * @return ProcessMachineInfo 객체 리스트
     */
    List<ProcessMachineInfo> getMachineInfosByProcessName(@Param("processName") String processName);
}