<template>
  <div class="dashboard-grid">
    <div class="card">
      <div class="card-header">
        <div class="card-title">📦 현재 재고</div>
        <div class="status-indicator status-good"></div>
      </div>
      <div class="inventory-chart">
        <v-chart class="chart" :option="gaugeOption" autoresize />
      </div>
      <div class="metric">
        <span>완제품</span>
        <span class="metric-value">2,847개</span>
      </div>
      <div class="metric">
        <span>반제품</span>
        <span class="metric-value">394개</span>
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
</template>

<script setup>
import { ref, provide, onMounted } from 'vue';
import { use } from 'echarts/core';
import { CanvasRenderer } from 'echarts/renderers';
import { GaugeChart } from 'echarts/charts';
import { TooltipComponent } from 'echarts/components';
import VChart, { THEME_KEY } from 'vue-echarts';

use([CanvasRenderer, GaugeChart, TooltipComponent]);
provide(THEME_KEY, 'dark');

const gaugeOption = ref({});

function setupGaugeChart() {
    const safetyStockRate = 112; // 샘플 데이터 (실제 데이터로 대체 예정)
    gaugeOption.value = {
        backgroundColor: 'transparent',
        series: [{
            type: 'gauge',
            center: ['50%', '60%'],
            startAngle: 200,
            endAngle: -20,
            min: 0,
            max: 200,
            splitNumber: 10,
            progress: { show: true, width: 18 },
            axisLine: {
                lineStyle: {
                    width: 18,
                    // ✨✨✨ --- 게이지 바 색상 변경 (대비되는 색상으로 확실히 수정) --- ✨✨✨
                    color: [ 
                        [0.5, '#e74c3c'], // 0% ~ 50% (위험): 빨간색
                        [0.8, '#f39c12'], // 50% ~ 80% (주의): 주황색
                        [1, '#27ae60']    // 80% ~ 100% (정상): 초록색
                    ]
                }
            },
            // ✨✨✨ --- 바늘 색상 유지 (선명하게) --- ✨✨✨
            pointer: {
                show: true,
                length: '60%',
                width: 6,
                itemStyle: {
                    color: '#FFD700' // 골드 색상
                }
            },
            axisTick: { show: false },
            splitLine: { show: false },
            axisLabel: { show: false },
            anchor: { show: false },
            title: {
                show: true,
                offsetCenter: [0, '80%'], // '안전 재고율' 텍스트 위치
                fontSize: 14,
                color: '#ccc'
            },
            detail: {
                valueAnimation: true,
                fontSize: 24,
                offsetCenter: [0, '45%'], // ✨✨✨ --- 퍼센트 값을 아래로 이동 --- ✨✨✨
                formatter: '{value}%',
                // ✨✨✨ --- 값 글자 색상 유지 (선명하게) --- ✨✨✨
                color: '#FFD700' // 골드 색상
            },
            data: [{ value: safetyStockRate, name: '안전 재고율' }]
        }]
    };
}

onMounted(() => {
    setupGaugeChart();
});
</script>

<style scoped>
.inventory-chart {
  height: 180px;
  width: 100%;
  margin-bottom: 1rem; /* 하단 여백 추가 */
}
.chart {
  height: 100%;
  width: 100%;
}

