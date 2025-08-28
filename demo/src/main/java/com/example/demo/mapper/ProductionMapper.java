package com.example.demo.mapper;

import com.example.demo.model.ProductionData;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;
@Mapper
public interface ProductionMapper {

    /**
     * 생산 실적 데이터를 데이터베이스에 삽입합니다.
     * @param productionData 삽입할 생산 실적 데이터
     */
    void insertProductionData(ProductionData productionData);

    /**
     * 특정 장비의 모든 생산 실적 기록을 시간 순으로 조회합니다.
     * @param pmId 조회할 장비 ID
     * @return 장비 생산 실적 기록 목록
     */
    List<ProductionData> selectAllProductionRecords(@Param("pmId") String pmId);
}