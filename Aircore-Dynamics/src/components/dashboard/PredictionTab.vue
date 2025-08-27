<template>
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
</template>

<script setup>
// 이 컴포넌트는 현재 정적 데이터만 표시하므로 스크립트 로직이 필요 없습니다.
</script>