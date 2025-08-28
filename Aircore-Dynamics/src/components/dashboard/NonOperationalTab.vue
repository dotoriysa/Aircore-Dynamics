<template>
  <div>
    <div class="non-op-grid">
      <div 
        v-for="machine in nonOperationalMachines" 
        :key="machine.pmId" 
        class="card machine-card" 
        @click="openMaintenanceModal(machine)"
      >
        <div class="card-header">
          <div class="card-title">{{ machine.machineName }}</div>
          <div class="status-indicator status-danger"></div>
        </div>
        <div class="metric">
          <span>공정</span>
          <span class="metric-value">{{ machine.processName }}</span>
        </div>
        <div class="metric">
          <span>상태</span>
          <span class="metric-value status-stopped">{{ machine.statusText }}</span>
        </div>
        <div class="metric">
          <span>마지막 업데이트</span>
          <span class="metric-value">{{ formatDateTime(machine.lastUpdate) }}</span>
        </div>
      </div>
    </div>

    <div v-if="isModalVisible" class="modal-overlay" @click.self="closeMaintenanceModal">
      <div class="modal-content">
        <div class="modal-header">
          <h3>{{ selectedMachine.machineName }} 유지보수 등록</h3>
          <button @click="closeMaintenanceModal" class="close-btn">&times;</button>
        </div>
        <div class="modal-body">
          <p>장비의 비가동 원인에 해당하는 오류 코드를 선택해주세요.</p>
          <select v-model="selectedErrorCode" class="error-code-select">
            <option disabled value="">오류 코드를 선택하세요</option>
            <option v-for="code in errorCodes" :key="code.code" :value="code.code">
              {{ code.label }} ({{ code.code }})
            </option>
          </select>
        </div>
        <div class="modal-footer">
          <button @click="submitMaintenance" class="submit-btn" :disabled="!selectedErrorCode">확인</button>
          <button @click="closeMaintenanceModal" class="cancel-btn">취소</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue';

// --- State ---
const nonOperationalMachines = ref([]);
const isModalVisible = ref(false);
const selectedMachine = ref(null);
const selectedErrorCode = ref('');
let apiInterval;

// 샘플 오류 코드 목록 (실제로는 API 또는 설정 파일에서 가져올 수 있습니다)
const errorCodes = ref([
  { code: 'TEMP_HIGH', label: '고온 경고' },
  { code: 'SENSOR_ERROR', label: '센서 오류' },
  { code: 'MOTOR_FAIL', label: '모터 이상' },
  { code: 'PRESSURE_LOW', label: '저압 경고' },
  { code: 'EMG_STOP', label: '비상 정지' }
]);

// --- Methods ---

// 비가동 장비 목록을 가져오는 API 호출 함수 (현재는 샘플 데이터 사용)
async function fetchNonOperationalMachines() {
  // try {
  //   const response = await fetch('/api/non-operational/machines');
  //   if (!response.ok) throw new Error('Failed to fetch data');
  //   const data = await response.json();
  //   nonOperationalMachines.value = data;
  // } catch (error) {
  //   console.error(error);
  
    // API가 없으므로 샘플 데이터로 대체합니다.
    nonOperationalMachines.value = [
      { processName: "주조", statusText: "정지", lastUpdate: "2025-08-28T10:15:30", errorCode: null, pmId: "PM001", machineName: "주조기1", status: 0 },
      { processName: "가공", statusText: "정지", lastUpdate: "2025-08-28T11:05:10", errorCode: null, pmId: "PM005", machineName: "가공기2", status: 0 },
      { processName: "검사", statusText: "정지", lastUpdate: "2025-08-28T09:45:00", errorCode: null, pmId: "PM006", machineName: "검사장비", status: 0 }
    ];
  // }
}

// 유지보수 등록 모달 열기
function openMaintenanceModal(machine) {
  selectedMachine.value = machine;
  selectedErrorCode.value = ''; // 오류코드 선택 초기화
  isModalVisible.value = true;
}

// 유지보수 등록 모달 닫기
function closeMaintenanceModal() {
  isModalVisible.value = false;
  selectedMachine.value = null;
}

// 유지보수 정보 서버로 전송
async function setMaintenance(pmId, errorCode) {
  try {
    const response = await fetch('/api/non-operational/maintenance', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ pmId, errorCode })
    });
    if (!response.ok) throw new Error('Server responded with an error');
    const data = await response.json();
    console.log('Maintenance set successfully:', data);
    // 성공 시 UI에 피드백을 줄 수 있습니다 (예: alert, toast)
    alert(`${pmId} 장비의 유지보수 코드(${errorCode})가 성공적으로 등록되었습니다.`);
  } catch (error) {
    console.error('Failed to set maintenance:', error);
    alert(`유지보수 등록에 실패했습니다: ${error.message}`);
  }
}

// '확인' 버튼 클릭 시 실행
function submitMaintenance() {
  if (!selectedErrorCode.value) {
    alert('오류 코드를 선택해주세요.');
    return;
  }
  setMaintenance(selectedMachine.value.pmId, selectedErrorCode.value);
  closeMaintenanceModal();
  // 목록을 새로고침하여 변경사항을 반영할 수 있습니다.
  fetchNonOperationalMachines();
}

// 날짜/시간 포맷팅 유틸리티
function formatDateTime(isoString) {
  if (!isoString) return 'N/A';
  const date = new Date(isoString);
  return date.toLocaleString('ko-KR');
}


// --- Lifecycle Hooks ---
onMounted(() => {
  fetchNonOperationalMachines();
  // 10초마다 데이터 갱신
  apiInterval = setInterval(fetchNonOperationalMachines, 10000);
});

onUnmounted(() => {
  clearInterval(apiInterval);
});

</script>

<style scoped>
.non-op-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(320px, 1fr));
  gap: 1.5rem;
}

.machine-card {
  cursor: pointer;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.machine-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 12px 40px rgba(77, 208, 225, 0.2);
}

.metric-value.status-stopped {
  color: #e74c3c;
}

/* Modal Styles */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.7);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 2000;
}

.modal-content {
  background: #2c3e50;
  padding: 2rem;
  border-radius: 16px;
  width: 90%;
  max-width: 500px;
  box-shadow: 0 8px 32px rgba(0,0,0,.3);
  border: 1px solid rgba(77,208,225,.2);
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  padding-bottom: 1rem;
  margin-bottom: 1rem;
}

.modal-header h3 {
  color: #4dd0e1;
  margin: 0;
}

.close-btn {
  background: none;
  border: none;
  color: white;
  font-size: 1.5rem;
  cursor: pointer;
}

.modal-body p {
  margin-bottom: 1rem;
  color: #bdc3c7;
}

.error-code-select {
  width: 100%;
  padding: 0.75rem;
  border-radius: 8px;
  border: 1px solid rgba(77,208,225,.2);
  background: rgba(0,0,0,.3);
  color: white;
  font-size: 1rem;
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 1rem;
  margin-top: 2rem;
}

.submit-btn, .cancel-btn {
  padding: 0.75rem 1.5rem;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-weight: bold;
  transition: background-color 0.3s ease;
}

.submit-btn {
  background-color: #00bcd4;
  color: white;
}

.submit-btn:hover {
  background-color: #0097a7;
}

.submit-btn:disabled {
  background-color: #555;
  cursor: not-allowed;
}

.cancel-btn {
  background-color: #7f8c8d;
  color: white;
}

.cancel-btn:hover {
  background-color: #616e78;
}
</style>