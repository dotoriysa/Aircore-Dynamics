-- ========================================
-- 스마트 팩토리 샘플 데이터 삽입 (가상 시간)
-- Database: smart_factory
-- ========================================

-- ========================================
-- 1. 마스터 데이터 삽입 (중복 방지)
-- ========================================

-- 공정별 머신 정보 초기 데이터 (INSERT IGNORE 사용)
INSERT IGNORE INTO process_machine_info (pm_id, process_name, machine_name, standard_cycle_time, description) VALUES
('PM001', '주조', '주조기1', 3600, '금속 용해 및 주조 장비 1호기'),
('PM002', '주조', '주조기2', 3600, '금속 용해 및 조 장비 2호기'),
('PM003', '주조', '주조기3', 3600, '금속 용해 및 주조 장비 3호기'),
('PM004', '가공', '가공기1', 1800, '정밀 가공 및 성형 장비 1호기'),
('PM005', '가공', '가공기2', 1800, '정밀 가공 및 성형 장비 2호기'),
('PM006', '검사', '검사장비', 900, '품질 검사 및 측정 장비'),
('PM007', '조립', '조립기', 1200, '부품 조립 및 결합 장비'),
('PM008', '포장', '포장기', 600, '자동 포장 및 밀봉 장비');

-- 공장 운영시간 초기 데이터 (ON DUPLICATE KEY UPDATE 사용)
INSERT INTO operation_schedule (schedule_type, start_time, end_time, description) VALUES
('운영시간', '06:00:00', '23:00:00', '일일 총 운영시간 17시간'),
('점심시간', '12:00:00', '13:00:00', '휴게시간 (비가동)'),
('저녁시간', '19:00:00', '20:00:00', '휴게시간 (비가동)')
ON DUPLICATE KEY UPDATE
start_time = VALUES(start_time),
end_time = VALUES(end_time),
description = VALUES(description);

-- ========================================
-- 2. 환경 데이터 샘플 (오전 9시 이전으로 수정)
-- ========================================
INSERT IGNORE INTO environment_data (timestamp, temperature, humidity) VALUES
(CONCAT(CURDATE(), ' 06:00:00'), 22.5, 45.2),
(CONCAT(CURDATE(), ' 07:00:00'), 24.6, 48.3),
(CONCAT(CURDATE(), ' 08:00:00'), 26.2, 51.2);

-- ========================================
-- 3. 생산 실적 샘플 데이터 (오전 9시 이전으로 수정)
-- ========================================
INSERT IGNORE INTO production_data (timestamp, pm_id, completed_quantity, cycle_time, quality_grade) VALUES
-- PM001 주조기1
(CONCAT(CURDATE(), ' 07:15:00'), 'PM001', 1, 3720, 'A'),
(CONCAT(CURDATE(), ' 08:22:00'), 'PM001', 1, 3650, 'A'),

-- PM002 주조기2
(CONCAT(CURDATE(), ' 07:20:00'), 'PM002', 1, 3680, 'A'),
(CONCAT(CURDATE(), ' 08:25:00'), 'PM002', 1, 3590, 'A'),

-- PM004 가공기1
(CONCAT(CURDATE(), ' 06:35:00'), 'PM004', 2, 1750, 'A'),
(CONCAT(CURDATE(), ' 07:05:00'), 'PM004', 2, 1820, 'A'),
(CONCAT(CURDATE(), ' 07:35:00'), 'PM004', 2, 1780, 'B'),
(CONCAT(CURDATE(), ' 08:10:00'), 'PM004', 2, 1850, 'A'),

-- PM006 검사장비
(CONCAT(CURDATE(), ' 06:20:00'), 'PM006', 5, 880, 'A'),
(CONCAT(CURDATE(), ' 06:35:00'), 'PM006', 5, 920, 'A'),
(CONCAT(CURDATE(), ' 06:50:00'), 'PM006', 5, 890, 'NG'),
(CONCAT(CURDATE(), ' 07:08:00'), 'PM006', 4, 950, 'B'),

-- PM008 포장기
(CONCAT(CURDATE(), ' 06:15:00'), 'PM008', 10, 580, 'A'),
(CONCAT(CURDATE(), ' 06:25:00'), 'PM008', 10, 620, 'A'),
(CONCAT(CURDATE(), ' 06:35:00'), 'PM008', 10, 590, 'NG'),
(CONCAT(CURDATE(), ' 06:48:00'), 'PM008', 8, 650, 'B');

-- ========================================
-- 4. 전력 데이터 샘플
-- ========================================
INSERT IGNORE INTO power_data (timestamp, pm_id, power_consumption, voltage, current_value) VALUES
-- 주조기 (고전력)
(CONCAT(CURDATE(), ' 06:00:00'), 'PM001', 45.2, 380.5, 68.9),
(CONCAT(CURDATE(), ' 06:01:00'), 'PM001', 47.8, 381.2, 71.2),
(CONCAT(CURDATE(), ' 06:02:00'), 'PM001', 46.5, 380.8, 69.8),
(CONCAT(CURDATE(), ' 06:00:00'), 'PM002', 44.8, 379.8, 68.2),
(CONCAT(CURDATE(), ' 06:01:00'), 'PM002', 46.2, 380.4, 69.9),

