<template>
  <div class="dashboard-grid">
    <div class="card factory-container">
      <div class="card-header">
        <div class="card-title">
          <span style="font-size: 1.1rem; font-weight: 600; color: #f5f5f5;">3D 공장 뷰어</span>
        </div>
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
        <div class="status-indicator status-warning"></div>
      </div>
      <div class="metric">
        <span>검사 통과율</span>
        <span class="metric-value">96.8%</span>
      </div>
      <div class="metric">
        <span>불량률</span>
        <span class="metric-value" style="color: #e74c3c;">3.2%</span>
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
        <span class="metric-value">23.4°C</span>
      </div>
      <div class="metric">
        <span>습도</span>
        <span class="metric-value">58.2%</span>
      </div>
        <div class="metric">
        <span>총 전력 소비량</span>
        <span class="metric-value">245.7 kW</span>
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
  total_operation_rate: 84.2,
  daily_total_production: 847,
});

const chartData = ref([]);

async function fetchData() {
  apiData.total_operation_rate = (Math.random() * 10 + 80).toFixed(1);
  apiData.daily_total_production = Math.floor(Math.random() * 100 + 800);
  chartData.value = [
    { value: 42, height: 120 }, { value: 38, height: 90 }, { value: 51, height: 150 },
    { value: 45, height: 100 }, { value: 49, height: 140 }, { value: 41, height: 95 },
    { value: 47, height: 130 }
  ].map(bar => ({ ...bar, height: Math.random() * 100 + 50 }));
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