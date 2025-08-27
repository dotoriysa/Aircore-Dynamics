<template>
  <div id="app-container">
    <div class="header">
      <div class="logo" style="display: flex; align-items: center;">
        <img src="/aircore icon.png" alt="AirCore Dynamics Logo" style="width: 32px; height: 32px; margin-right: 12px;">
        AirCore Dynamics
      </div>
      <div class="time-display">{{ currentTime }}</div>
    </div>

    <div class="container">
      <div class="tab-navigation">
        <button
          v-for="tab in tabs"
          :key="tab.id"
          :class="['tab-btn', { active: activeTab === tab.id }]"
          @click="activeTab = tab.id"
        >
          {{ tab.name }}
        </button>
      </div>

      <div v-if="activeTab === 'dashboard'" class="tab-content active">
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
      </div>
      
      <div v-else-if="activeTab === 'lines'" class="tab-content active">
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
      <div v-else-if="activeTab === 'prediction'" class="tab-content active">
        <div class="dashboard-grid">
          <div class="card wide-card">
            <div class="card-header">
              <div class="card-title">📈 생산량 예측</div>
              <div class="status-indicator status-good"></div>
            </div>
            <div class="metric">
              <span>예상 일일 생산량</span>
              <span class="metric-value">1,087개</span>
            </div>
            <div class="metric">
              <span>목표 달성률</span>
              <span class="metric-value">98.8%</span>
            </div>
          </div>
          <div class="card">
            <div class="card-header">
              <div class="card-title">🔍 병목 구간 분석</div>
              <div class="status-indicator status-warning"></div>
            </div>
            <div style="font-size: 0.9rem;">
              <div style="color: #e74c3c; margin: 0.5rem 0;">
                <strong>주요 병목:</strong> <span>검사 라인</span>
              </div>
              <div style="color: #7f8c8d; font-size: 0.8rem;">
                • 평균 대기시간: <span>8.3</span>분<br>
                • 처리속도: <span>85</span>% 목표대비<br>
                • 권장사항: <span>검사대 추가 운영</span>
              </div>
            </div>
          </div>
        </div>
      </div>
      <div v-else-if="activeTab === 'inventory'" class="tab-content active">
        <div class="dashboard-grid">
          <div class="card">
            <div class="card-header">
              <div class="card-title">📦 현재 재고</div>
              <div class="status-indicator status-good"></div>
            </div>
            <div class="metric">
              <span>완제품</span>
              <span class="metric-value">2,847개</span>
            </div>
            <div class="metric">
              <span>반제품</span>
              <span class="metric-value">394개</span>
            </div>
            <div class="metric">
              <span>안전 재고율</span>
              <span class="metric-value">112%</span>
            </div>
          </div>
          <div class="card">
            <div class="card-header">
              <div class="card-title">🚚 출하 현황</div>
              <div class="status-indicator status-good"></div>
            </div>
            <div class="metric">
              <span>금일 출하량</span>
              <span class="metric-value">1,150개</span>
            </div>
            <div class="metric">
              <span>대기 출하량</span>
              <span class="metric-value">850개</span>
            </div>
            <div class="metric">
              <span>예정 출하</span>
              <span class="metric-value">내일 1,200개</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted, computed } from 'vue';
import { RouterLink } from 'vue-router';
import ThreeViewer from '../components/ThreeViewer.vue';

// --- State ---
const currentTime = ref(new Date().toLocaleString('ko-KR'));
const activeTab = ref('dashboard');
const tabs = [
  { id: 'dashboard', name: '대시보드' },
  { id: 'lines', name: '라인별 상세' },
  { id: 'prediction', name: '예측 분석' },
  { id: 'inventory', name: '재고/출하' },
];
const selectedMachine = ref(null);
const viewerRef = ref(null);
const isAnimationRunning = ref(true);
let timeInterval, apiInterval;

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

// --- Machine Data (✨ 상세 정보 추가) ---
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

