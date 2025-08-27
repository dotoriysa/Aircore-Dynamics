<template>
  <div>
    <div class="line-selector">
      <button 
        v-for="line in Object.keys(lineData)" 
        :key="line" 
        :class="['line-btn', { active: selectedLine === line }]" 
        @click="selectedLine = line"
      >
        {{ lineData[line].title }}
      </button>
    </div>
    <div class="dashboard-grid">
      <div class="card">
        <div class="card-header">
          <div class="card-title">{{ currentLineData.prod.title }}</div>
          <div :class="['status-indicator', currentLineData.prod.status]"></div>
        </div>
        <div class="metric" v-for="metric in currentLineData.prod.metrics" :key="metric.label">
          <span>{{ metric.label }}</span>
          <span class="metric-value">{{ metric.value }}</span>
        </div>
      </div>
      <div class="card">
        <div class="card-header">
          <div class="card-title">{{ currentLineData.equip.title }}</div>
          <div :class="['status-indicator', currentLineData.equip.status]"></div>
        </div>
        <div class="metric" v-for="metric in currentLineData.equip.metrics" :key="metric.label">
          <span>{{ metric.label }}</span>
          <span class="metric-value">{{ metric.value }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, watch, onMounted, onUnmounted } from 'vue';

const selectedLine = ref('casting');
const lineData = reactive({
  casting: {
    title: '주조 라인',
    prod: { 
      title: '주조 라인 - 생산 현황', 
      status: 'status-good', 
      metrics: [] // API 데이터로 채워질 빈 배열
    },
    equip: { 
      title: '설비별 상태', 
      status: 'status-good', 
      metrics: [] // API 데이터로 채워질 빈 배열
    }
  },
  machining: {
    title: '가공 라인',
    prod: { 
      title: '가공 라인 - 생산 현황', 
      status: 'status-good', 
      metrics: []
    },
    equip: { 
      title: '설비별 상태', 
      status: 'status-good', 
      metrics: []
    }
  },
  inspection: {
    title: '검사 라인',
    prod: { 
      title: '검사 라인 - 생산 현황', 
      status: 'status-warning', 
      metrics: []
    },
    equip: { 
      title: '설비별 상태', 
      status: 'status-warning', 
      metrics: []
    }
  },
  assembly: {
    title: '조립 라인',
    prod: { 
      title: '조립 라인 - 생산 현황', 
      status: 'status-good', 
      metrics: []
    },
    equip: { 
      title: '설비별 상태', 
      status: 'status-good', 
      metrics: []
    }
  },
  packaging: {
    title: '포장/출하',
    prod: { 
      title: '포장/출하 라인 - 생산 현황', 
      status: 'status-good', 
      metrics: []
    },
    equip: { 
      title: '설비별 상태', 
      status: 'status-good', 
      metrics: []
    }
  }
});
const currentLineData = computed(() => lineData[selectedLine.value]);

// 라인 키를 API 엔드포인트의 한국어 이름으로 변환하는 맵
const lineApiNames = {
    casting: '주조',
    machining: '가공',
    inspection: '검사',
    assembly: '조립',
    packaging: '포장'
};

// API로부터 데이터를 가져오는 함수
async function fetchData(lineKey) {
  const apiName = lineApiNames[lineKey];
  if (!apiName) {
      console.error(`Invalid line key: ${lineKey}`);
      return;
  }

  try {
    const response = await fetch(`/api/process_dashboard/${apiName}`);
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }
    const data = await response.json();
    console.log(`API Data for ${apiName} received:`, data);

    // API 응답 데이터를 템플릿 형식에 맞게 변환
    const prodMetrics = [
        { label: '시간당 생산량', value: `${data.daily_total_production}개` },
        { label: '가동률', value: `${(data.total_operation_rate || 0).toFixed(1)}%` },
        { label: '가동 장비수', value: `${data.operating_machines}대` },
        { label: '전력량', value: `${data.total_power_consumption || 0} kWh` },
        { label: '불량률', value: `${(data.defect_rate || 0).toFixed(1)}%` }
    ];

    // 설비별 상태는 예시 데이터에 없으므로 가상 값을 사용
    let equipMetrics = [];
    if (lineKey === 'casting') {
        equipMetrics = [
            { label: '용해로 온도', value: '742°C' },
            { label: '주조 압력', value: '85 bar' },
            { label: '냉각 시간', value: '12.3분' }
        ];
    } else if (lineKey === 'machining') {
        equipMetrics = [
            { label: 'CNC 스핀들 속도', value: '12,000 RPM' },
            { label: '절삭유량', value: '25 L/min' }
        ];
    }
    
    // 변환된 데이터를 Vue 반응형 상태에 할당
    lineData[lineKey].prod.metrics = prodMetrics;
    lineData[lineKey].prod.status = (data.defect_rate > 5) ? 'status-warning' : 'status-good';
    lineData[lineKey].equip.metrics = equipMetrics;
    // equip.status는 prod.status와 동일하게 설정 (예시)
    lineData[lineKey].equip.status = lineData[lineKey].prod.status;

  } catch (error) {
    console.error(`Failed to fetch data for ${apiName}:`, error);
  }
}

// selectedLine 값이 변경될 때마다 API 호출
watch(selectedLine, (newLine) => {
  fetchData(newLine);
}, { immediate: true }); // 컴포넌트 마운트 시 즉시 실행

// 5초마다 데이터 갱신
let apiInterval;
onMounted(() => {
  apiInterval = setInterval(() => fetchData(selectedLine.value), 5000);
});

onUnmounted(() => {
  clearInterval(apiInterval);
});
</script>