-- =============================================
-- i龄守护 数据库初始化脚本
-- 使用方法：在 MySQL 中执行此文件即可完成数据库搭建
-- mysql -u root -p < init.sql
-- =============================================

CREATE DATABASE IF NOT EXISTS `health_db`
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE `health_db`;

-- =============================================
-- 建表（若已存在则跳过，安全可重复执行）
-- =============================================

-- 1. 用户表
CREATE TABLE IF NOT EXISTS `user` (
  `phone`           VARCHAR(11)  NOT NULL COMMENT '手机号（主键）',
  `password`        VARCHAR(100) NOT NULL COMMENT '密码',
  `gender`          VARCHAR(10)  NOT NULL COMMENT '性别：male/female',
  `avatar_url`      VARCHAR(500)          COMMENT '头像地址',
  `nickname`        VARCHAR(50)           COMMENT '昵称',
  `age`             INT                   COMMENT '年龄',
  `role`            VARCHAR(20)  DEFAULT 'USER'   COMMENT '角色',
  `status`          VARCHAR(20)  DEFAULT 'ACTIVE' COMMENT '账户状态',
  `created_at`      DATETIME              COMMENT '创建时间',
  `updated_at`      DATETIME              COMMENT '更新时间',
  `last_login_at`   DATETIME              COMMENT '最近登录时间',
  PRIMARY KEY (`phone`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 2. 老人基本信息表
CREATE TABLE IF NOT EXISTS `elderly` (
  `id`                BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name`              VARCHAR(50)  NOT NULL COMMENT '姓名',
  `age`               INT                   COMMENT '年龄',
  `gender`            VARCHAR(10)           COMMENT '性别：male/female',
  `height`            DOUBLE                COMMENT '身高(cm)',
  `weight`            DOUBLE                COMMENT '体重(kg)',
  `blood_type`        VARCHAR(10)           COMMENT '血型',
  `emergency_contact` VARCHAR(50)           COMMENT '紧急联系人姓名',
  `emergency_phone`   VARCHAR(20)           COMMENT '紧急联系人电话',
  `address`           VARCHAR(200)          COMMENT '居住地址',
  `medical_history`   TEXT                  COMMENT '病史',
  `created_at`        DATETIME              COMMENT '创建时间',
  `updated_at`        DATETIME              COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='老人基本信息表';

-- 3. 生理数据表
CREATE TABLE IF NOT EXISTS `physiological_data` (
  `id`                  BIGINT  NOT NULL AUTO_INCREMENT COMMENT '主键',
  `elderly_id`          BIGINT  NOT NULL               COMMENT '关联老人ID',
  `heart_rate`          INT                            COMMENT '心率(bpm)',
  `blood_pressure_high` INT                            COMMENT '收缩压(mmHg)',
  `blood_pressure_low`  INT                            COMMENT '舒张压(mmHg)',
  `blood_oxygen`        INT                            COMMENT '血氧饱和度(%)',
  `body_temperature`    DOUBLE                         COMMENT '体温(°C)',
  `respiratory_rate`    INT                            COMMENT '呼吸频率(次/分)',
  `blood_sugar`         DOUBLE                         COMMENT '血糖(mmol/L)',
  `recorded_at`         DATETIME NOT NULL              COMMENT '记录时间',
  PRIMARY KEY (`id`),
  INDEX `idx_physiological_elderly_id` (`elderly_id`),
  INDEX `idx_physiological_recorded_at` (`recorded_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='生理数据表';

-- 4. 行为数据表（UWB定位）
CREATE TABLE IF NOT EXISTS `behavior_data` (
  `id`             BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  `elderly_id`     BIGINT       NOT NULL               COMMENT '关联老人ID',
  `position_x`     DOUBLE                              COMMENT '位置X坐标(m)',
  `position_y`     DOUBLE                              COMMENT '位置Y坐标(m)',
  `position_z`     DOUBLE                              COMMENT '位置Z坐标(m)',
  `movement_speed` DOUBLE                              COMMENT '移动速度(m/s)',
  `stay_duration`  INT                                 COMMENT '停留时长(秒)',
  `activity_type`  VARCHAR(50)                         COMMENT '活动类型',
  `recorded_at`    DATETIME     NOT NULL               COMMENT '记录时间',
  PRIMARY KEY (`id`),
  INDEX `idx_behavior_elderly_id` (`elderly_id`),
  INDEX `idx_behavior_recorded_at` (`recorded_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='行为数据表';

-- 5. 环境数据表
CREATE TABLE IF NOT EXISTS `environment_data` (
  `id`                BIGINT  NOT NULL AUTO_INCREMENT COMMENT '主键',
  `elderly_id`        BIGINT  NOT NULL               COMMENT '关联老人ID',
  `room_temperature`  DOUBLE                         COMMENT '室温(°C)',
  `humidity`          INT                            COMMENT '湿度(%)',
  `air_quality_index` INT                            COMMENT '空气质量指数',
  `light_intensity`   INT                            COMMENT '光照强度(lux)',
  `noise_level`       INT                            COMMENT '噪声(dB)',
  `recorded_at`       DATETIME NOT NULL              COMMENT '记录时间',
  PRIMARY KEY (`id`),
  INDEX `idx_environment_elderly_id` (`elderly_id`),
  INDEX `idx_environment_recorded_at` (`recorded_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='环境数据表';

-- 6. 预警记录表
CREATE TABLE IF NOT EXISTS `alert_record` (
  `id`            BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键',
  `elderly_id`    BIGINT      NOT NULL               COMMENT '关联老人ID',
  `alert_type`    VARCHAR(50)                        COMMENT '预警类型',
  `alert_level`   VARCHAR(20)                        COMMENT '预警级别：low/medium/high',
  `alert_message` TEXT                               COMMENT '预警详情',
  `is_handled`    TINYINT(1)  DEFAULT 0              COMMENT '是否已处理',
  `handled_at`    DATETIME                           COMMENT '处理时间',
  `handler`       VARCHAR(50)                        COMMENT '处理人',
  `handle_note`   TEXT                               COMMENT '处理备注',
  `created_at`    DATETIME    NOT NULL               COMMENT '创建时间',
  PRIMARY KEY (`id`),
  INDEX `idx_alert_elderly_id` (`elderly_id`),
  INDEX `idx_alert_is_handled` (`is_handled`),
  INDEX `idx_alert_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='预警记录表';

-- 7. 健康风险记录表
CREATE TABLE IF NOT EXISTS `health_risk_record` (
  `id`                BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键',
  `elderly_id`        BIGINT      NOT NULL               COMMENT '关联老人ID',
  `risk_score`        DOUBLE                             COMMENT '风险评分(0-100)',
  `risk_level`        VARCHAR(20)                        COMMENT '风险等级：low/medium/high',
  `status_label`      VARCHAR(50)                        COMMENT '状态标签',
  `risk_factors`      TEXT                               COMMENT '风险因素描述',
  `prediction_result` TEXT                               COMMENT 'AI预测结果',
  `is_alerted`        TINYINT(1)  DEFAULT 0              COMMENT '是否已发送预警',
  `alert_sent_at`     DATETIME                           COMMENT '预警发送时间',
  `created_at`        DATETIME    NOT NULL               COMMENT '创建时间',
  PRIMARY KEY (`id`),
  INDEX `idx_risk_elderly_id` (`elderly_id`),
  INDEX `idx_risk_created_at` (`created_at`),
  INDEX `idx_risk_level` (`risk_level`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='健康风险记录表';

-- 8. 设备表
CREATE TABLE IF NOT EXISTS `device` (
  `id`               BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_phone`       VARCHAR(11)  NOT NULL               COMMENT '关联用户手机号',
  `device_name`      VARCHAR(50)  NOT NULL               COMMENT '设备名称',
  `device_type`      VARCHAR(20)                         COMMENT '设备类型：手环/血压计/血氧仪/体温计/其他',
  `device_sn`        VARCHAR(100)                        COMMENT '设备序列号',
  `status`           VARCHAR(20)  DEFAULT 'binding'      COMMENT '状态：online/offline/binding',
  `bind_time`        DATETIME                            COMMENT '绑定时间',
  `last_online_time` DATETIME                            COMMENT '最近在线时间',
  PRIMARY KEY (`id`),
  INDEX `idx_device_user_phone` (`user_phone`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='设备表';

-- 9. 紧急联系人表
CREATE TABLE IF NOT EXISTS `emergency_contact` (
  `id`            BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_phone`    VARCHAR(11) NOT NULL               COMMENT '关联用户手机号',
  `name`          VARCHAR(30) NOT NULL               COMMENT '联系人姓名',
  `contact_phone` VARCHAR(11) NOT NULL               COMMENT '联系人手机号',
  `relationship`  VARCHAR(20)                        COMMENT '关系：子女/配偶/父母/兄弟姐妹/朋友/其他',
  `is_primary`    TINYINT(1)  DEFAULT 0              COMMENT '是否主要联系人',
  `created_at`    DATETIME                           COMMENT '创建时间',
  `updated_at`    DATETIME                           COMMENT '更新时间',
  PRIMARY KEY (`id`),
  INDEX `idx_contact_user_phone` (`user_phone`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='紧急联系人表';

-- =============================================
-- 模拟数据（INSERT IGNORE 防重复执行）
-- 数据范围：近 7 天，覆盖生理/行为/环境/风险
-- =============================================

-- 1. 示例用户（测试账号）
INSERT IGNORE INTO `user` (phone, password, gender, nickname, age, role, status, created_at, updated_at)
VALUES ('13800138000', '123456', 'female', '张淑芬家属', 40, 'USER', 'ACTIVE', '2024-01-01 08:00:00', NOW());

-- 2. 老人基本信息
INSERT IGNORE INTO `elderly` (id, name, age, gender, height, weight, blood_type,
  emergency_contact, emergency_phone, address, medical_history, created_at, updated_at)
VALUES (1, '张淑芬', 68, 'female', 158.0, 55.0, 'A',
  '张明', '13912345678', '北京市朝阳区幸福里小区3栋201',
  '高血压（一级，已服药控制）；无糖尿病史；2019年轻度脑梗，已康复',
  '2024-01-01 08:00:00', NOW());

-- 3. 生理数据（近7天，每天8个时间点）
-- 正常范围：HR 62-80，BP 110-135/68-85，SpO2 96-99，Temp 36.1-36.9
-- Day4 模拟轻度发热事件，Day2 傍晚血压偏高

-- 2026-02-26（Day1，状态良好）
INSERT IGNORE INTO `physiological_data` (elderly_id,heart_rate,blood_pressure_high,blood_pressure_low,blood_oxygen,body_temperature,respiratory_rate,blood_sugar,recorded_at) VALUES
(1,68,118,74,98,36.4,16,5.2,'2026-02-26 07:15:00'),
(1,72,122,76,98,36.5,15,6.1,'2026-02-26 09:30:00'),
(1,75,125,78,97,36.6,16,5.8,'2026-02-26 12:00:00'),
(1,70,120,75,98,36.5,15,5.3,'2026-02-26 14:30:00'),
(1,73,124,77,97,36.6,16,5.6,'2026-02-26 16:00:00'),
(1,69,119,74,98,36.4,15,5.1,'2026-02-26 18:30:00'),
(1,67,117,73,99,36.3,14,5.0,'2026-02-26 20:00:00'),
(1,65,115,72,99,36.2,14,4.9,'2026-02-26 22:00:00');

-- 2026-02-27（Day2，傍晚血压偏高）
INSERT IGNORE INTO `physiological_data` (elderly_id,heart_rate,blood_pressure_high,blood_pressure_low,blood_oxygen,body_temperature,respiratory_rate,blood_sugar,recorded_at) VALUES
(1,69,120,75,98,36.4,15,5.3,'2026-02-27 07:20:00'),
(1,74,123,77,98,36.5,16,6.2,'2026-02-27 09:45:00'),
(1,76,126,79,97,36.6,16,5.9,'2026-02-27 12:10:00'),
(1,71,121,76,98,36.5,15,5.4,'2026-02-27 14:00:00'),
(1,80,138,88,96,36.7,18,5.7,'2026-02-27 17:30:00'),
(1,78,136,86,97,36.6,17,5.5,'2026-02-27 19:00:00'),
(1,70,122,76,98,36.4,15,5.1,'2026-02-27 21:00:00'),
(1,66,116,73,99,36.3,14,4.8,'2026-02-27 22:30:00');

-- 2026-02-28（Day3，正常）
INSERT IGNORE INTO `physiological_data` (elderly_id,heart_rate,blood_pressure_high,blood_pressure_low,blood_oxygen,body_temperature,respiratory_rate,blood_sugar,recorded_at) VALUES
(1,67,117,73,99,36.3,14,5.1,'2026-02-28 07:00:00'),
(1,71,121,75,98,36.4,15,6.0,'2026-02-28 09:30:00'),
(1,74,124,77,97,36.5,16,5.7,'2026-02-28 12:00:00'),
(1,70,120,75,98,36.5,15,5.3,'2026-02-28 14:30:00'),
(1,72,122,76,97,36.6,16,5.5,'2026-02-28 16:30:00'),
(1,68,118,74,98,36.4,15,5.0,'2026-02-28 18:30:00'),
(1,66,116,72,99,36.3,14,4.9,'2026-02-28 20:30:00'),
(1,64,114,71,99,36.2,14,4.8,'2026-02-28 22:00:00');

-- 2026-03-01（Day4，轻度发热+心率偏快）
INSERT IGNORE INTO `physiological_data` (elderly_id,heart_rate,blood_pressure_high,blood_pressure_low,blood_oxygen,body_temperature,respiratory_rate,blood_sugar,recorded_at) VALUES
(1,76,128,80,97,37.0,17,5.4,'2026-03-01 07:10:00'),
(1,82,132,84,96,37.3,19,5.8,'2026-03-01 09:00:00'),
(1,84,133,85,96,37.5,20,6.0,'2026-03-01 11:30:00'),
(1,80,130,82,96,37.4,19,5.7,'2026-03-01 14:00:00'),
(1,78,128,80,97,37.2,18,5.5,'2026-03-01 16:00:00'),
(1,75,125,78,97,37.0,17,5.3,'2026-03-01 18:30:00'),
(1,71,121,75,98,36.8,16,5.1,'2026-03-01 20:30:00'),
(1,68,118,74,98,36.6,15,4.9,'2026-03-01 22:30:00');

-- 2026-03-02（Day5，恢复中）
INSERT IGNORE INTO `physiological_data` (elderly_id,heart_rate,blood_pressure_high,blood_pressure_low,blood_oxygen,body_temperature,respiratory_rate,blood_sugar,recorded_at) VALUES
(1,70,120,75,98,36.6,15,5.2,'2026-03-02 07:30:00'),
(1,73,122,76,98,36.6,16,6.1,'2026-03-02 09:30:00'),
(1,72,121,76,98,36.5,15,5.6,'2026-03-02 12:00:00'),
(1,69,119,74,98,36.5,15,5.2,'2026-03-02 14:30:00'),
(1,71,121,75,98,36.5,15,5.4,'2026-03-02 16:30:00'),
(1,68,118,73,99,36.4,14,5.0,'2026-03-02 18:30:00'),
(1,66,116,72,99,36.3,14,4.9,'2026-03-02 20:30:00'),
(1,64,114,71,99,36.2,14,4.8,'2026-03-02 22:00:00');

-- 2026-03-03（Day6，状态良好，较活跃）
INSERT IGNORE INTO `physiological_data` (elderly_id,heart_rate,blood_pressure_high,blood_pressure_low,blood_oxygen,body_temperature,respiratory_rate,blood_sugar,recorded_at) VALUES
(1,67,117,73,99,36.3,14,5.1,'2026-03-03 07:00:00'),
(1,78,126,79,97,36.6,17,6.3,'2026-03-03 09:00:00'),
(1,76,125,78,97,36.6,16,5.9,'2026-03-03 12:00:00'),
(1,71,121,75,98,36.5,15,5.3,'2026-03-03 14:30:00'),
(1,74,124,77,97,36.6,16,5.5,'2026-03-03 16:00:00'),
(1,70,120,75,98,36.5,15,5.1,'2026-03-03 18:30:00'),
(1,67,117,73,99,36.4,14,5.0,'2026-03-03 20:30:00'),
(1,65,115,72,99,36.3,14,4.8,'2026-03-03 22:00:00');

-- 2026-03-04（Day7，今日数据）
INSERT IGNORE INTO `physiological_data` (elderly_id,heart_rate,blood_pressure_high,blood_pressure_low,blood_oxygen,body_temperature,respiratory_rate,blood_sugar,recorded_at) VALUES
(1,68,118,74,98,36.4,15,5.2,'2026-03-04 07:00:00'),
(1,72,121,75,98,36.5,15,6.1,'2026-03-04 09:30:00'),
(1,74,123,77,97,36.5,16,5.7,'2026-03-04 12:00:00'),
(1,71,120,75,98,36.5,15,5.3,'2026-03-04 14:52:00');

-- 4. 行为数据（近7天，每天6条）
INSERT IGNORE INTO `behavior_data` (elderly_id,position_x,position_y,position_z,movement_speed,stay_duration,activity_type,recorded_at) VALUES
-- Day1
(1,3.2,2.1,0,0.0,1800,'坐下休息','2026-02-26 08:00:00'),
(1,3.5,5.8,0,0.8,600,'缓慢行走','2026-02-26 09:30:00'),
(1,6.1,3.2,0,0.0,2400,'用餐','2026-02-26 12:00:00'),
(1,4.2,4.5,0,0.0,3600,'午睡','2026-02-26 13:00:00'),
(1,2.8,2.0,0,1.1,1200,'户外散步','2026-02-26 16:00:00'),
(1,3.1,2.2,0,0.0,900,'站立','2026-02-26 19:00:00'),
-- Day2
(1,3.2,2.1,0,0.0,1500,'坐下休息','2026-02-27 08:00:00'),
(1,2.9,5.5,0,0.7,900,'缓慢行走','2026-02-27 10:00:00'),
(1,6.2,3.1,0,0.0,2100,'用餐','2026-02-27 12:10:00'),
(1,4.1,4.4,0,0.0,3000,'午睡','2026-02-27 13:30:00'),
(1,2.5,2.3,0,0.0,1200,'站立','2026-02-27 16:30:00'),
(1,3.0,2.0,0,0.0,2400,'坐下休息','2026-02-27 20:00:00'),
-- Day3
(1,3.3,2.2,0,0.0,1200,'站立','2026-02-28 07:30:00'),
(1,3.1,5.6,0,0.9,1200,'缓慢行走','2026-02-28 09:30:00'),
(1,6.0,3.3,0,0.0,2400,'用餐','2026-02-28 12:00:00'),
(1,4.3,4.6,0,0.0,3600,'午睡','2026-02-28 13:00:00'),
(1,2.7,2.1,0,1.2,1800,'户外散步','2026-02-28 16:00:00'),
(1,3.2,2.2,0,0.0,1800,'坐下休息','2026-02-28 20:00:00'),
-- Day4（发热，活动减少）
(1,4.2,4.5,0,0.0,7200,'卧床休息','2026-03-01 08:00:00'),
(1,3.2,2.1,0,0.3,300,'缓慢行走','2026-03-01 10:00:00'),
(1,6.1,3.2,0,0.0,1800,'用餐','2026-03-01 12:00:00'),
(1,4.2,4.5,0,0.0,7200,'卧床休息','2026-03-01 13:00:00'),
(1,3.0,2.0,0,0.2,600,'缓慢行走','2026-03-01 17:00:00'),
(1,4.1,4.4,0,0.0,5400,'卧床休息','2026-03-01 19:00:00'),
-- Day5
(1,3.2,2.1,0,0.0,1800,'坐下休息','2026-03-02 08:00:00'),
(1,3.4,5.7,0,0.6,900,'缓慢行走','2026-03-02 10:00:00'),
(1,6.0,3.2,0,0.0,2100,'用餐','2026-03-02 12:00:00'),
(1,4.2,4.5,0,0.0,3600,'午睡','2026-03-02 13:00:00'),
(1,2.8,2.1,0,0.9,1200,'户外散步','2026-03-02 16:30:00'),
(1,3.1,2.2,0,0.0,1800,'坐下休息','2026-03-02 20:00:00'),
-- Day6
(1,3.1,2.0,0,0.0,900,'站立','2026-03-03 07:30:00'),
(1,2.6,5.4,0,1.1,1800,'户外散步','2026-03-03 09:00:00'),
(1,6.1,3.1,0,0.0,2400,'用餐','2026-03-03 12:00:00'),
(1,4.2,4.5,0,0.0,2700,'午睡','2026-03-03 13:00:00'),
(1,2.7,2.0,0,1.0,1500,'户外散步','2026-03-03 16:00:00'),
(1,3.0,2.2,0,0.0,2100,'坐下休息','2026-03-03 19:30:00'),
-- Day7（今日）
(1,3.2,2.1,0,0.0,1200,'坐下休息','2026-03-04 07:00:00'),
(1,3.3,5.6,0,0.7,900,'缓慢行走','2026-03-04 09:30:00'),
(1,6.0,3.2,0,0.0,2400,'用餐','2026-03-04 12:00:00'),
(1,3.1,2.2,0,0.0,480,'站立','2026-03-04 14:52:00');

-- 5. 环境数据（近7天，每天4条）
INSERT IGNORE INTO `environment_data` (elderly_id,room_temperature,humidity,air_quality_index,light_intensity,noise_level,recorded_at) VALUES
-- Day1
(1,23.5,52,28,450,38,'2026-02-26 07:00:00'),
(1,24.2,55,32,600,42,'2026-02-26 12:00:00'),
(1,24.8,54,35,520,40,'2026-02-26 17:00:00'),
(1,23.0,56,25,80,32,'2026-02-26 22:00:00'),
-- Day2
(1,23.2,51,30,420,37,'2026-02-27 07:00:00'),
(1,24.5,54,38,580,44,'2026-02-27 12:00:00'),
(1,25.1,53,42,500,41,'2026-02-27 17:00:00'),
(1,23.1,55,28,85,33,'2026-02-27 22:00:00'),
-- Day3
(1,23.8,53,26,440,36,'2026-02-28 07:00:00'),
(1,24.6,55,31,610,43,'2026-02-28 12:00:00'),
(1,24.9,54,33,515,39,'2026-02-28 17:00:00'),
(1,23.2,56,24,78,31,'2026-02-28 22:00:00'),
-- Day4
(1,23.0,58,29,380,35,'2026-03-01 07:00:00'),
(1,23.5,60,34,400,38,'2026-03-01 12:00:00'),
(1,23.2,59,30,300,36,'2026-03-01 17:00:00'),
(1,22.8,61,22,60,30,'2026-03-01 22:00:00'),
-- Day5
(1,23.4,52,27,430,37,'2026-03-02 07:00:00'),
(1,24.3,55,32,590,42,'2026-03-02 12:00:00'),
(1,24.7,54,36,505,40,'2026-03-02 17:00:00'),
(1,23.1,56,25,82,32,'2026-03-02 22:00:00'),
-- Day6
(1,23.6,51,25,460,38,'2026-03-03 07:00:00'),
(1,24.8,53,30,625,43,'2026-03-03 12:00:00'),
(1,25.0,52,34,530,41,'2026-03-03 17:00:00'),
(1,23.3,54,23,90,31,'2026-03-03 22:00:00'),
-- Day7
(1,23.5,53,28,445,37,'2026-03-04 07:00:00'),
(1,24.1,55,31,595,42,'2026-03-04 12:00:00'),
(1,24.0,55,30,490,39,'2026-03-04 14:52:00');

-- 6. 健康风险记录（关键时间点）
INSERT IGNORE INTO `health_risk_record` (elderly_id,risk_score,risk_level,status_label,risk_factors,prediction_result,is_alerted,created_at) VALUES
(1,12.5,'low','健康良好','无明显异常因素','未来24小时风险极低，建议保持规律作息',0,'2026-02-26 22:30:00'),
(1,28.0,'low','轻微异常','傍晚血压偏高（138/88），情绪或疲劳相关','建议明日清晨复测血压，注意休息',0,'2026-02-27 22:30:00'),
(1,15.0,'low','健康良好','各项指标平稳','无明显风险，继续保持',0,'2026-02-28 22:30:00'),
(1,62.0,'medium','需要关注','体温37.5°C持续6小时，心率偏快（84bpm），活动量明显减少','建议密切观察体温变化，若持续升高及时就医',1,'2026-03-01 12:00:00'),
(1,45.0,'medium','恢复中','体温已降至37.0°C，心率逐渐恢复正常','健康风险下降，继续观察',0,'2026-03-01 22:30:00'),
(1,18.0,'low','基本恢复','各项指标趋于正常','风险降低，注意适当补充营养和休息',0,'2026-03-02 22:30:00'),
(1,10.0,'low','健康良好','今日活动充分，各项指标优秀','保持良好生活习惯',0,'2026-03-03 22:30:00'),
(1,14.0,'low','健康良好','当前状态良好','风险极低',0,'2026-03-04 14:52:00');

-- 7. 预警记录（Day4 发热事件产生预警）
INSERT IGNORE INTO `alert_record` (elderly_id,alert_type,alert_level,alert_message,is_handled,handled_at,handler,handle_note,created_at) VALUES
(1,'体温异常','medium','检测到体温升高至37.5°C，持续超过2小时，心率同步偏快，请关注老人健康状况',1,'2026-03-01 14:30:00','张明（子女）','已联系老人确认，反映有轻微感冒症状，已服用感冒药，持续观察','2026-03-01 12:05:00'),
(1,'血压偏高','low','2月27日傍晚血压达138/88mmHg，超出正常范围，请关注',1,'2026-02-28 09:00:00','张明（子女）','已提醒老人按时服用降压药','2026-02-27 18:00:00');
