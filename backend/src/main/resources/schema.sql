-- Spring Boot 自动执行（application.properties 配置 spring.sql.init.mode=always）
-- 仅在表不存在时创建，不影响已有数据

CREATE TABLE IF NOT EXISTS `user` (
  `phone`           VARCHAR(11)  NOT NULL,
  `password`        VARCHAR(100) NOT NULL,
  `gender`          VARCHAR(10)  NOT NULL,
  `avatar_url`      VARCHAR(500)          DEFAULT NULL,
  `nickname`        VARCHAR(50)           DEFAULT NULL,
  `age`             INT                   DEFAULT NULL,
  `role`            VARCHAR(20)           DEFAULT 'USER',
  `status`          VARCHAR(20)           DEFAULT 'ACTIVE',
  `created_at`      DATETIME              DEFAULT NULL,
  `updated_at`      DATETIME              DEFAULT NULL,
  `last_login_at`   DATETIME              DEFAULT NULL,
  PRIMARY KEY (`phone`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `elderly` (
  `id`                BIGINT       NOT NULL AUTO_INCREMENT,
  `name`              VARCHAR(50)  NOT NULL,
  `age`               INT                   DEFAULT NULL,
  `gender`            VARCHAR(10)           DEFAULT NULL,
  `height`            DOUBLE                DEFAULT NULL,
  `weight`            DOUBLE                DEFAULT NULL,
  `blood_type`        VARCHAR(10)           DEFAULT NULL,
  `emergency_contact` VARCHAR(50)           DEFAULT NULL,
  `emergency_phone`   VARCHAR(20)           DEFAULT NULL,
  `address`           VARCHAR(200)          DEFAULT NULL,
  `medical_history`   TEXT,
  `created_at`        DATETIME              DEFAULT NULL,
  `updated_at`        DATETIME              DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `physiological_data` (
  `id`                  BIGINT   NOT NULL AUTO_INCREMENT,
  `elderly_id`          BIGINT   NOT NULL,
  `heart_rate`          INT               DEFAULT NULL,
  `blood_pressure_high` INT               DEFAULT NULL,
  `blood_pressure_low`  INT               DEFAULT NULL,
  `blood_oxygen`        INT               DEFAULT NULL,
  `body_temperature`    DOUBLE            DEFAULT NULL,
  `respiratory_rate`    INT               DEFAULT NULL,
  `blood_sugar`         DOUBLE            DEFAULT NULL,
  `recorded_at`         DATETIME NOT NULL,
  PRIMARY KEY (`id`),
  INDEX `idx_physiological_elderly_id` (`elderly_id`),
  INDEX `idx_physiological_recorded_at` (`recorded_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `behavior_data` (
  `id`             BIGINT      NOT NULL AUTO_INCREMENT,
  `elderly_id`     BIGINT      NOT NULL,
  `position_x`     DOUBLE               DEFAULT NULL,
  `position_y`     DOUBLE               DEFAULT NULL,
  `position_z`     DOUBLE               DEFAULT NULL,
  `movement_speed` DOUBLE               DEFAULT NULL,
  `stay_duration`  INT                  DEFAULT NULL,
  `activity_type`  VARCHAR(50)          DEFAULT NULL,
  `recorded_at`    DATETIME    NOT NULL,
  PRIMARY KEY (`id`),
  INDEX `idx_behavior_elderly_id` (`elderly_id`),
  INDEX `idx_behavior_recorded_at` (`recorded_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `environment_data` (
  `id`                BIGINT   NOT NULL AUTO_INCREMENT,
  `elderly_id`        BIGINT   NOT NULL,
  `room_temperature`  DOUBLE            DEFAULT NULL,
  `humidity`          INT               DEFAULT NULL,
  `air_quality_index` INT               DEFAULT NULL,
  `light_intensity`   INT               DEFAULT NULL,
  `noise_level`       INT               DEFAULT NULL,
  `recorded_at`       DATETIME NOT NULL,
  PRIMARY KEY (`id`),
  INDEX `idx_environment_elderly_id` (`elderly_id`),
  INDEX `idx_environment_recorded_at` (`recorded_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `alert_record` (
  `id`            BIGINT      NOT NULL AUTO_INCREMENT,
  `elderly_id`    BIGINT      NOT NULL,
  `alert_type`    VARCHAR(50)          DEFAULT NULL,
  `alert_level`   VARCHAR(20)          DEFAULT NULL,
  `alert_message` TEXT,
  `is_handled`    TINYINT(1)           DEFAULT 0,
  `handled_at`    DATETIME             DEFAULT NULL,
  `handler`       VARCHAR(50)          DEFAULT NULL,
  `handle_note`   TEXT,
  `created_at`    DATETIME    NOT NULL,
  PRIMARY KEY (`id`),
  INDEX `idx_alert_elderly_id` (`elderly_id`),
  INDEX `idx_alert_is_handled` (`is_handled`),
  INDEX `idx_alert_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `health_risk_record` (
  `id`                BIGINT      NOT NULL AUTO_INCREMENT,
  `elderly_id`        BIGINT      NOT NULL,
  `risk_score`        DOUBLE               DEFAULT NULL,
  `risk_level`        VARCHAR(20)          DEFAULT NULL,
  `status_label`      VARCHAR(50)          DEFAULT NULL,
  `risk_factors`      TEXT,
  `prediction_result` TEXT,
  `is_alerted`        TINYINT(1)           DEFAULT 0,
  `alert_sent_at`     DATETIME             DEFAULT NULL,
  `created_at`        DATETIME    NOT NULL,
  PRIMARY KEY (`id`),
  INDEX `idx_risk_elderly_id` (`elderly_id`),
  INDEX `idx_risk_created_at` (`created_at`),
  INDEX `idx_risk_level` (`risk_level`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `device` (
  `id`               BIGINT       NOT NULL AUTO_INCREMENT,
  `user_phone`       VARCHAR(11)  NOT NULL,
  `device_name`      VARCHAR(50)  NOT NULL,
  `device_type`      VARCHAR(20)           DEFAULT NULL,
  `device_sn`        VARCHAR(100)          DEFAULT NULL,
  `status`           VARCHAR(20)           DEFAULT 'binding',
  `bind_time`        DATETIME              DEFAULT NULL,
  `last_online_time` DATETIME              DEFAULT NULL,
  PRIMARY KEY (`id`),
  INDEX `idx_device_user_phone` (`user_phone`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `emergency_contact` (
  `id`            BIGINT      NOT NULL AUTO_INCREMENT,
  `user_phone`    VARCHAR(11) NOT NULL,
  `name`          VARCHAR(30) NOT NULL,
  `contact_phone` VARCHAR(11) NOT NULL,
  `relationship`  VARCHAR(20)          DEFAULT NULL,
  `is_primary`    TINYINT(1)           DEFAULT 0,
  `created_at`    DATETIME             DEFAULT NULL,
  `updated_at`    DATETIME             DEFAULT NULL,
  PRIMARY KEY (`id`),
  INDEX `idx_contact_user_phone` (`user_phone`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
