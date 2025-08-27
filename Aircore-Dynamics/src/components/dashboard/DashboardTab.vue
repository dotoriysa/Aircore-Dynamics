<template>
  <div class="dashboard-grid">
    <div class="card factory-container">
      <div class="card-header">
        <div class="card-title">3D 공장 뷰어</div>
        <div class="factory-controls">
          <button class="factory-btn" @click="toggleViewerAnimation">
            <span>{{ isAnimationRunning ? '⏸️ 일시정지' : '▶️ 시작' }}</span>
          </button>
          <button class="factory-btn" @click="zoomIn">➕</button>
          <button class="factory-btn" @click="zoomOut">➖</button>
          <router-link to="/view" target="_blank" class="factory-btn">
            🖼️ 전체화면
          </router-link>
        </div>
      </div>
      <div class="viewer-wrapper">
        <ThreeViewer
          ref="viewerRef"
          :machine-info="processMachineInfo"
          @object-selected="updateInfoPanel"
        />
        <div class="machine-info-panel" :class="{ hidden: !selectedMachine }">
            <div v-if="selectedMachine">
            <strong>ID:</strong> {{ selectedMachine.PM_ID }}<br>
            <strong>이름:</strong> {{ selectedMachine.Machine_Name }}<br>
            <strong>공정:</strong> {{ selectedMachine.Process_Name }}<br>
            <strong>표준 사이클 타임:</strong> {{ selectedMachine.Standard_Cycle_Time }}s<br>
            <strong>설명:</strong> {{ selectedMachine.Description }}
          </div>
        </div>
      </div>
    </div>

    <div class="card">
      <div class="card-header">
        <div class="card-title">📊 실시간 생산 현황</div>
        <div class="status-indicator status-good"></div>
      </div>
      <div class="metric">
        <span>금일 생산량</span>
        <span class="metric-value">{{ apiData.daily_total_production }} / 1100</span>
      </div>
      <div class="progress-bar">
        <div class="progress-fill" :style="{ width: productionProgress + '%' }"></div>
      </div>
      <div class="metric">
        <span>전체 가동률</span>
        <span class="metric-value">{{ apiData.total_operation_rate }}%</span>
      </div>
        <div class="production-chart">
        <div
          v-for="(bar, index) in chartData"
          :key="index"
          class="chart-bar"
          :style="{ height: bar.height + 'px' }"
        >
          {{ bar.value }}
        </div>
      </div>
    </div>
    <div class="card">
      <div class="card-header">
        <div class="card-title">🎯 품질 현황</div>
        <div :class="['status-indicator', apiData.defect_rate > 5 ? 'status-warning' : 'status-good']"></div>
      </div>
      <div class="metric">
        <span>검사 통과율</span>
        <span class="metric-value">{{ apiData.normal_rate }}%</span>
      </div>
      <div class="metric">
        <span>불량률</span>
        <span class="metric-value" style="color: #e74c3c;">{{ apiData.defect_rate }}%</span>
      </div>
        <div style="margin-top: 1rem; font-size: 0.8rem; color: #7f8c8d;">
          • 기공: 1.8%<br>
          • 치수오차: 0.9%<br>
          • 조립불량: 0.5%
      </div>
    </div>
    <div class="card">
      <div class="card-header">
        <div class="card-title">⚙️ 설비 상태</div>
        <div class="status-indicator status-good"></div>
      </div>
      <div class="equipment-status">
        <div class="equipment-item">
          <div class="equipment-icon">🔥</div>
          <div style="font-size: 0.8rem;">용해로</div>
          <div style="color: #27ae60; font-size: 0.8rem;">정상</div>
        </div>
        <div class="equipment-item">
          <div class="equipment-icon">⚡</div>
          <div style="font-size: 0.8rem;">CNC</div>
          <div style="color: #27ae60; font-size: 0.8rem;">정상</div>
        </div>
        <div class="equipment-item">
          <div class="equipment-icon">🔍</div>
          <div style="font-size: 0.8rem;">검사대</div>
          <div style="color: #f39c12; font-size: 0.8rem;">주의</div>
        </div>
        <div class="equipment-item">
          <div class="equipment-icon">🤖</div>
          <div style="font-size: 0.8rem;">조립기</div>
          <div style="color: #27ae60; font-size: 0.8rem;">정상</div>
        </div>
      </div>
    </div>
    <div class="card">
      <div class="card-header">
        <div class="card-title">🌡️ 환경 모니터링</div>
        <div class="status-indicator status-good"></div>
      </div>
      <div class="metric">
        <span>온도</span>
        <span class="metric-value">{{ apiData.factory_temperature }}</span>
      </div>
      <div class="metric">
        <span>습도</span>
        <span class="metric-value">{{ apiData.factory_humidity }}</span>
      </div>
        <div class="metric">
        <span>총 전력 소비량</span>
        <span class="metric-value">{{ apiData.total_power_consumption }} kW</span>
      </div>
    </div>
    <div class="card">
      <div class="card-header">
        <div class="card-title">🚨 최근 알람</div>
        <div class="status-indicator status-warning"></div>
      </div>
      <div class="alert-box">
        <span class="alert-icon">⚠️</span>
        <div>
          <div style="font-weight: bold;">검사대 #2 진동 이상</div>
          <div style="font-size: 0.8rem; opacity: 0.9;">2분 전</div>
        </div>
      </div>
        <div style="background: rgba(243, 156, 18, 0.1); color: #f39c12; padding: 0.5rem; border-radius: 8px; margin: 0.5rem; font-size: 0.8rem;">
        <span class="alert-icon">🔧</span>
        CNC #3 공구 교체 필요 - 15분 전
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted, computed } from 'vue';
import { RouterLink } from 'vue-router';
import ThreeViewer from '../ThreeViewer.vue';

