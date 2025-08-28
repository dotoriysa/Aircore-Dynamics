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
          <div :class="['status-indicator', machine.statusClass || 'status-danger']"></div>
        </div>
        <div class="metric">
          <span>공정</span>
          <span class="metric-value">{{ machine.processName }}</span>
        </div>
        <div class="metric">
          <span>상태</span>
          <span 
            class="metric-value" 
            :class="{ 
              'status-stopped': machine.statusClass === 'status-danger' || !machine.statusClass,
              'status-warning-text': machine.statusClass === 'status-warning'
            }"
          >
            {{ machine.statusText }}
          </span>
        </div>
        <div class="metric">
          <span>업데이트</span>
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

    <div v-if="isLoading" class="loading-overlay">
      <div class="loading-spinner">로딩 중...</div>
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
const isLoading = ref(false);
let apiInterval;

// 오류 코드 목록
const errorCodes = ref([
  { code: 'TEMP_HIGH', label: '고온 경고' },
  { code: 'SENSOR_ERROR', label: '센서 오류' },
  { code: 'MOTOR_FAIL', label: '모터 이상' },
  { code: 'PRESSURE_LOW', label: '저압 경고' },
  { code: 'EMG_STOP', label: '비상 정지' }
]);

// --- Methods ---

// 비가동 장비 목록을 가져오는 API 호출 함수
async function fetchNonOperationalMachines() {
  try {
    const response = await fetch('/api/non-operational/machines');
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }
    const data = await response.json();
    
    data.forEach(newMachine => {
        const existingMachine = nonOperationalMachines.value.find(m => m.pmId === newMachine.pmId);
        if (!existingMachine) {
            nonOperationalMachines.value.push({
                ...newMachine,
                statusClass: 'status-danger'
            });
        } else {
            if (existingMachine.lastUpdate !== newMachine.lastUpdate) {
                Object.assign(existingMachine, newMachine);
            }
        }
    });

  } catch (error) {
    console.error('비가동 장비 목록 조회 실패:', error);
    alert(`데이터를 불러오는데 실패했습니다: ${error.message}`);
    nonOperationalMachines.value = [];
  } finally {
    isLoading.value = false;
  }
}

// 유지보수 등록 모달 열기
function openMaintenanceModal(machine) {
  selectedMachine.value = machine;
  selectedErrorCode.value = '';
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
    
    if (!response.ok) {
      const errorText = await response.text();
      throw new Error(`Server error: ${response.status} - ${errorText}`);
    }
    
    const data = await response.json();
    console.log('Maintenance set successfully:', data);
    
    alert(`${pmId} 장비의 유지보수 코드(${errorCode})가 성공적으로 등록되었습니다.`);
    return true;
  } catch (error) {
    console.error('Failed to set maintenance:', error);
    alert(`유지보수 등록에 실패했습니다: ${error.message}`);
    throw error;
  }
}

// '확인' 버튼 클릭 시 실행
async function submitMaintenance() {
  if (!selectedErrorCode.value) {
    alert('오류 코드를 선택해주세요.');
    return;
  }
  
  try {
    await setMaintenance(selectedMachine.value.pmId, selectedErrorCode.value);
    
    const machineToUpdate = nonOperationalMachines.value.find(
      m => m.pmId === selectedMachine.value.pmId
    );
    const selectedError = errorCodes.value.find(
      e => e.code === selectedErrorCode.value
    );

    if (machineToUpdate && selectedError) {
      machineToUpdate.statusText = selectedError.label;
      machineToUpdate.statusClass = 'status-warning';
    }

    closeMaintenanceModal();
  } catch (error) {
    console.error('유지보수 등록 중 오류 발생:', error);
  }
}

// ✨✨✨ --- 날짜/시간 포맷팅 유틸리티 수정 --- ✨✨✨
function formatDateTime(isoString) {
  if (!isoString) return 'N/A';
  try {
    const date = new Date(isoString);
    const fullString = date.toLocaleString('ko-KR', {
      year: 'numeric',
      month: 'numeric',
      day: 'numeric',
      hour: 'numeric',
      minute: '2-digit',
      second: '2-digit',
      hour12: true
    });
    
    // ' 오전' 또는 ' 오후' 앞의 공백을 찾아 그 위치를 기준으로 문자열을 나눕니다.
    let splitIndex = fullString.indexOf(' 오전');
    if (splitIndex === -1) {
      splitIndex = fullString.indexOf(' 오후');
    }

    if (splitIndex > -1) {
      const datePart = fullString.substring(0, splitIndex);
      const timePart = fullString.substring(splitIndex + 1); // 공백 다음부터
      return `${datePart}\n${timePart}`;
    }

    return fullString; // '오전/오후'를 찾지 못할 경우 원래 형식으로 반환
  } catch (error) {
    console.error('날짜 포맷팅 오류:', error);
    return 'Invalid Date';
  }
}

// --- Lifecycle Hooks ---
onMounted(async () => {
  isLoading.value = true;
  await fetchNonOperationalMachines();
  apiInterval = setInterval(fetchNonOperationalMachines, 10000);
});

onUnmounted(() => {
  if (apiInterval) {
    clearInterval(apiInterval);
  }
});

</script>

<style scoped>
.metric {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.metric > span:first-of-type {
  white-space: nowrap;
  margin-right: 1rem;
  flex-shrink: 0;
}
.metric .metric-value {
  text-align: right;
  flex-grow: 1;
  word-break: keep-all;
  white-space: pre-line; 
  line-height: 1.4;
}

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
.metric-value.status-warning-text {
  color: #f39c12;
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

/* Loading Styles */
.loading-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.loading-spinner {
  background: #2c3e50;
  color: #4dd0e1;
  padding: 2rem;
  border-radius: 8px;
  font-weight: bold;
}

.status-indicator{width:12px;height:12px;border-radius:50%;animation:pulse 2s infinite}.status-good{background-color:#27ae60}.status-warning{background-color:#f39c12}.status-danger{background-color:#e74c3c}@keyframes pulse{0%{box-shadow:0 0 0 0 rgba(39,174,96,.4)}70%{box-shadow:0 0 0 10px transparent}to{box-shadow:0 0 0 0 transparent}}
</style>