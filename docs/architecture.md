# 🏗️ Architecture Overview

이 문서는 **Aircore Dynamics** 프로젝트의 백엔드/프론트엔드 디렉토리 구조를 한눈에 볼 수 있도록 정리한 아키텍처 가이드입니다.

---

## 🔧 Tech Stack (요약)

- **Frontend**: HTML5, CSS3, JavaScript, Vue.js, Three.js, ECharts  
- **Backend**: Java, Spring Boot, MyBatis  
- **Database**: MySQL  
- **Build Tool**: Maven  
- **Design**: Figma


---

## 🖥️ Backend (Spring Boot)

```plaintext
src/main/java/com/example/demo/
├── config/
│   └── CorsConfig.java                      # CORS 설정
│
├── controller/
│   ├── MachineDashboardController.java      # 머신 대시보드 API
│   ├── MainDashboardController.java         # 메인 대시보드 API
│   ├── NonOperationalDashboardController.java # 비가동 대시보드 API
│   └── ProcessDashboardController.java      # 공정별 대시보드 API
│
├── mapper/
│   ├── FactoryMapper.java                   # 공장 데이터 접근
│   ├── MachineInfoMapper.java               # 머신(설비) 정보 접근
│   ├── MachineStatusMapper.java             # 머신 상태 데이터 접근
│   ├── PowerMapper.java                     # 전력 데이터 접근
│   └── ProductionMapper.java                # 생산 데이터 접근
│
├── model/
│   ├── FactoryEnvironmentData.java          # 공장 환경 데이터 모델 (온습도 등)
│   ├── MachineStatusData.java               # 머신 상태 데이터 모델
│   ├── PowerData.java                       # 전력 데이터 모델
│   ├── ProcessMachineInfo.java              # 공정별 머신 정보 모델
│   └── ProductionData.java                  # 생산 데이터 모델
│
├── service/
│   ├── MachineDashboardService.java         # 머신 대시보드 서비스
│   ├── MachineStatusService.java            # 머신 상태 서비스
│   ├── MainDashboardService.java            # 메인 대시보드 서비스
│   ├── NonOperationalDashboardService.java  # 비가동 대시보드 서비스
│   ├── PowerDataService.java                # 전력 데이터 서비스
│   ├── ProcessDashboardService.java         # 공정별 대시보드 서비스
│   ├── ProductionDataService.java           # 생산 데이터 서비스
│   └── FactoryEnvironmentDataService.java   # 공장 환경 데이터 생성/조회 서비스
│
└── util/
    ├── RandomEnvironmentDataGenerator.java  # 환경 데이터 생성기 (더미)
    ├── RandomMachineStatusGenerator.java    # 머신 상태 생성기 (더미)
    ├── RandomPowerDataGenerator.java        # 전력 데이터 생성기 (더미)
    └── RandomProductionDataGenerator.java   # 생산 데이터 생성기 (더미)
```

---

## 🌐 Frontend (Vue.js)

```plaintext
src/
├── assets/
│   └── styles/                              # 전역 스타일, 폰트/아이콘 설정
│
├── components/
│   ├── dashboard/
│   │   ├── MachineStatus.vue                # 장비 상태 위젯
│   │   ├── PowerUsage.vue                   # 전력량 차트(ECharts)
│   │   ├── ProductionRate.vue               # 생산량 차트(ECharts)
│   │   └── Environment.vue                  # 공장 환경(온습도 등) 위젯
│   └── common/
│       ├── Navbar.vue                       # 상단 네비게이션
│       ├── Sidebar.vue                      # 좌측 사이드 메뉴
│       └── Footer.vue                       # 하단 푸터
│
├── router/
│   └── index.js                             # Vue Router 설정 (페이지 라우팅)
│
├── views/
│   ├── MainDashboard.vue                    # 메인 대시보드 (공장 전체 현황)
│   ├── ProcessDashboard.vue                 # 공정별 대시보드 (주조/가공/검사 등)
│   ├── MachineDashboard.vue                 # 개별 장비 대시보드
│   └── NonOperational.vue                   # 비가동 장비 모니터링
│
├── App.vue                                  # Root 컴포넌트 (레이아웃)
└── main.js                                  # 진입 파일 (Vue 초기화 & Router 연결)
```


---
