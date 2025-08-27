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
import { ref, reactive, computed } from 'vue';

const selectedLine = ref('casting');
const lineData = reactive({
  casting: {
    title: '주조 라인',
    prod: { 
      title: '주조 라인 - 생산 현황', 
      status: 'status-good', 
      metrics: [
        {label:'시간당 생산량', value:'47개'},
        {label:'가동률', value:'89.2%'},
        {label:'가동 장비수', value:'3 / 3대'},
        {label:'전력량', value:'120 kWh'},
        {label:'불량률', value:'2.1%'}
      ]
    },
    equip: { title: '설비별 상태', status: 'status-good', metrics: [{label:'용해로 온도',value:'742°C'},{label:'주조 압력',value:'85 bar'},{label:'냉각 시간',value:'12.3분'}]}
  },
  machining: {
    title: '가공 라인',
    prod: { 
      title: '가공 라인 - 생산 현황', 
      status: 'status-good', 
      metrics: [
        {label:'시간당 생산량', value:'55개'},
        {label:'가동률', value:'91.5%'},
        {label:'가동 장비수', value:'2 / 2대'},
        {label:'전력량', value:'85 kWh'},
        {label:'불량률', value:'0.9%'}
      ]
    },
    equip: { title: '설비별 상태', status: 'status-good', metrics: [{label:'CNC 스핀들 속도',value:'12,000 RPM'},{label:'절삭유량',value:'25 L/min'}]}
  },
  inspection: {
    title: '검사 라인',
    prod: { 
      title: '검사 라인 - 생산 현황', 
      status: 'status-warning', 
      metrics: [
        {label:'시간당 처리량', value:'60개'},
        {label:'가동률', value:'78.0%'},
        {label:'가동 장비수', value:'1 / 1대'},
        {label:'전력량', value:'30 kWh'},
        {label:'불량 발견율', value:'10.5%'}
      ]
    },
    equip: { title: '설비별 상태', status: 'status-warning', metrics: [{label:'검사기 #2 진동',value:'이상 (Warning)'},{label:'센서 민감도',value:'정상'}]}
  },
  assembly: {
    title: '조립 라인',
    prod: { 
      title: '조립 라인 - 생산 현황', 
      status: 'status-good', 
      metrics: [
        {label:'시간당 조립량', value:'40개'},
        {label:'가동률', value:'85.5%'},
        {label:'가동 장비수', value:'1 / 1대'},
        {label:'전력량', value:'45 kWh'},
        {label:'조립불량률', value:'0.5%'}
      ]
    },
    equip: { title: '설비별 상태', status: 'status-good', metrics: [{label:'로봇 팔 상태',value:'정상'},{label:'토크 값',value:'기준치 내'}]}
  },
  packaging: {
    title: '포장/출하',
    prod: { 
      title: '포장/출하 라인 - 생산 현황', 
      status: 'status-good', 
      metrics: [
        {label:'시간당 포장량', value:'65개'},
        {label:'가동률', value:'95.0%'},
        {label:'가동 장비수', value:'1 / 1대'},
        {label:'전력량', value:'25 kWh'},
        {label:'불량률', value:'0.1%'}
      ]
    },
    equip: { title: '설비별 상태', status: 'status-good', metrics: [{label:'컨베이어 속도',value:'1.2 m/s'},{label:'포장재 잔량',value:'70%'}]}
  }
});
const currentLineData = computed(() => lineData[selectedLine.value]);
</script>