// --- 라인별 상세 탭 데이터 ---
const selectedLine = ref('casting');
const lineData = reactive({
  casting: {
    title: '주조 라인',
    prod: { title: '주조 라인 - 생산 현황', status: 'status-good', metrics: [{label:'시간당 생산량',value:'47개'},{label:'가동률',value:'89.2%'},{label:'불량률',value:'2.1%'}]},
    equip: { title: '설비별 상태', status: 'status-good', metrics: [{label:'용해로 온도',value:'742°C'},{label:'주조 압력',value:'85 bar'},{label:'냉각 시간',value:'12.3분'}]}
  },
  machining: {
    title: '가공 라인',
    prod: { title: '가공 라인 - 생산 현황', status: 'status-good', metrics: [{label:'시간당 생산량',value:'55개'},{label:'가동률',value:'91.5%'},{label:'불량률',value:'0.9%'}]},
    equip: { title: '설비별 상태', status: 'status-good', metrics: [{label:'CNC 스핀들 속도',value:'12,000 RPM'},{label:'절삭유량',value:'25 L/min'}]}
  },
  inspection: {
    title: '검사 라인',
    prod: { title: '검사 라인 - 생산 현황', status: 'status-warning', metrics: [{label:'시간당 처리량',value:'60개'},{label:'가동률',value:'78.0%'},{label:'불량 발견율',value:'99.5%'}]},
    equip: { title: '설비별 상태', status: 'status-warning', metrics: [{label:'검사기 #2 진동',value:'이상 (Warning)'},{label:'센서 민감도',value:'정상'}]}
  },
  assembly: {
    title: '조립 라인',
    prod: { title: '조립 라인 - 생산 현황', status: 'status-good', metrics: [{label:'시간당 조립량',value:'40개'},{label:'가동률',value:'85.5%'},{label:'조립불량률',value:'0.5%'}]},
    equip: { title: '설비별 상태', status: 'status-good', metrics: [{label:'로봇 팔 상태',value:'정상'},{label:'토크 값',value:'기준치 내'}]}
  },
  packaging: {
    title: '포장/출하',
    prod: { title: '포장/출하 라인 - 생산 현황', status: 'status-good', metrics: [{label:'시간당 포장량',value:'65개'},{label:'가동률',value:'95.0%'}]},
    equip: { title: '설비별 상태', status: 'status-good', metrics: [{label:'컨베이어 속도',value:'1.2 m/s'},{label:'포장재 잔량',value:'70%'}]}
  }
});
const currentLineData = computed(() => lineData[selectedLine.value]);


// --- Lifecycle ---
onMounted(() => {
  timeInterval = setInterval(() => {
    currentTime.value = new Date().toLocaleString('ko-KR');
  }, 1000);
  
  fetchData();
  apiInterval = setInterval(fetchData, 5000);
});

onUnmounted(() => {
  clearInterval(timeInterval);
  clearInterval(apiInterval);
});
</script>

