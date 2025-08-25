-- ========================================
-- 스마트 팩토리 데이터베이스 스키마 (MySQL 호환)
-- Database: smart_factory
-- ========================================

-- ========================================
-- 1. 공정별 머신 고정 테이블 (마스터 데이터)
-- ========================================
CREATE TABLE IF NOT EXISTS process_machine_info (
    pm_id VARCHAR(10) NOT NULL PRIMARY KEY COMMENT '통합 머신 ID (PM001~PM008)',
    process_name VARCHAR(50) NOT NULL COMMENT '공정명 (주조, 가공, 검사, 조립, 포장)',
    machine_name VARCHAR(50) NOT NULL COMMENT '머신명',
    standard_cycle_time INT NOT NULL COMMENT '표준 사이클 타임 (초)',
    description VARCHAR(200) COMMENT '머신 설명',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='공정별 머신 정보 테이블';

-- ========================================
-- 2. 환경 데이터 테이블 (10분 간격)
-- ========================================
CREATE TABLE IF NOT EXISTS environment_data (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '고유 ID',
    timestamp DATETIME NOT NULL COMMENT '측정 시간',
    temperature FLOAT COMMENT '온도 (°C)',
    humidity FLOAT COMMENT '습도 (%)',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '데이터 생성일시',

    INDEX idx_timestamp (timestamp),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='환경 데이터 테이블 (10분 간격)';

-- ========================================
-- 3. 생산 실적 테이블 (완료 시점 기준)
-- ========================================
CREATE TABLE IF NOT EXISTS production_data (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '고유 ID',
    timestamp DATETIME NOT NULL COMMENT '완료 시간',
    pm_id VARCHAR(10) NOT NULL COMMENT '머신 ID',
    completed_quantity INT DEFAULT 0 COMMENT '완료 수량 (개)',
    cycle_time INT COMMENT '실제 사이클 타임 (초)',
    quality_grade VARCHAR(10) COMMENT '품질 등급 (A, B, C, NG)',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '데이터 생성일시',

    INDEX idx_timestamp (timestamp),
    INDEX idx_pm_id (pm_id),
    INDEX idx_quality_grade (quality_grade),
    INDEX idx_created_at (created_at),

    CONSTRAINT fk_production_pm_id
        FOREIGN KEY (pm_id)
        REFERENCES process_machine_info(pm_id)
        ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='생산 실적 테이블';

-- ========================================
-- 4. 전력 데이터 테이블 (1분 간격)
-- ========================================
CREATE TABLE IF NOT EXISTS power_data (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '고유 ID',
    timestamp DATETIME NOT NULL COMMENT '측정 시간',
    pm_id VARCHAR(10) NOT NULL COMMENT '머신 ID',
    power_consumption FLOAT DEFAULT 0.0 COMMENT '1분간 전력 소비량 (kWh)',
    voltage FLOAT COMMENT '전압 (V)',
    current_value FLOAT COMMENT '전류 (A)',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '데이터 생성일시',

    INDEX idx_timestamp (timestamp),
    INDEX idx_pm_id (pm_id),
    INDEX idx_created_at (created_at),

    CONSTRAINT fk_power_pm_id
        FOREIGN KEY (pm_id)
        REFERENCES process_machine_info(pm_id)
        ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='전력 데이터 테이블 (1분 간격)';

-- ========================================
-- 5. 진동 데이터 테이블 (10초 간격)
-- ========================================
CREATE TABLE IF NOT EXISTS vibration_data (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '고유 ID',
    timestamp DATETIME NOT NULL COMMENT '측정 시간',
    pm_id VARCHAR(10) NOT NULL COMMENT '머신 ID',
    vibration_x FLOAT DEFAULT 0.0 COMMENT 'X축 진동값',
    vibration_y FLOAT DEFAULT 0.0 COMMENT 'Y축 진동값',
    vibration_z FLOAT DEFAULT 0.0 COMMENT 'Z축 진동값',
    rms_value FLOAT COMMENT 'RMS 진동값',
    peak_value FLOAT COMMENT '피크 진동값',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '데이터 생성일시',

    INDEX idx_timestamp (timestamp),
    INDEX idx_pm_id (pm_id),
    INDEX idx_created_at (created_at),

    CONSTRAINT fk_vibration_pm_id
        FOREIGN KEY (pm_id)
        REFERENCES process_machine_info(pm_id)
        ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='진동 데이터 테이블 (10초 간격)';

-- ========================================
-- 6. 머신 가동상태 테이블 (10초 간격)
-- ========================================
CREATE TABLE IF NOT EXISTS machine_status_data (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '고유 ID',
    timestamp DATETIME NOT NULL COMMENT '측정 시간',
    pm_id VARCHAR(10) NOT NULL COMMENT '머신 ID',
    status TINYINT NOT NULL COMMENT '가동 상태 (0:정지, 1:운전, 2:점검, 3:고장)',
    status_duration INT DEFAULT 0 COMMENT '상태 지속시간 (초)',
    error_code VARCHAR(20) COMMENT '에러 코드',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '데이터 생성일시',

    INDEX idx_timestamp (timestamp),
    INDEX idx_pm_id (pm_id),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at),

    CONSTRAINT fk_machine_status_pm_id
        FOREIGN KEY (pm_id)
        REFERENCES process_machine_info(pm_id)
        ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='머신 가동상태 테이블 (10초 간격)';

-- ========================================
-- 7. 공장 운영시간 테이블 (고정 테이블)
-- ========================================
CREATE TABLE IF NOT EXISTS operation_schedule (
    id INT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '고유 ID',
    schedule_type VARCHAR(20) NOT NULL COMMENT '구분 (운영시간, 점심시간, 저녁시간)',
    start_time TIME NOT NULL COMMENT '시작시간',
    end_time TIME NOT NULL COMMENT '종료시간',
    description VARCHAR(100) COMMENT '설명',
    is_active BOOLEAN DEFAULT TRUE COMMENT '활성화 여부',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',

    UNIQUE KEY uk_schedule_type (schedule_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='공장 운영시간 테이블';

-- 스키마 생성 완료 로그
SELECT 'Smart Factory Database Schema Created Successfully!' as status;