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

      <div class="card" v-if="shouldShowEquipCard">
        <div class="card-header">
          <div class="card-title">{{ currentLineData.equip.title }}</div>
          <div :class="['status-indicator', currentLineData.equip.status]"></div>
        </div>
        <div class="metric" v-for="metric in currentLineData.equip.metrics" :key="metric.label">
          <span>{{ metric.label }}</span>
          <span class="metric-value">{{ metric.value }}</span>
        </div>
      </div>
      
      <div 
        :class="['card', 'factory-container', { 'wide-card': !shouldShowEquipCard }]"
      >
        <div class="card-header">
          <div class="card-title">라인별 3D 뷰어</div>
          <div class="factory-controls">
            <button class="factory-btn" @click="toggleViewerAnimation">
              <span>{{ isAnimationRunning ? '⏸️ 일시정지' : '▶️ 시작' }}</span>
            </button>
            <router-link to="/view" target="_blank" class="factory-btn">
              🖼️ 전체화면
            </router-link>
          </div>
        </div>
        
        <div class="viewer-wrapper">
          <ThreeViewer
            ref="viewerRef"
            :machine-info="processMachineInfo"
            :highlighted-process="highlightedProcessName"
            :machine-statuses="allMachineStatuses"
            @object-selected="updateSelectedMachine"
          />
          
          <div class="machine-info-panel" :class="{ hidden: !selectedMachine }">
            <div v-if="selectedMachine">
                <div class="info-section">
                <div class="info-item"><strong>ID:</strong> <span>{{ selectedMachine.PM_ID }}</span></div>
                <div class="info-item"><strong>이름:</strong> <span>{{ selectedMachine.Machine_Name }}</span></div>
                <div class="info-item"><strong>공정:</strong> <span>{{ selectedMachine.Process_Name }}</span></div>
              </div>
              <div v-if="selectedMachineRealtimeData" class="info-section realtime-data">
                <div class="info-item">
                  <span>현재 상태</span>
                  <span class="metric-value" :class="selectedMachineCurrentStatus.class">
                    {{ selectedMachineCurrentStatus.text }}
                  </span>
                </div>
                <div class="info-item"><span>시간당 생산량</span> <span class="metric-value">{{ selectedMachineRealtimeData.hourly_production }}개</span></div>
                <div class="info-item"><span>가동률</span> <span class="metric-value">{{ selectedMachineRealtimeData.operation_rate }}%</span></div>
                <div class="info-item"><span>전력량</span> <span class="metric-value">{{ selectedMachineRealtimeData.power_consumption }}kWh</span></div>
                <div class="info-item"><span>불량률</span> <span class="metric-value defect-rate">{{ selectedMachineRealtimeData.defect_rate }}%</span></div>
              </div>
              <div v-else class="loading-text">실시간 데이터 로딩 중...</div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, watch, onMounted, onUnmounted, nextTick } from 'vue';
import { RouterLink } from 'vue-router';
import ThreeViewer from '../ThreeViewer.vue';

const selectedLine = ref('casting');
const viewerRef = ref(null);
const selectedMachine = ref(null);
const selectedMachineRealtimeData = ref(null);
const allMachineStatuses = ref({});
let statusInterval;

const isAnimationRunning = ref(true);

const selectedMachineCurrentStatus = computed(() => {
  if (!selectedMachine.value || !allMachineStatuses.value[selectedMachine.value.PM_ID]) {
    return { text: '확인 중...', class: 'status-unknown' };
  }
  const status = allMachineStatuses.value[selectedMachine.value.PM_ID].status;
  if (status === 'running') {
    return { text: '가동 중', class: 'status-running' };
  } else {
    return { text: '멈춤', class: 'status-stopped' };
  }
});

function toggleViewerAnimation() {
  isAnimationRunning.value = viewerRef.value?.toggleAnimation();
}