-- 가공기 (중간 전력)
(CONCAT(CURDATE(), ' 06:00:00'), 'PM004', 28.5, 220.3, 39.2),
(CONCAT(CURDATE(), ' 06:01:00'), 'PM004', 29.1, 220.8, 39.8),
(CONCAT(CURDATE(), ' 06:00:00'), 'PM005', 27.8, 219.8, 38.9),
(CONCAT(CURDATE(), ' 06:01:00'), 'PM005', 29.5, 220.9, 40.1),

-- 검사장비 (저전력)
(CONCAT(CURDATE(), ' 06:00:00'), 'PM006', 12.3, 220.1, 16.8),
(CONCAT(CURDATE(), ' 06:01:00'), 'PM006', 11.9, 219.8, 16.4),

-- 조립기
(CONCAT(CURDATE(), ' 06:00:00'), 'PM007', 18.5, 220.2, 25.3),
(CONCAT(CURDATE(), ' 06:01:00'), 'PM007', 19.1, 220.6, 25.9),

-- 포장기 (최저 전력)
(CONCAT(CURDATE(), ' 06:00:00'), 'PM008', 8.2, 220.0, 11.3),
(CONCAT(CURDATE(), ' 06:01:00'), 'PM008', 8.6, 220.2, 11.7);

-- ========================================
-- 5. 진동 데이터 샘플
-- ========================================
INSERT IGNORE INTO vibration_data (timestamp, pm_id, vibration_x, vibration_y, vibration_z, rms_value, peak_value) VALUES
-- 정상 운전 상태
(CONCAT(CURDATE(), ' 16:00:00'), 'PM001', 2.15, 1.98, 2.32, 2.18, 4.85),
(CONCAT(CURDATE(), ' 16:00:10'), 'PM001', 2.08, 2.12, 2.18, 2.13, 4.72),
(CONCAT(CURDATE(), ' 16:00:20'), 'PM001', 2.22, 1.89, 2.41, 2.21, 5.01),

-- 약간 높은 진동
(CONCAT(CURDATE(), ' 16:00:00'), 'PM002', 3.22, 2.98, 3.45, 3.25, 7.12),
(CONCAT(CURDATE(), ' 16:00:10'), 'PM002', 3.18, 3.05, 3.38, 3.22, 6.98),

-- 가공기 정상 진동
(CONCAT(CURDATE(), ' 16:00:00'), 'PM004', 1.85, 1.72, 1.98, 1.87, 3.95),
(CONCAT(CURDATE(), ' 16:00:10'), 'PM004', 1.78, 1.89, 1.82, 1.83, 3.78),

-- 검사장비 낮은 진동
(CONCAT(CURDATE(), ' 16:00:00'), 'PM006', 0.45, 0.52, 0.38, 0.46, 0.89),
(CONCAT(CURDATE(), ' 16:00:10'), 'PM006', 0.41, 0.48, 0.43, 0.44, 0.85),

-- 조립기 중간 진동
(CONCAT(CURDATE(), ' 16:00:00'), 'PM007', 1.25, 1.38, 1.12, 1.26, 2.58),
(CONCAT(CURDATE(), ' 16:00:10'), 'PM007', 1.32, 1.29, 1.41, 1.34, 2.78),

-- 포장기 낮은 진동
(CONCAT(CURDATE(), ' 16:00:00'), 'PM008', 0.78, 0.85, 0.71, 0.78, 1.52),
(CONCAT(CURDATE(), ' 16:00:10'), 'PM008', 0.82, 0.79, 0.88, 0.83, 1.65);


-- ========================================
-- 6. 머신 가동상태 데이터 샘플 (수정)
-- 6시 기준 모든 머신 상태를 '1' (가동)으로 초기화
-- ========================================

INSERT IGNORE INTO machine_status_data (timestamp, pm_id, status, status_duration, error_code) VALUES
(CONCAT(CURDATE(), ' 06:00:00'), 'PM001', 1, 0, NULL),
(CONCAT(CURDATE(), ' 06:00:00'), 'PM002', 1, 0, NULL),
(CONCAT(CURDATE(), ' 06:00:00'), 'PM003', 1, 0, NULL),
(CONCAT(CURDATE(), ' 06:00:00'), 'PM004', 1, 0, NULL),
(CONCAT(CURDATE(), ' 06:00:00'), 'PM005', 1, 0, NULL),
(CONCAT(CURDATE(), ' 06:00:00'), 'PM006', 1, 0, NULL),
(CONCAT(CURDATE(), ' 06:00:00'), 'PM007', 1, 0, NULL),
(CONCAT(CURDATE(), ' 06:00:00'), 'PM008', 1, 0, NULL);
-- 데이터 삽입 완료 확인

SELECT 'Smart Factory sample data inserted successfully!' as completion_message;