// --- State ---
const selectedMachine = ref(null);
const viewerRef = ref(null);
const isAnimationRunning = ref(true);
let apiInterval;

// --- API Data ---
const apiData = reactive({
  total_operation_rate: 0,
  daily_total_production: 0,
  operating_machines: 0,
  factory_temperature: '',
  factory_humidity: '',
  total_power_consumption: 0,
  defect_rate: 0,
  normal_rate: 0,
});

const chartData = ref([]);

async function fetchData() {
  try {
    const response = await fetch('/api/main-dashboard/summary');
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }
    const data = await response.json();
    console.log("API Data received:", data);

    // API 응답 데이터를 Vue 반응형 상태에 할당
    apiData.total_operation_rate = parseFloat(data.total_operation_rate);
    apiData.daily_total_production = parseInt(data.daily_total_production);
    apiData.operating_machines = parseInt(data.operating_machines);
    apiData.factory_temperature = data.factory_temperature;
    apiData.factory_humidity = data.factory_humidity;
    apiData.total_power_consumption = parseFloat(data.total_power_consumption);
    apiData.defect_rate = parseFloat(data.defect_rate);
    apiData.normal_rate = parseFloat(data.normal_rate);
    
    // 차트 데이터는 임시로 유지하거나 API에서 받아오는 형식에 맞춰 수정 가능
    chartData.value = [
      { value: 42, height: 120 }, { value: 38, height: 90 }, { value: 51, height: 150 },
      { value: 45, height: 100 }, { value: 49, height: 140 }, { value: 41, height: 95 },
      { value: 47, height: 130 }
    ].map(bar => ({ ...bar, height: Math.random() * 100 + 50 }));

  } catch (error) {
    console.error("Failed to fetch dashboard summary:", error);
    // API 호출 실패 시 대체 값 설정 (선택 사항)
    apiData.total_operation_rate = 0;
    apiData.daily_total_production = 0;
  }
}

const productionProgress = computed(() =>
  Math.min(100, (apiData.daily_total_production / 1100) * 100)
);

// --- Machine Data ---
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

// --- Event Handlers ---
function updateInfoPanel(data) {
  selectedMachine.value = data;
}

function toggleViewerAnimation() {
  const running = viewerRef.value?.toggleAnimation();
  isAnimationRunning.value = running;
}

function zoomIn() {
  viewerRef.value?.moveCamera('zoom-in');
}

function zoomOut() {
  viewerRef.value?.moveCamera('zoom-out');
}

// --- Lifecycle ---
onMounted(() => {
  fetchData();
  apiInterval = setInterval(fetchData, 5000);
});

onUnmounted(() => {
  clearInterval(apiInterval);
});
</script>