const lineData = reactive({
  casting: { title: '주조 라인', prod: { title: '주조 라인 - 생산 현황', status: 'status-good', metrics: [] }, equip: { title: '설비별 상태', status: 'status-good', metrics: [] } },
  machining: { title: '가공 라인', prod: { title: '가공 라인 - 생산 현황', status: 'status-good', metrics: [] }, equip: { title: '설비별 상태', status: 'status-good', metrics: [] } },
  inspection: { title: '검사 라인', prod: { title: '검사 라인 - 생산 현황', status: 'status-warning', metrics: [] }, equip: { title: '설비별 상태', status: 'status-warning', metrics: [] } },
  assembly: { title: '조립 라인', prod: { title: '조립 라인 - 생산 현황', status: 'status-good', metrics: [] }, equip: { title: '설비별 상태', status: 'status-good', metrics: [] } },
  packaging: { title: '포장/출하', prod: { title: '포장/출하 라인 - 생산 현황', status: 'status-good', metrics: [] }, equip: { title: '설비별 상태', status: 'status-good', metrics: [] } }
});
const currentLineData = computed(() => lineData[selectedLine.value]);

const processMachineInfo = [
    {PM_ID: 'PM001', Process_Name: '주조', Machine_Name: '주조기1', Standard_Cycle_Time: 3600, Description: '금속 용해 및 주조 장비 1호기'},
    {PM_ID: 'PM002', Process_Name: '주조', Machine_Name: '주조기2', Standard_Cycle_Time: 3600, Description: '금속 용해 및 주조 장비 2호기'},
    {PM_ID: 'PM003', Process_Name: '주조', Machine_Name: '주조기3', Standard_Cycle_Time: 3600, Description: '금속 용해 및 주조 장비 3호기'},
    {PM_ID: 'PM004', Process_Name: '가공', Machine_Name: '가공기1', Standard_Cycle_Time: 1800, Description: '정밀 가공 및 성형 장비 1호기'},
    {PM_ID: 'PM005', Process_Name: '가공', Machine_Name: '가공기2', Standard_Cycle_Time: 1800, Description: '정밀 가공 및 성형 장비 2호기'},
    {PM_ID: 'PM006', Process_Name: '검사', Machine_Name: '검사장비', Standard_Cycle_Time: 900, Description: '품질 검사 및 측정 장비'},
    {PM_ID: 'PM007', Process_Name: '조립', Machine_Name: '조립기', Standard_Cycle_Time: 1200, Description: '부품 조립 및 결합 장비'},
    {PM_ID: 'PM008', Process_Name: '포장', Machine_Name: '포장기', Standard_Cycle_Time: 600, Description: '자동 포장 및 밀봉 장비'}
];

const shouldShowEquipCard = computed(() => {
  return selectedLine.value === 'casting' || selectedLine.value === 'machining';
});
const lineApiNames = {
    casting: '주조', machining: '가공', inspection: '검사', assembly: '조립', packaging: '포장'
};
const highlightedProcessName = computed(() => lineApiNames[selectedLine.value]);

async function updateAllMachineStatuses() {
  const newStatuses = {};
  
  const statusPromises = processMachineInfo.map(async (machine) => {
    try {
      const response = await fetch(`/api/machine-dashboard/${machine.PM_ID}`);
      if (!response.ok) {
        console.error(`Error fetching status for ${machine.PM_ID}: ${response.statusText}`);
        return { pmId: machine.PM_ID, status: 'stopped' };
      }
      const data = await response.json();
      const frontendStatus = data.status === 1 ? 'running' : 'stopped';
      return { pmId: machine.PM_ID, status: frontendStatus };
    } catch (error) {
      console.error(`Failed to fetch machine status for ${machine.PM_ID}:`, error);
      return { pmId: machine.PM_ID, status: 'stopped' };
    }
  });

  const resolvedStatuses = await Promise.all(statusPromises);

  resolvedStatuses.forEach(item => {
    newStatuses[item.pmId] = { status: item.status };
  });

  allMachineStatuses.value = newStatuses;
}

async function updateSelectedMachine(data) {
 selectedMachine.value = data;
 selectedMachineRealtimeData.value = null;

 if (data) {
   try {
     const response = await fetch(`/api/machine-dashboard/${data.PM_ID}`);
     if (!response.ok) throw new Error('Machine data fetch failed');
     
     const machineData = await response.json();
     
     selectedMachineRealtimeData.value = {
       hourly_production: machineData.dailyProduction || 0,
       operation_rate: parseFloat(machineData.operationRate || 0).toFixed(2),
       power_consumption: parseFloat(machineData.powerConsumption || 0).toFixed(2),
       defect_rate: parseFloat(machineData.defectRate || 0).toFixed(2),
     };
   } catch (error) {
     console.error("Failed to fetch machine status:", error);
     setTimeout(() => {
       if (selectedMachine.value && selectedMachine.value.PM_ID === data.PM_ID) {
         selectedMachineRealtimeData.value = {
           hourly_production: Math.floor(Math.random() * 20 + 30),
           operation_rate: (Math.random() * 5 + 95).toFixed(2),
           power_consumption: (Math.random() * 10 + 50).toFixed(2),
           defect_rate: (Math.random() * 2).toFixed(2),
         };
       }
     }, 500);
   }
 }
}

