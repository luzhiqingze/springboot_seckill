SET FOREIGN_KEY_CHECKS=0;


-- ===============================
-- 2. 用户表（升级版）
-- ===============================
DROP TABLE IF EXISTS seckill_user;
CREATE TABLE `seckill_user` (
                                `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
                                `mobile` VARCHAR(20) NOT NULL COMMENT '手机号',
                                `nickname` VARCHAR(255) NOT NULL COMMENT '昵称',
                                `password` VARCHAR(100) NOT NULL COMMENT '加密密码（支持BCrypt）',
                                `salt` VARCHAR(32) DEFAULT NULL COMMENT '加密盐',
                                `head` VARCHAR(128) DEFAULT NULL COMMENT '头像',
                                `register_date` DATETIME DEFAULT CURRENT_TIMESTAMP,
                                `last_login_date` DATETIME DEFAULT NULL,
                                `login_count` INT DEFAULT 0,
                                PRIMARY KEY (`id`),
                                UNIQUE KEY `uk_mobile` (`mobile`)
) ENGINE=InnoDB COMMENT='用户表';

-- ===============================
-- 3. 商品表
-- ===============================
DROP TABLE IF EXISTS goods;
CREATE TABLE `goods` (
                         `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '商品ID',
                         `goods_name` VARCHAR(30) DEFAULT NULL,
                         `goods_title` VARCHAR(64) DEFAULT NULL,
                         `goods_img` VARCHAR(128) DEFAULT NULL,
                         `goods_detail` LONGTEXT,
                         `goods_price` DECIMAL(10,2) DEFAULT '0.00',
                         `goods_stock` INT DEFAULT '0',
                         `create_date` DATETIME DEFAULT CURRENT_TIMESTAMP,
                         `update_date` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                         PRIMARY KEY (`id`)
) ENGINE=InnoDB COMMENT='商品表';

-- ===============================
-- 4. 秒杀商品表（核心）
-- ===============================
DROP TABLE IF EXISTS seckill_goods;
CREATE TABLE `seckill_goods` (
                                 `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '秒杀商品ID',
                                 `goods_id` BIGINT NOT NULL COMMENT '商品ID',
                                 `seckill_price` DECIMAL(10,2) DEFAULT '0.00',
                                 `stock_count` INT DEFAULT '0',
                                 `start_date` DATETIME DEFAULT NULL,
                                 `end_date` DATETIME DEFAULT NULL,
                                 `version` INT DEFAULT '0' COMMENT '乐观锁版本号',
                                 PRIMARY KEY (`id`),
                                 UNIQUE KEY `uk_goods_id` (`goods_id`),
                                 KEY `idx_time` (`start_date`, `end_date`)
) ENGINE=InnoDB COMMENT='秒杀商品表';

-- ===============================
-- 5. 订单表（升级版）
-- ===============================
DROP TABLE IF EXISTS order_info;
CREATE TABLE `order_info` (
                              `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '订单ID',
                              `user_id` BIGINT DEFAULT NULL,
                              `goods_id` BIGINT DEFAULT NULL,
                              `goods_name` VARCHAR(30) DEFAULT NULL,
                              `goods_count` INT DEFAULT '0',
                              `goods_price` DECIMAL(10,2) DEFAULT '0.00',
                              `order_channel` TINYINT DEFAULT '0' COMMENT '1PC 2Android 3iOS',
                              `status` TINYINT DEFAULT '0' COMMENT '0新建 1已支付 2已发货 3已收货 4退款',
                              `pay_status` TINYINT DEFAULT '0' COMMENT '0未支付 1已支付',
                              `create_date` DATETIME DEFAULT CURRENT_TIMESTAMP,
                              `pay_date` DATETIME DEFAULT NULL,
                              `close_time` DATETIME DEFAULT NULL,
                              PRIMARY KEY (`id`),
                              KEY `idx_user_id` (`user_id`),
                              KEY `idx_goods_id` (`goods_id`)
) ENGINE=InnoDB COMMENT='订单表';

-- ===============================
-- 6. 秒杀订单表（防重复）
-- ===============================
DROP TABLE IF EXISTS seckill_order;
CREATE TABLE `seckill_order` (
                                 `id` BIGINT NOT NULL AUTO_INCREMENT,
                                 `user_id` BIGINT NOT NULL,
                                 `order_id` BIGINT NOT NULL,
                                 `goods_id` BIGINT NOT NULL,
                                 PRIMARY KEY (`id`),
                                 UNIQUE KEY `uk_user_goods` (`user_id`,`goods_id`),
                                 KEY `idx_goods_id` (`goods_id`)
) ENGINE=InnoDB COMMENT='秒杀订单表';

-- ===============================
-- 7. 秒杀库存流水表（高并发兜底）
-- ===============================
DROP TABLE IF EXISTS seckill_stock_log;
CREATE TABLE `seckill_stock_log` (
                                     `id` BIGINT NOT NULL AUTO_INCREMENT,
                                     `goods_id` BIGINT NOT NULL,
                                     `order_id` BIGINT DEFAULT NULL,
                                     `status` TINYINT NOT NULL COMMENT '0初始化 1扣减成功 2回滚',
                                     `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
                                     PRIMARY KEY (`id`),
                                     KEY `idx_goods_id` (`goods_id`)
) ENGINE=InnoDB COMMENT='秒杀库存流水表';

-- ===============================
-- 8. 接口幂等表（防重复提交）
-- ===============================
DROP TABLE IF EXISTS request_log;
CREATE TABLE `request_log` (
                               `id` BIGINT NOT NULL AUTO_INCREMENT,
                               `request_id` VARCHAR(64) NOT NULL,
                               `user_id` BIGINT DEFAULT NULL,
                               `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
                               PRIMARY KEY (`id`),
                               UNIQUE KEY `uk_request_id` (`request_id`)
) ENGINE=InnoDB COMMENT='接口幂等日志表';
-- Sample test user (password: 123456)
INSERT INTO seckill_user (mobile, nickname, password, salt)
VALUES ('13800138000', 'test_user', 'e10adc3949ba59abbe56e057f20f883e', '1a2b3c4d');

-- Sample goods
INSERT INTO goods (goods_name, goods_title, goods_img, goods_detail, goods_price, goods_stock)
VALUES ('iPhone 14', 'Apple iPhone 14 Pro Max', '/img/iphone14.jpg', 'Latest iPhone model', 9999.00, 100);

-- Sample seckill goods
INSERT INTO seckill_goods (goods_id, seckill_price, stock_count, start_date, end_date)
VALUES (1, 8999.00, 10, '2024-01-01 00:00:00', '2024-01-02 23:59:59');