/* 기존 스타일들 유지 */
.dashboard-grid{display:grid;grid-template-columns:repeat(auto-fit,minmax(320px,1fr));gap:1.5rem}.card{background:rgba(28,49,58,.85);backdrop-filter:blur(10px);border-radius:16px;padding:1.5rem;box-shadow:0 8px 32px rgba(0,0,0,.3);border:1px solid rgba(77,208,225,.2);transition:transform .3s ease,box-shadow .3s ease}.card:hover{transform:translateY(-5px);box-shadow:0 12px 40px rgba(0,0,0,.4)}.card-header{display:flex;justify-content:space-between;align-items:center;margin-bottom:1rem;padding-bottom:.5rem;border-bottom:2px solid hsla(0,0%,100%,.1)}.card-title{font-size:1.1rem;font-weight:600;color:#f5f5f5}.metric{display:flex;justify-content:space-between;align-items:center;margin:.5rem 0;padding:.5rem;background:rgba(0,0,0,.2);border-radius:8px}.metric-value{font-size:1.2rem;font-weight:700;color:#4dd0e1}.progress-bar{width:100%;height:8px;background:rgba(0,0,0,.3);border-radius:4px;overflow:hidden;margin:.5rem 0}.progress-fill{height:100%;background:linear-gradient(90deg,#0097a7,#4dd0e1);border-radius:4px;transition:width .3s ease}.factory-container{grid-column:span 2;min-height:450px;display:flex;flex-direction:column}.viewer-wrapper{flex-grow:1;position:relative;overflow:hidden;margin-top:.5rem}.factory-controls{display:flex;gap:.5rem}.factory-btn{padding:.5rem 1rem;background:rgba(0,0,0,.7);color:#fff;border:none;border-radius:6px;cursor:pointer;font-size:.8rem;text-decoration:none}
.machine-info-panel{position:absolute;bottom:1rem;left:1rem;background:rgba(0,0,0,.85);backdrop-filter:blur(5px);color:#fff;padding:1rem;border-radius:8px;font-size:.9rem;width:280px;border:1px solid rgba(77,208,225,.2);transition:opacity .3s ease}.machine-info-panel.hidden{opacity:0;pointer-events:none}
@media (max-width:1200px){.factory-container{grid-column:span 1}}
.status-indicator{width:12px;height:12px;border-radius:50%;animation:pulse 2s infinite}.status-good{background-color:#27ae60}.status-warning{background-color:#f39c12}.status-danger{background-color:#e74c3c}@keyframes pulse{0%{box-shadow:0 0 0 0 rgba(39,174,96,.4)}70%{box-shadow:0 0 0 10px transparent}to{box-shadow:0 0 0 0 transparent}}.equipment-status{display:grid;grid-template-columns:repeat(auto-fit,minmax(120px,1fr));gap:1rem;margin-top:1rem}.equipment-item{text-align:center;padding:1rem;background:rgba(0,0,0,.2);border-radius:8px;transition:transform .3s ease;cursor:pointer}.equipment-item:hover{transform:scale(1.05)}.equipment-icon{font-size:2rem;margin-bottom:.5rem}.alert-box{background:linear-gradient(135deg,#e74c3c,#c0392b);color:#fff;padding:1rem;border-radius:8px;margin:.5rem 0;display:flex;align-items:center;gap:.5rem;animation:alertPulse 2s infinite}@keyframes alertPulse{0%,to{opacity:1}50%{opacity:.8}}



.wide-card { grid-column: span 2; }
.line-selector{display:flex;gap:.5rem;margin-bottom:1rem;flex-wrap:wrap}.line-btn{padding:.5rem 1rem;background:rgba(0,0,0,.2);border:2px solid transparent;border-radius:20px;cursor:pointer;transition:all .3s ease;color:#e0e0e0}.line-btn.active{background:#00bcd4;color:#fff;border-color:#0097a7}
.info-section { padding: 0.5rem 0; }
.info-section:first-child { padding-top: 0; }
.info-section.realtime-data { border-top: 1px solid rgba(255, 255, 255, 0.1); margin-top: 0.5rem; padding-top: 1rem; }
.info-item { display: flex; justify-content: space-between; align-items: center; font-size: 0.85rem; margin-bottom: 0.5rem; }
.info-item span:first-child { color: #bdc3c7; }
.info-item .metric-value { font-size: 1rem; color: #4dd0e1; font-weight: 600; }
.info-item .defect-rate { color: #e74c3c; }
.loading-text { font-size: 0.85rem; color: #f39c12; text-align: center; padding: 1rem 0; }

.metric-value.status-running { color: #27ae60; }
.metric-value.status-stopped { color: #e74c3c; }
.metric-value.status-unknown { color: #f39c12; }
</style>