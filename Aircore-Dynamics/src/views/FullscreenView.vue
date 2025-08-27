<template>
  <div class="fullscreen-wrapper">
    <ThreeViewer
      ref="viewerRef"
      :machine-info="processMachineInfo"
      @object-selected="updateSelectedEquipment"
    />

    <div class="controls">
      <button class="control-btn" @click="handleToggleAnimation">
        <span id="animationBtn">{{ animationButtonText }}</span>
      </button>
      <button class="control-btn" @click="viewerRef?.resetView()">🔄 리셋 뷰</button>
      <button class="control-btn" @click="viewerRef?.toggleTopView()">📐 탑 뷰</button>
      <button class="control-btn" @click="viewerRef?.focusNextEquipment()">🎯 장비 포커스</button>
      <button class="control-btn" @click="viewerRef?.toggleAutoRotate()">🔄 자동 회전</button>
    </div>

    <div class="status-panel">
      <div class="status-title">🏭 공장 현황</div>
      <div class="status-item">
        <span>전체 장비:</span>
        <span>8대</span>
      </div>
       <div class="status-item">
        <span>가동 중:</span>
        <span style="color: #27ae60;">7대</span>
      </div>
      <div class="status-item">
        <span>주의 필요:</span>
        <span style="color: #f39c12;">1대</span>
      </div>
      <div class="status-item">
        <span>정비 중:</span>
        <span style="color: #e74c3c;">0대</span>
      </div>
      <div class="status-item" v-if="!selectedEquipment">
        <span>선택된 장비:</span>
        <span>없음</span>
      </div>
      <div v-if="selectedEquipment" class="selected-equipment-details">
        <div class="status-item"><span>ID:</span> <span>{{ selectedEquipment.PM_ID }}</span></div>
        <div class="status-item"><span>이름:</span> <span>{{ selectedEquipment.Machine_Name }}</span></div>
        <div class="status-item"><span>공정:</span> <span>{{ selectedEquipment.Process_Name }}</span></div>
        <div class="status-item"><span>사이클 타임:</span> <span>{{ selectedEquipment.Standard_Cycle_Time }}s</span></div>
        <div class="status-item description"><span>설명:</span> <span>{{ selectedEquipment.Description }}</span></div>
      </div>
    </div>

    <div class="equipment-legend">
        <div class="legend-title">장비 구분</div>
        <div class="legend-item"><div class="legend-color" style="background-color: #e74c3c;"></div><span>주조기 (3대)</span></div>
        <div class="legend-item"><div class="legend-color" style="background-color: #3498db;"></div><span>가공기 (2대)</span></div>
        <div class="legend-item"><div class="legend-color" style="background-color: #2ecc71;"></div><span>검사장비 (1대)</span></div>
        <div class="legend-item"><div class="legend-color" style="background-color: #f39c12;"></div><span>조립기 (1대)</span></div>
        <div class="legend-item"><div class="legend-color" style="background-color: #8e44ad;"></div><span>포장기 (1대)</span></div>
    </div>

    <div class="help-panel">
        <div style="font-weight: bold; margin-bottom: 8px; color: #3498db;">💡 조작법</div>
        <div>• 마우스: 장비 클릭/회전</div>
        <div>• WASD: 카메라 이동</div>
        <div>• QE: 상하 이동</div>
        <div>• R: 뷰 리셋</div>
        <div>• T: 탑뷰 전환</div>
        <div>• F: 장비 포커스</div>
        <div>• 스페이스: 애니메이션</div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, computed } from 'vue';
import ThreeViewer from '../components/ThreeViewer.vue';

const viewerRef = ref(null);
const isAnimationRunning = ref(true);
const selectedEquipment = ref(null); // 이름 대신 전체 객체를 저장

// ✨ 상세 정보 추가
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

const animationButtonText = computed(() => 
  isAnimationRunning.value ? '⏸️ 일시정지' : '▶️ 시작'
);

function handleToggleAnimation() {
  const running = viewerRef.value?.toggleAnimation();
  isAnimationRunning.value = running;
}

// ✨ 이벤트 핸들러 수정
function updateSelectedEquipment(data) {
  selectedEquipment.value = data;
}

const handleKeyDown = (event) => {
  const key = event.key.toLowerCase();
  switch (key) {
    case 'w': case 's': case 'a': case 'd': case 'q': case 'e':
      viewerRef.value?.moveCamera(key);
      break;
    case 'r': viewerRef.value?.resetView(); break;
    case 't': viewerRef.value?.toggleTopView(); break;
    case 'f': viewerRef.value?.focusNextEquipment(); break;
    case ' ':
      event.preventDefault();
      handleToggleAnimation();
      break;
  }
};

onMounted(() => {
  window.addEventListener('keydown', handleKeyDown);
});

onUnmounted(() => {
  window.removeEventListener('keydown', handleKeyDown);
});
</script>

<style scoped>
.fullscreen-wrapper {
  width: 100vw;
  height: 100vh;
  position: absolute;
  top: 0;
  left: 0;
  overflow: hidden;
}

/* --- view.html에서 가져온 UI 스타일 --- */
.controls{position:absolute;top:20px;left:20px;display:flex;flex-direction:column;gap:10px;z-index:100}.control-btn{padding:10px 15px;background:rgba(52,73,94,.8);color:white;border:none;border-radius:8px;cursor:pointer;font-size:14px;backdrop-filter:blur(10px);transition:all .3s ease;text-align:left}.control-btn:hover{background:rgba(52,73,94,1);transform:translateY(-2px)}.status-panel{position:absolute;top:20px;right:20px;background:rgba(0,0,0,.8);color:white;padding:20px;border-radius:12px;font-size:14px;backdrop-filter:blur(10px);min-width:250px;z-index:100}.status-title{font-size:16px;font-weight:bold;margin-bottom:15px;color:#3498db}.status-item{display:flex;justify-content:space-between;margin:8px 0;padding:5px 0;border-bottom:1px solid hsla(0,0%,100%,.1)}.equipment-legend{position:absolute;bottom:20px;left:20px;background:rgba(0,0,0,.8);color:white;padding:15px;border-radius:12px;font-size:12px;backdrop-filter:blur(10px);z-index:100}.legend-title{font-weight:bold;margin-bottom:10px;color:#e74c3c}.legend-item{display:flex;align-items:center;margin:5px 0}.legend-color{width:15px;height:15px;border-radius:3px;margin-right:8px}.help-panel{position:absolute;bottom:20px;right:20px;background:rgba(0,0,0,.8);color:white;padding:15px;border-radius:12px;font-size:12px;backdrop-filter:blur(10px);max-width:200px;z-index:100}
/* ✨ 추가된 스타일 */
.selected-equipment-details { margin-top: 10px; }
.description { flex-direction: column; align-items: flex-start; }
.description span:last-child { margin-top: 5px; opacity: 0.8; }
</style>