<style>
/* (스타일 태그 내용은 이전과 동일) */
* { margin: 0; padding: 0; box-sizing: border-box; }
#app-container { font-family: 'Malgun Gothic', 'Apple SD Gothic Neo', sans-serif; }
body {
  background: linear-gradient(135deg, #0f2027 0%, #203a43 50%, #2c5364 100%);
  color: #e0e0e0;
  min-height: 100vh;
}
.header{background:rgba(28,49,58,.85);backdrop-filter:blur(10px);padding:1rem 2rem;box-shadow:0 2px 20px rgba(77,208,225,.1);display:flex;justify-content:space-between;align-items:center;position:sticky;top:0;z-index:1000;border-bottom:1px solid rgba(77,208,225,.2)}.logo{font-size:1.8rem;font-weight:700;color:#fff}.time-display{font-size:1.1rem;color:#e0e0e0}.container{max-width:1600px;margin:0 auto;padding:2rem}.tab-navigation{display:flex;gap:.5rem;margin-bottom:2rem;background:rgba(0,0,0,.2);backdrop-filter:blur(10px);border-radius:12px;padding:.5rem;flex-wrap:wrap}.tab-btn{padding:.75rem 1.5rem;background:transparent;color:#e0e0e0;border:none;border-radius:8px;cursor:pointer;transition:all .3s ease;font-weight:500}.tab-btn.active{background:rgba(77,208,225,.3);color:#fff;backdrop-filter:blur(10px);transform:translateY(-2px);box-shadow:0 4px 15px rgba(0,0,0,.2)}.tab-content{display:none;animation:fadeIn .5s ease}.tab-content.active{display:block}@keyframes fadeIn{0%{opacity:0;transform:translateY(20px)}to{opacity:1;transform:translateY(0)}}.dashboard-grid{display:grid;grid-template-columns:repeat(auto-fit,minmax(320px,1fr));gap:1.5rem}.card{background:rgba(28,49,58,.85);backdrop-filter:blur(10px);border-radius:16px;padding:1.5rem;box-shadow:0 8px 32px rgba(0,0,0,.3);border:1px solid rgba(77,208,225,.2);transition:transform .3s ease,box-shadow .3s ease}.card:hover{transform:translateY(-5px);box-shadow:0 12px 40px rgba(0,0,0,.4)}.card-header{display:flex;justify-content:space-between;align-items:center;margin-bottom:1rem;padding-bottom:.5rem;border-bottom:2px solid hsla(0,0%,100%,.1)}.card-title{font-size:1.1rem;font-weight:600;color:#f5f5f5}.metric{display:flex;justify-content:space-between;align-items:center;margin:.5rem 0;padding:.5rem;background:rgba(0,0,0,.2);border-radius:8px}.metric-value{font-size:1.2rem;font-weight:700;color:#4dd0e1}.progress-bar{width:100%;height:8px;background:rgba(0,0,0,.3);border-radius:4px;overflow:hidden;margin:.5rem 0}.progress-fill{height:100%;background:linear-gradient(90deg,#0097a7,#4dd0e1);border-radius:4px;transition:width .3s ease}.factory-container{grid-column:span 2;min-height:450px;display:flex;flex-direction:column}.viewer-wrapper{flex-grow:1;position:relative;overflow:hidden;margin-top:.5rem}.factory-controls{display:flex;gap:.5rem}.factory-btn{padding:.5rem 1rem;background:rgba(0,0,0,.7);color:#fff;border:none;border-radius:6px;cursor:pointer;font-size:.8rem;text-decoration:none}.machine-info-panel{position:absolute;bottom:1rem;left:1rem;background:rgba(0,0,0,.8);color:#fff;padding:1rem;border-radius:8px;font-size:.9rem;max-width:300px;transition:opacity .3s ease}.machine-info-panel.hidden{opacity:0;pointer-events:none}@media (max-width:1200px){.factory-container{grid-column:span 1}}
.status-indicator{width:12px;height:12px;border-radius:50%;animation:pulse 2s infinite}.status-good{background-color:#27ae60}.status-warning{background-color:#f39c12}.status-danger{background-color:#e74c3c}@keyframes pulse{0%{box-shadow:0 0 0 0 rgba(39,174,96,.4)}70%{box-shadow:0 0 0 10px transparent}to{box-shadow:0 0 0 0 transparent}}.equipment-status{display:grid;grid-template-columns:repeat(auto-fit,minmax(120px,1fr));gap:1rem;margin-top:1rem}.equipment-item{text-align:center;padding:1rem;background:rgba(0,0,0,.2);border-radius:8px;transition:transform .3s ease;cursor:pointer}.equipment-item:hover{transform:scale(1.05)}.equipment-icon{font-size:2rem;margin-bottom:.5rem}.alert-box{background:linear-gradient(135deg,#e74c3c,#c0392b);color:#fff;padding:1rem;border-radius:8px;margin:.5rem 0;display:flex;align-items:center;gap:.5rem;animation:alertPulse 2s infinite}@keyframes alertPulse{0%,to{opacity:1}50%{opacity:.8}}.production-chart{height:150px;background:rgba(0,0,0,.2);border-radius:8px;position:relative;overflow:hidden;margin:1rem 0;display:flex;align-items:flex-end;justify-content:space-around;padding:1rem}.chart-bar{background:linear-gradient(0deg,#0097a7,#4dd0e1);border-radius:4px 4px 0 0;width:30px;display:flex;align-items:flex-end;justify-content:center;color:#fff;font-size:.7rem;font-weight:700;padding-bottom:.25rem;transition:height 1s ease}
.wide-card { grid-column: span 2; }
.line-selector{display:flex;gap:.5rem;margin-bottom:1rem;flex-wrap:wrap}.line-btn{padding:.5rem 1rem;background:rgba(0,0,0,.2);border:2px solid transparent;border-radius:20px;cursor:pointer;transition:all .3s ease;color:#e0e0e0}.line-btn.active{background:#00bcd4;color:#fff;border-color:#0097a7}
</style>