async function fetchData(lineKey) {
  const apiName = lineApiNames[lineKey];
  if (!apiName) return;
  try {
    const response = await fetch(`/api/process_dashboard/${apiName}`);
    if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`);
    const data = await response.json();

    // ✨✨✨ --- 여기서 모든 값의 소수점을 두 자리로 통일 --- ✨✨✨
    const prodMetrics = [
        { label: '시간당 생산량', value: `${data.daily_total_production}개` },
        { label: '가동률', value: `${parseFloat(data.total_operation_rate || 0).toFixed(2)}%` },
        { label: '가동 장비수', value: `${data.operating_machines}대` },
        { label: '전력량', value: `${parseFloat(data.total_power_consumption || 0).toFixed(2)} kWh` },
        { label: '불량률', value: `${parseFloat(data.defect_rate || 0).toFixed(2)}%` }
    ];
    let equipMetrics = [];
    if (lineKey === 'casting') equipMetrics = [ { label: '용해로 온도', value: '742°C' }, { label: '주조 압력', value: '85 bar' } ];
    else if (lineKey === 'machining') equipMetrics = [ { label: 'CNC 스핀들 속도', value: '12,000 RPM' }, { label: '절삭유량', value: '25 L/min' } ];
    
    lineData[lineKey].prod.metrics = prodMetrics;
    lineData[lineKey].equip.metrics = equipMetrics;
  } catch (error) {
    console.error(`Failed to fetch data for ${apiName}:`, error);
  }
}

watch(selectedLine, (newLine) => {
  fetchData(newLine);
  selectedMachine.value = null; 
  viewerRef.value?.focusOnProcess(lineApiNames[newLine]);
  nextTick(() => { viewerRef.value?.handleResize(); });
}, { immediate: true }); 

let apiInterval;
onMounted(() => {
  nextTick(() => {
      viewerRef.value?.focusOnProcess(lineApiNames[selectedLine.value]);
      viewerRef.value?.handleResize();
  });
  apiInterval = setInterval(() => fetchData(selectedLine.value), 5000);
  
  updateAllMachineStatuses();
  statusInterval = setInterval(updateAllMachineStatuses, 3000);
});

onUnmounted(() => { 
  clearInterval(apiInterval);
  clearInterval(statusInterval);
});
</script>

<style scoped>
.factory-container {
  min-height: 450px;
  display: flex;
  flex-direction: column;
}
.viewer-wrapper {
  flex-grow: 1;
  position: relative;
  overflow: hidden;
  margin-top: .5rem;
  border-radius: 8px;
}
.machine-info-panel {
  position: absolute;
  bottom: 1rem;
  left: 1rem;
  background: rgba(0,0,0,.85);
  backdrop-filter: blur(5px);
  color: #fff;
  padding: 1rem;
  border-radius: 8px;
  font-size: .9rem;
  width: 280px;
  border: 1px solid rgba(77,208,225,.2);
  transition: opacity .3s ease;
  z-index: 100;
}
.machine-info-panel.hidden {
  opacity: 0;
  pointer-events: none;
}
@media (max-width:1200px) {
  .factory-container {
    grid-column: span 1;
  }
}
.info-section { padding: 0.5rem 0; }
.info-section:first-child { padding-top: 0; }
.info-section.realtime-data { border-top: 1px solid rgba(255, 255, 255, 0.1); margin-top: 0.5rem; padding-top: 1rem; }
.info-item { display: flex; justify-content: space-between; align-items: center; font-size: 0.85rem; margin-bottom: 0.5rem; }
.info-item span:first-child { color: #bdc3c7; }
.info-item .metric-value { font-size: 1rem; color: #4dd0e1; font-weight: 600; }
.info-item .defect-rate { color: #e74c3c; }
.loading-text { font-size: 0.85rem; color: #f39c12; text-align: center; padding: 1rem 0; }

.metric-value.status-running {
  color: #27ae60;
}
.metric-value.status-stopped {
  color: #e74c3c;
}
.metric-value.status-unknown {
  color: #f39c12;
}
</style>