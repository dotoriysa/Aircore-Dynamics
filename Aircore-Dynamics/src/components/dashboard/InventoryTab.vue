<template>
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
</template>

<script setup>
// 이 컴포넌트는 현재 정적 데이터만 표시하므로 스크립트 로직이 필요 없습니다.
</script>