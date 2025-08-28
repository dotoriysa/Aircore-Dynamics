<template>
  <div class="dashboard-grid">
    <div class="card wide-card">
      <div class="card-header">
        <div class="card-title">📈 생산량 예측 분석</div>
        <div class="status-indicator status-good"></div>
      </div>
      <div class="prediction-chart">
        <v-chart class="chart" :option="chartOption" autoresize />
      </div>
    </div>
    <div class="card">
      <div class="card-header">
        <div class="card-title">🔍 병목 구간 분석</div>
        <div class="status-indicator status-warning"></div>
      </div>
      <div class="bottleneck-info">
        <div class="bottleneck-item primary">
          <strong>주요 병목 공정:</strong>
          <span class="value">검사 라인</span>
        </div>
        <div class="bottleneck-item">
          <span>평균 대기시간:</span>
          <span class="value">8.3 분</span>
        </div>
        <div class="bottleneck-item">
          <span>처리 속도 (목표 대비):</span>
          <span class="value">85%</span>
        </div>
         <div class="bottleneck-item recommendation">
          <span>권장 사항:</span>
          <span class="value">검사대 추가 운영 검토</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, provide, onMounted } from 'vue';
import { use } from 'echarts/core';
import { CanvasRenderer } from 'echarts/renderers';
import { BarChart, LineChart } from 'echarts/charts';
import {
  TitleComponent,
  TooltipComponent,
  GridComponent,
  LegendComponent,
} from 'echarts/components';
import VChart, { THEME_KEY } from 'vue-echarts';

use([
  CanvasRenderer,
  BarChart,
  LineChart,
  TitleComponent,
  TooltipComponent,
  GridComponent,
  LegendComponent,
]);

provide(THEME_KEY, 'dark');

const chartOption = ref({});

function setupChart() {
  chartOption.value = {
    backgroundColor: 'transparent',
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'cross' }
    },
    legend: {
      data: ['실제 생산량', '예측 생산량'],
      textStyle: { color: '#ccc' }
    },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: {
      type: 'category',
      data: ['어제', '오늘', '내일', '모레'],
      axisLine: { lineStyle: { color: 'rgba(255, 255, 255, 0.2)' } }
    },
    yAxis: {
      type: 'value',
      name: '생산량 (개)',
      axisLabel: { color: 'rgba(255, 255, 255, 0.5)' },
      splitLine: { lineStyle: { color: 'rgba(255, 255, 255, 0.1)' } }
    },
    series: [
      {
        name: '실제 생산량',
        type: 'bar',
        data: [1050, 1087, null, null], // 오늘까지만 실제 데이터
        itemStyle: { color: '#4dd0e1' }
      },
      {
        name: '예측 생산량',
        type: 'line',
        smooth: true,
        lineStyle: { type: 'dashed', color: '#f39c12' },
        itemStyle: { color: '#f39c12' },
        data: [null, 1087, 1120, 1105] // 오늘부터 예측 데이터
      }
    ]
  };
}

onMounted(() => {
  setupChart(); // 현재는 샘플 데이터로 차트 생성
});
</script>

<style scoped>
.prediction-chart {
  height: 250px;
  width: 100%;
}
.chart {
  height: 100%;
  width: 100%;
}
.bottleneck-info {
  font-size: 0.9rem;
  display: flex;
  flex-direction: column;
  gap: 1rem;
}
.bottleneck-item {
  display: flex;
  justify-content: space-between;
}
.bottleneck-item.primary {
  font-weight: bold;
  color: #e74c3c;
}
.bottleneck-item .value {
  font-weight: 600;
}
.recommendation {
  margin-top: 0.5rem;
  padding-top: 0.5rem;
  border-top: 1px solid rgba(255,255,255,0.1);
  color: #3498db;
}
</style>