<style scoped>
/* (스타일 태그 내용은 이전과 동일) */
/* 기존 스타일 코드는 여기에 그대로 유지 */
.dashboard-grid{display:grid;grid-template-columns:repeat(auto-fit,minmax(320px,1fr));gap:1.5rem}.card{background:rgba(28,49,58,.85);backdrop-filter:blur(10px);border-radius:16px;padding:1.5rem;box-shadow:0 8px 32px rgba(0,0,0,.3);border:1px solid rgba(77,208,225,.2);transition:transform .3s ease,box-shadow .3s ease}.card:hover{transform:translateY(-5px);box-shadow:0 12px 40px rgba(0,0,0,.4)}.card-header{display:flex;justify-content:space-between;align-items:center;margin-bottom:1rem;padding-bottom:.5rem;border-bottom:2px solid hsla(0,0%,100%,.1)}.card-title{font-size:1.1rem;font-weight:600;color:#f5f5f5}.metric{display:flex;justify-content:space-between;align-items:center;margin:.5rem 0;padding:.5rem;background:rgba(0,0,0,.2);border-radius:8px}.metric-value{font-size:1.2rem;font-weight:700;color:#4dd0e1}.progress-bar{width:100%;height:8px;background:rgba(0,0,0,.3);border-radius:4px;overflow:hidden;margin:.5rem 0}.progress-fill{height:100%;background:linear-gradient(90deg,#0097a7,#4dd0e1);border-radius:4px;transition:width .3s ease}.factory-container{grid-column:span 2;min-height:450px;display:flex;flex-direction:column}.viewer-wrapper{flex-grow:1;position:relative;overflow:hidden;margin-top:.5rem}.factory-controls{display:flex;gap:.5rem}.factory-btn{padding:.5rem 1rem;background:rgba(0,0,0,.7);color:#fff;border:none;border-radius:6px;cursor:pointer;font-size:.8rem;text-decoration:none}.machine-info-panel{position:absolute;bottom:1rem;left:1rem;background:rgba(0,0,0,.8);color:#fff;padding:1rem;border-radius:8px;font-size:.9rem;max-width:300px;transition:opacity .3s ease}.machine-info-panel.hidden{opacity:0;pointer-events:none}@media (max-width:1200px){.factory-container{grid-column:span 1}}
.status-indicator{width:12px;height:12px;border-radius:50%;animation:pulse 2s infinite}.status-good{background-color:#27ae60}.status-warning{background-color:#f39c12}.status-danger{background-color:#e74c3c}@keyframes pulse{0%{box-shadow:0 0 0 0 rgba(39,174,96,.4)}70%{box-shadow:0 0 0 10px transparent}to{box-shadow:0 0 0 0 transparent}}.equipment-status{display:grid;grid-template-columns:repeat(auto-fit,minmax(120px,1fr));gap:1rem;margin-top:1rem}.equipment-item{text-align:center;padding:1rem;background:rgba(0,0,0,.2);border-radius:8px;transition:transform .3s ease;cursor:pointer}.equipment-item:hover{transform:scale(1.05)}.equipment-icon{font-size:2rem;margin-bottom:.5rem}.alert-box{background:linear-gradient(135deg,#e74c3c,#c0392b);color:#fff;padding:1rem;border-radius:8px;margin:.5rem 0;display:flex;align-items:center;gap:.5rem;animation:alertPulse 2s infinite}@keyframes alertPulse{0%,to{opacity:1}50%{opacity:.8}}.production-chart{height:150px;background:rgba(0,0,0,.2);border-radius:8px;position:relative;overflow:hidden;margin:1rem 0;display:flex;align-items:flex-end;justify-content:space-around;padding:1rem}.chart-bar{background:linear-gradient(0deg,#0097a7,#4dd0e1);border-radius:4px 4px 0 0;width:30px;display:flex;align-items:flex-end;justify-content:center;color:#fff;font-size:.7rem;font-weight:700;padding-bottom:.25rem;transition:height 1s ease}
.wide-card { grid-column: span 2; }
.line-selector{display:flex;gap:.5rem;margin-bottom:1rem;flex-wrap:wrap}.line-btn{padding:.5rem 1rem;background:rgba(0,0,0,.2);border:2px solid transparent;border-radius:20px;cursor:pointer;transition:all .3s ease;color:#e0e0e0}.line-btn.active{background:#00bcd4;color:#fff;border-color:#0097a7}
</style>