package com.example.demo.mapper;

import com.example.demo.model.MachineStatusData;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface MachineStatusMapper {

    /**
     * 장비 상태 데이터를 데이터베이스에 삽입합니다.
     * @param machineStatusData 삽입할 장비 상태 데이터
     */
    void insertMachineStatusData(MachineStatusData machineStatusData);

    /**
     * 특정 장비의 가장 최근 가동 상태 데이터를 조회합니다.
     * @param pmId 조회할 장비 ID
     * @return 최신 장비 가동 상태 객체
     */
    MachineStatusData selectLatestMachineStatus(@Param("pmId") String pmId);

    /**
     * 특정 장비의 모든 상태 기록을 시간 순으로 조회합니다.
     * @param pmId 조회할 장비 ID
     * @return 장비 가동 상태 데이터 목록
     */
    List<MachineStatusData> selectAllMachineStatusRecords(@Param("pmId") String pmId);
}