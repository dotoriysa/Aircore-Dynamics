<template>
  <div id="app">
    <div class="header">
      <div class="logo">🏭 AirCore Dynamics</div>
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
              <div class="card-title">🏭 3D 공장 뷰어</div>
            </div>
            <div class="viewer-wrapper">
              <ThreeViewer
                :machine-info="processMachineInfo"
                @object-selected="updateInfoPanel"
              />
              <div class="factory-controls">
                <router-link to="/view" target="_blank" class="factory-btn">
                  🖼️ 전체화면
                </router-link>
              </div>
              <div class="machine-info-panel" :class="{ hidden: !selectedMachine }">
                <div v-if="selectedMachine">
                  <strong>ID:</strong> {{ selectedMachine.PM_ID }}<br>
                  <strong>이름:</strong> {{ selectedMachine.Machine_Name }}<br>
                  <strong>공정:</strong> {{ selectedMachine.Process_Name }}
                </div>
              </div>
            </div>
          </div>
          <div class="card">
            <div class="card-header">
              <div class="card-title">📊 실시간 생산 현황</div>
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
          </div>
          </div>
      </div>
      <div v-else class="tab-content active">
        <h2>{{ tabs.find(t => t.id === activeTab).name }} 페이지</h2>
        <p>이곳에 해당 탭의 콘텐츠를 구성합니다.</p>
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
let timeInterval, apiInterval;

// --- API Data ---
const apiData = reactive({
  total_operation_rate: 84.2,
  daily_total_production: 847,
});
// This would be your actual API call
async function fetchData() {
  console.log("Fetching data from API...");
  // Example of updating data, simulating an API call
  apiData.total_operation_rate = (Math.random() * 10 + 80).toFixed(1);
  apiData.daily_total_production = Math.floor(Math.random() * 100 + 800);
}

const productionProgress = computed(() =>
  Math.min(100, (apiData.daily_total_production / 1100) * 100)
);

// --- Machine Data (can be loaded from API as well) ---
const processMachineInfo = [
    {PM_ID: 'PM001', Process_Name: '주조', Machine_Name: '주조기1'},
    {PM_ID: 'PM002', Process_Name: '주조', Machine_Name: '주조기2'},
    {PM_ID: 'PM003', Process_Name: '주조', Machine_Name: '주조기3'},
    {PM_ID: 'PM004', Process_Name: '가공', Machine_Name: '가공기1'},
    {PM_ID: 'PM005', Process_Name: '가공', Machine_Name: '가공기2'},
    {PM_ID: 'PM006', Process_Name: '검사', Machine_Name: '검사장비'},
    {PM_ID: 'PM007', Process_Name: '조립', Machine_Name: '조립기'},
    {PM_ID: 'PM008', Process_Name: '포장', Machine_Name: '포장기'}
];

// --- Event Handlers ---
function updateInfoPanel(data) {
  selectedMachine.value = data;
}

// --- Lifecycle ---
onMounted(() => {
  timeInterval = setInterval(() => {
    currentTime.value = new Date().toLocaleString('ko-KR');
  }, 1000);
  
  fetchData(); // Initial fetch
  apiInterval = setInterval(fetchData, 5000); // Fetch data every 5 seconds
});

onUnmounted(() => {
  clearInterval(timeInterval);
  clearInterval(apiInterval);
});
</script>

<style>
/* --- 기본 및 대시보드 스타일 (전역) --- */
* { margin: 0; padding: 0; box-sizing: border-box; }
body {
  font-family: 'Malgun Gothic', 'Apple SD Gothic Neo', sans-serif;
  background: linear-gradient(135deg, #0f2027 0%, #203a43 50%, #2c5364 100%);
  color: #e0e0e0;
  min-height: 100vh;
}
/* Factory_Dashboard.html의 모든 CSS를 여기에 복사합니다. */
.header{background:rgba(28,49,58,.85);backdrop-filter:blur(10px);padding:1rem 2rem;box-shadow:0 2px 20px rgba(77,208,225,.1);display:flex;justify-content:space-between;align-items:center;position:sticky;top:0;z-index:1000;border-bottom:1px solid rgba(77,208,225,.2)}.logo{font-size:1.8rem;font-weight:700;color:#fff}.time-display{font-size:1.1rem;color:#e0e0e0}.container{max-width:1600px;margin:0 auto;padding:2rem}.tab-navigation{display:flex;gap:.5rem;margin-bottom:2rem;background:rgba(0,0,0,.2);backdrop-filter:blur(10px);border-radius:12px;padding:.5rem;flex-wrap:wrap}.tab-btn{padding:.75rem 1.5rem;background:transparent;color:#e0e0e0;border:none;border-radius:8px;cursor:pointer;transition:all .3s ease;font-weight:500}.tab-btn.active{background:rgba(77,208,225,.3);color:#fff;backdrop-filter:blur(10px);transform:translateY(-2px);box-shadow:0 4px 15px rgba(0,0,0,.2)}.tab-content{display:none;animation:fadeIn .5s ease}.tab-content.active{display:block}@keyframes fadeIn{0%{opacity:0;transform:translateY(20px)}to{opacity:1;transform:translateY(0)}}.dashboard-grid{display:grid;grid-template-columns:repeat(auto-fit,minmax(320px,1fr));gap:1.5rem}.card{background:rgba(28,49,58,.85);backdrop-filter:blur(10px);border-radius:16px;padding:1.5rem;box-shadow:0 8px 32px rgba(0,0,0,.3);border:1px solid rgba(77,208,225,.2);transition:transform .3s ease,box-shadow .3s ease}.card:hover{transform:translateY(-5px);box-shadow:0 12px 40px rgba(0,0,0,.4)}.card-header{display:flex;justify-content:space-between;align-items:center;margin-bottom:1rem;padding-bottom:.5rem;border-bottom:2px solid hsla(0,0%,100%,.1)}.card-title{font-size:1.1rem;font-weight:600;color:#f5f5f5}.metric{display:flex;justify-content:space-between;align-items:center;margin:.5rem 0;padding:.5rem;background:rgba(0,0,0,.2);border-radius:8px}.metric-value{font-size:1.2rem;font-weight:700;color:#4dd0e1}.progress-bar{width:100%;height:8px;background:rgba(0,0,0,.3);border-radius:4px;overflow:hidden;margin:.5rem 0}.progress-fill{height:100%;background:linear-gradient(90deg,#0097a7,#4dd0e1);border-radius:4px;transition:width .3s ease}.factory-container{grid-column:span 2;min-height:450px;display:flex;flex-direction:column}.viewer-wrapper{flex-grow:1;position:relative;overflow:hidden;margin-top:1rem}.factory-controls{position:absolute;top:1rem;right:1rem;display:flex;gap:.5rem}.factory-btn{padding:.5rem 1rem;background:rgba(0,0,0,.7);color:#fff;border:none;border-radius:6px;cursor:pointer;font-size:.8rem;text-decoration:none}.machine-info-panel{position:absolute;bottom:1rem;left:1rem;background:rgba(0,0,0,.8);color:#fff;padding:1rem;border-radius:8px;font-size:.9rem;max-width:300px;transition:opacity .3s ease}.machine-info-panel.hidden{opacity:0;pointer-events:none}@media (max-width:1200px){.factory-container{grid-column:span 1}}
</style>