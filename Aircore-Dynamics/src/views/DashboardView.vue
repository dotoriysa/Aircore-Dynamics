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

      <div class="tab-content active">
        <DashboardTab v-if="activeTab === 'dashboard'" />
        <LinesTab v-else-if="activeTab === 'lines'" />
        <PredictionTab v-else-if="activeTab === 'prediction'" />
        <InventoryTab v-else-if="activeTab === 'inventory'" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue';
import DashboardTab from '../components/dashboard/DashboardTab.vue';
import LinesTab from '../components/dashboard/LinesTab.vue';
import PredictionTab from '../components/dashboard/PredictionTab.vue';
import InventoryTab from '../components/dashboard/InventoryTab.vue';

// --- State ---
const currentTime = ref(new Date().toLocaleString('ko-KR'));
const activeTab = ref('dashboard');
const tabs = [
  { id: 'dashboard', name: '대시보드' },
  { id: 'lines', name: '라인별 상세' },
  { id: 'prediction', name: '예측 분석' },
  { id: 'inventory', name: '재고/출하' },
];
let timeInterval;

// --- Lifecycle ---
onMounted(() => {
  timeInterval = setInterval(() => {
    currentTime.value = new Date().toLocaleString('ko-KR');
  }, 1000);
});

onUnmounted(() => {
  clearInterval(timeInterval);
});
</script>

<style>
/* 공통 스타일은 그대로 유지합니다 */
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