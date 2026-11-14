-- =============================================================
-- 优果仓（YouGuoCang）干果电商平台 — 数据库结构
-- MySQL 8.0+ / utf8mb4
-- 设计原则：
--   1. 三端角色分表（t_user / t_merchant / t_admin），便于权限隔离与分别扩展
--   2. 订单、评价等核心单据保留商品快照字段，避免商品改价后历史单据失真
--   3. 所有业务表带 deleted 逻辑删除位与 created_at / updated_at 审计时间
--   4. 推荐所需的共现数据单独落表（t_product_cooccur），由离线任务刷新
-- =============================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

CREATE DATABASE IF NOT EXISTS `youguocang`
    DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
USE `youguocang`;

-- -------------------------------------------------------------
-- 1. 管理端账号
-- -------------------------------------------------------------
DROP TABLE IF EXISTS `t_admin`;
CREATE TABLE `t_admin` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `username`    VARCHAR(32)  NOT NULL COMMENT '登录名',
    `password`    VARCHAR(100) NOT NULL COMMENT 'BCrypt 密文',
    `real_name`   VARCHAR(32)  DEFAULT NULL COMMENT '姓名',
    `phone`       VARCHAR(20)  DEFAULT NULL COMMENT '手机号',
    `status`      TINYINT      NOT NULL DEFAULT 1 COMMENT '账号状态：0禁用 1启用',
    `created_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted`     TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除：0否 1是',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_admin_username` (`username`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='管理端账号';

-- -------------------------------------------------------------
-- 2. 商家账号
-- -------------------------------------------------------------
DROP TABLE IF EXISTS `t_merchant`;
CREATE TABLE `t_merchant` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `username`    VARCHAR(32)  NOT NULL COMMENT '登录名',
    `password`    VARCHAR(100) NOT NULL COMMENT 'BCrypt 密文',
    `phone`       VARCHAR(20)  NOT NULL COMMENT '手机号',
    `status`      TINYINT      NOT NULL DEFAULT 1 COMMENT '账号状态：0禁用 1启用',
    `created_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted`     TINYINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_merchant_username` (`username`),
    KEY `idx_merchant_phone` (`phone`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='商家账号';

-- -------------------------------------------------------------
-- 3. 店铺信息（与商家账号 1:1）
-- -------------------------------------------------------------
DROP TABLE IF EXISTS `t_shop`;
CREATE TABLE `t_shop` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `merchant_id` BIGINT       NOT NULL COMMENT '所属商家',
    `shop_name`   VARCHAR(64)  NOT NULL COMMENT '店铺名称',
    `intro`       VARCHAR(500) DEFAULT NULL COMMENT '店铺简介',
    `contact`     VARCHAR(64)  DEFAULT NULL COMMENT '联系方式',
    `logo`        VARCHAR(255) DEFAULT NULL COMMENT '店铺 Logo',
    `status`      TINYINT      NOT NULL DEFAULT 1 COMMENT '营业状态：0关店 1营业',
    `created_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted`     TINYINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_shop_merchant` (`merchant_id`),
    CONSTRAINT `fk_shop_merchant` FOREIGN KEY (`merchant_id`) REFERENCES `t_merchant` (`id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='店铺信息';

-- -------------------------------------------------------------
-- 4. 用户账号
-- -------------------------------------------------------------
DROP TABLE IF EXISTS `t_user`;
CREATE TABLE `t_user` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `username`    VARCHAR(32)  NOT NULL COMMENT '用户名',
    `phone`       VARCHAR(20)  NOT NULL COMMENT '手机号（登录用）',
    `password`    VARCHAR(100) NOT NULL COMMENT 'BCrypt 密文',
    `nickname`    VARCHAR(32)  DEFAULT NULL COMMENT '昵称',
    `avatar`      VARCHAR(255) DEFAULT NULL COMMENT '头像',
    `email`       VARCHAR(64)  DEFAULT NULL COMMENT '邮箱',
    `status`      TINYINT      NOT NULL DEFAULT 1 COMMENT '账号状态：0禁用 1启用',
    `created_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
    `updated_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted`     TINYINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_username` (`username`),
    UNIQUE KEY `uk_user_phone` (`phone`),
    KEY `idx_user_created` (`created_at`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='用户账号';

-- -------------------------------------------------------------
-- 5. 收货地址
-- -------------------------------------------------------------
DROP TABLE IF EXISTS `t_address`;
CREATE TABLE `t_address` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT,
    `user_id`     BIGINT       NOT NULL COMMENT '所属用户',
    `receiver`    VARCHAR(32)  NOT NULL COMMENT '收货人',
    `phone`       VARCHAR(20)  NOT NULL COMMENT '联系电话',
    `province`    VARCHAR(32)  NOT NULL COMMENT '省',
    `city`        VARCHAR(32)  NOT NULL COMMENT '市',
    `district`    VARCHAR(32)  NOT NULL COMMENT '区/县',
    `detail`      VARCHAR(255) NOT NULL COMMENT '详细地址',
    `is_default`  TINYINT      NOT NULL DEFAULT 0 COMMENT '是否默认：0否 1是',
    `created_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted`     TINYINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    KEY `idx_address_user` (`user_id`),
    CONSTRAINT `fk_address_user` FOREIGN KEY (`user_id`) REFERENCES `t_user` (`id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='收货地址';

-- -------------------------------------------------------------
-- 6. 商品分类（逐级）
-- -------------------------------------------------------------
DROP TABLE IF EXISTS `t_category`;
CREATE TABLE `t_category` (
    `id`          BIGINT      NOT NULL AUTO_INCREMENT,
    `parent_id`   BIGINT      NOT NULL DEFAULT 0 COMMENT '上级分类，0 为顶级',
    `name`        VARCHAR(32) NOT NULL COMMENT '分类名称',
    `sort`        INT         NOT NULL DEFAULT 0 COMMENT '排序值，越小越前',
    `status`      TINYINT     NOT NULL DEFAULT 1 COMMENT '状态：0停用 1启用',
    `created_at`  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted`     TINYINT     NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    KEY `idx_category_parent` (`parent_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='商品分类';

-- -------------------------------------------------------------
-- 7. 商品
--    status 控制上下架；deleted 由「分类删除校验」与商家删除共用
-- -------------------------------------------------------------
DROP TABLE IF EXISTS `t_product`;
CREATE TABLE `t_product` (
    `id`            BIGINT        NOT NULL AUTO_INCREMENT,
    `merchant_id`   BIGINT        NOT NULL COMMENT '所属商家',
    `shop_id`       BIGINT        NOT NULL COMMENT '所属店铺',
    `category_id`   BIGINT        NOT NULL COMMENT '分类',
    `name`          VARCHAR(128)  NOT NULL COMMENT '商品名称',
    `price`         DECIMAL(10,2) NOT NULL COMMENT '售价',
    `origin_price`  DECIMAL(10,2) DEFAULT NULL COMMENT '原价（展示划线价）',
    `origin_place`  VARCHAR(64)   DEFAULT NULL COMMENT '产地',
    `spec`          VARCHAR(64)   DEFAULT NULL COMMENT '规格，如 500g/袋',
    `stock`         INT           NOT NULL DEFAULT 0 COMMENT '库存',
    `sales_count`   INT           NOT NULL DEFAULT 0 COMMENT '累计销量（冗余，供热门排序）',
    `main_image`    VARCHAR(255)  DEFAULT NULL COMMENT '主图',
    `detail`        TEXT          COMMENT '图文介绍',
    `status`        TINYINT       NOT NULL DEFAULT 0 COMMENT '上架状态：0下架 1上架',
    `created_at`    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted`       TINYINT       NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    KEY `idx_product_merchant` (`merchant_id`),
    KEY `idx_product_category` (`category_id`),
    KEY `idx_product_status` (`status`, `deleted`),
    KEY `idx_product_sales` (`sales_count`),
    CONSTRAINT `fk_product_merchant` FOREIGN KEY (`merchant_id`) REFERENCES `t_merchant` (`id`),
    CONSTRAINT `fk_product_shop` FOREIGN KEY (`shop_id`) REFERENCES `t_shop` (`id`),
    CONSTRAINT `fk_product_category` FOREIGN KEY (`category_id`) REFERENCES `t_category` (`id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='商品';

-- -------------------------------------------------------------
-- 8. 商品图片（细节图，主图另存于 t_product.main_image）
-- -------------------------------------------------------------
DROP TABLE IF EXISTS `t_product_image`;
CREATE TABLE `t_product_image` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT,
    `product_id`  BIGINT       NOT NULL,
    `url`         VARCHAR(255) NOT NULL COMMENT '图片地址',
    `sort`        INT          NOT NULL DEFAULT 0 COMMENT '展示顺序',
    `created_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_pimage_product` (`product_id`),
    CONSTRAINT `fk_pimage_product` FOREIGN KEY (`product_id`) REFERENCES `t_product` (`id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='商品细节图';

-- -------------------------------------------------------------
-- 9. 收藏
-- -------------------------------------------------------------
DROP TABLE IF EXISTS `t_favorite`;
CREATE TABLE `t_favorite` (
    `id`          BIGINT   NOT NULL AUTO_INCREMENT,
    `user_id`     BIGINT   NOT NULL,
    `product_id`  BIGINT   NOT NULL,
    `created_at`  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_favorite_user_product` (`user_id`, `product_id`),
    KEY `idx_favorite_product` (`product_id`),
    CONSTRAINT `fk_favorite_user` FOREIGN KEY (`user_id`) REFERENCES `t_user` (`id`),
    CONSTRAINT `fk_favorite_product` FOREIGN KEY (`product_id`) REFERENCES `t_product` (`id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='商品收藏';

-- -------------------------------------------------------------
-- 10. 购物车
--    同一用户同一商品仅一行（加入时自动合并数量），checked 控制是否参与结算
-- -------------------------------------------------------------
DROP TABLE IF EXISTS `t_cart_item`;
CREATE TABLE `t_cart_item` (
    `id`          BIGINT   NOT NULL AUTO_INCREMENT,
    `user_id`     BIGINT   NOT NULL,
    `product_id`  BIGINT   NOT NULL,
    `quantity`    INT      NOT NULL DEFAULT 1 COMMENT '数量',
    `checked`     TINYINT  NOT NULL DEFAULT 1 COMMENT '是否勾选：0否 1是',
    `created_at`  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_cart_user_product` (`user_id`, `product_id`),
    CONSTRAINT `fk_cart_user` FOREIGN KEY (`user_id`) REFERENCES `t_user` (`id`),
    CONSTRAINT `fk_cart_product` FOREIGN KEY (`product_id`) REFERENCES `t_product` (`id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='购物车';

-- -------------------------------------------------------------
-- 11. 订单主表
--     status：0待付款 1待发货 2待收货 3已完成 4已取消 5售后中
-- -------------------------------------------------------------
DROP TABLE IF EXISTS `t_order`;
CREATE TABLE `t_order` (
    `id`              BIGINT        NOT NULL AUTO_INCREMENT,
    `order_no`        VARCHAR(32)   NOT NULL COMMENT '订单号',
    `user_id`         BIGINT        NOT NULL,
    `merchant_id`     BIGINT        NOT NULL COMMENT '所属商家（便于商家端隔离）',
    `status`          TINYINT       NOT NULL DEFAULT 0 COMMENT '0待付款 1待发货 2待收货 3已完成 4已取消 5售后中',
    `total_amount`    DECIMAL(10,2) NOT NULL COMMENT '商品总额',
    `pay_amount`      DECIMAL(10,2) NOT NULL COMMENT '应付金额',
    `receiver`        VARCHAR(32)   NOT NULL COMMENT '收货人（快照）',
    `receiver_phone`  VARCHAR(20)   NOT NULL COMMENT '联系电话（快照）',
    `receiver_addr`   VARCHAR(300)  NOT NULL COMMENT '收货地址全文（快照）',
    `remark`          VARCHAR(255)  DEFAULT NULL COMMENT '买家备注',
    `paid_at`         DATETIME      DEFAULT NULL COMMENT '付款时间',
    `shipped_at`      DATETIME      DEFAULT NULL COMMENT '发货时间',
    `finished_at`     DATETIME      DEFAULT NULL COMMENT '完成时间',
    `cancelled_at`    DATETIME      DEFAULT NULL COMMENT '取消时间',
    `created_at`      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '下单时间',
    `updated_at`      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted`         TINYINT       NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order_no` (`order_no`),
    KEY `idx_order_user` (`user_id`, `status`),
    KEY `idx_order_merchant` (`merchant_id`, `status`),
    KEY `idx_order_created` (`created_at`),
    CONSTRAINT `fk_order_user` FOREIGN KEY (`user_id`) REFERENCES `t_user` (`id`),
    CONSTRAINT `fk_order_merchant` FOREIGN KEY (`merchant_id`) REFERENCES `t_merchant` (`id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='订单';

-- -------------------------------------------------------------
-- 12. 订单明细（商品快照）
-- -------------------------------------------------------------
DROP TABLE IF EXISTS `t_order_item`;
CREATE TABLE `t_order_item` (
    `id`            BIGINT        NOT NULL AUTO_INCREMENT,
    `order_id`      BIGINT        NOT NULL,
    `product_id`    BIGINT        NOT NULL,
    `product_name`  VARCHAR(128)  NOT NULL COMMENT '下单时商品名（快照）',
    `product_image` VARCHAR(255)  DEFAULT NULL COMMENT '下单时主图（快照）',
    `spec`          VARCHAR(64)   DEFAULT NULL COMMENT '下单时规格（快照）',
    `price`         DECIMAL(10,2) NOT NULL COMMENT '成交单价',
    `quantity`      INT           NOT NULL COMMENT '数量',
    `amount`        DECIMAL(10,2) NOT NULL COMMENT '小计',
    `reviewed`      TINYINT       NOT NULL DEFAULT 0 COMMENT '是否已评价：0否 1是',
    `created_at`    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_oitem_order` (`order_id`),
    KEY `idx_oitem_product` (`product_id`),
    CONSTRAINT `fk_oitem_order` FOREIGN KEY (`order_id`) REFERENCES `t_order` (`id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='订单明细';

-- -------------------------------------------------------------
-- 13. 售后单（用户申请 → 商家处理 → 平台介入）
--     type：1取消订单 2退款 3退货退款
--     status：0待商家处理 1商家已同意 2商家已拒绝 3平台介入中 4已完成 5已撤销
-- -------------------------------------------------------------
DROP TABLE IF EXISTS `t_after_sale`;
CREATE TABLE `t_after_sale` (
    `id`             BIGINT       NOT NULL AUTO_INCREMENT,
    `after_sale_no`  VARCHAR(32)  NOT NULL COMMENT '售后单号',
    `order_id`       BIGINT       NOT NULL,
    `order_item_id`  BIGINT       DEFAULT NULL COMMENT '整单售后时为空',
    `user_id`        BIGINT       NOT NULL,
    `merchant_id`    BIGINT       NOT NULL,
    `type`           TINYINT      NOT NULL COMMENT '1取消订单 2退款 3退货退款',
    `reason`         VARCHAR(255) NOT NULL COMMENT '申请原因',
    `status`         TINYINT      NOT NULL DEFAULT 0 COMMENT '0待商家处理 1商家已同意 2商家已拒绝 3平台介入中 4已完成 5已撤销',
    `merchant_remark` VARCHAR(255) DEFAULT NULL COMMENT '商家处理意见',
    `admin_id`       BIGINT       DEFAULT NULL COMMENT '介入的管理员',
    `admin_remark`   VARCHAR(255) DEFAULT NULL COMMENT '平台裁定意见',
    `handled_at`     DATETIME     DEFAULT NULL,
    `created_at`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_after_sale_no` (`after_sale_no`),
    KEY `idx_as_order` (`order_id`),
    KEY `idx_as_merchant` (`merchant_id`, `status`),
    CONSTRAINT `fk_as_order` FOREIGN KEY (`order_id`) REFERENCES `t_order` (`id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='售后单';

-- -------------------------------------------------------------
-- 14. 商品评价（提交后待审核，审核通过才对外展示）
-- -------------------------------------------------------------
DROP TABLE IF EXISTS `t_review`;
CREATE TABLE `t_review` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT,
    `order_item_id` BIGINT     NOT NULL COMMENT '对应订单明细，保证一单一评',
    `order_id`    BIGINT       NOT NULL,
    `user_id`     BIGINT       NOT NULL,
    `product_id`  BIGINT       NOT NULL,
    `merchant_id` BIGINT       NOT NULL,
    `score`       TINYINT      NOT NULL DEFAULT 5 COMMENT '1-5 星',
    `content`     VARCHAR(1000) NOT NULL COMMENT '评价内容',
    `images`      VARCHAR(1000) DEFAULT NULL COMMENT '图片地址，逗号分隔',
    `status`      TINYINT      NOT NULL DEFAULT 0 COMMENT '0待审核 1已通过 2已拒绝',
    `audit_admin_id` BIGINT    DEFAULT NULL COMMENT '审核人',
    `audit_remark`   VARCHAR(255) DEFAULT NULL,
    `audited_at`  DATETIME     DEFAULT NULL,
    `created_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `deleted`     TINYINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_review_order_item` (`order_item_id`),
    KEY `idx_review_product` (`product_id`, `status`),
    KEY `idx_review_merchant` (`merchant_id`, `status`),
    CONSTRAINT `fk_review_oitem` FOREIGN KEY (`order_item_id`) REFERENCES `t_order_item` (`id`),
    CONSTRAINT `fk_review_product` FOREIGN KEY (`product_id`) REFERENCES `t_product` (`id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='商品评价';

-- -------------------------------------------------------------
-- 15. 商家回复（同样待审核）
-- -------------------------------------------------------------
DROP TABLE IF EXISTS `t_review_reply`;
CREATE TABLE `t_review_reply` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT,
    `review_id`   BIGINT       NOT NULL,
    `merchant_id` BIGINT       NOT NULL,
    `content`     VARCHAR(500) NOT NULL COMMENT '回复内容',
    `status`      TINYINT      NOT NULL DEFAULT 0 COMMENT '0待审核 1已通过 2已拒绝',
    `audited_at`  DATETIME     DEFAULT NULL,
    `created_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_reply_review` (`review_id`),
    CONSTRAINT `fk_reply_review` FOREIGN KEY (`review_id`) REFERENCES `t_review` (`id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='商家回复';

-- -------------------------------------------------------------
-- 16. 平台公告
-- -------------------------------------------------------------
DROP TABLE IF EXISTS `t_notice`;
CREATE TABLE `t_notice` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT,
    `title`       VARCHAR(128) NOT NULL,
    `content`     TEXT         NOT NULL,
    `publisher_id` BIGINT      NOT NULL COMMENT '发布管理员',
    `status`      TINYINT      NOT NULL DEFAULT 1 COMMENT '0已下架 1已发布',
    `created_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted`     TINYINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    KEY `idx_notice_status` (`status`, `created_at`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='平台公告';

-- -------------------------------------------------------------
-- 17. 站内通知（订单状态变更提示）
-- -------------------------------------------------------------
DROP TABLE IF EXISTS `t_notification`;
CREATE TABLE `t_notification` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT,
    `user_id`     BIGINT       NOT NULL,
    `type`        TINYINT      NOT NULL DEFAULT 1 COMMENT '1订单通知 2系统通知 3售后通知',
    `title`       VARCHAR(128) NOT NULL,
    `content`     VARCHAR(500) NOT NULL,
    `biz_id`      BIGINT       DEFAULT NULL COMMENT '关联业务 ID（订单/售后）',
    `is_read`     TINYINT      NOT NULL DEFAULT 0 COMMENT '0未读 1已读',
    `created_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_notify_user` (`user_id`, `is_read`),
    CONSTRAINT `fk_notify_user` FOREIGN KEY (`user_id`) REFERENCES `t_user` (`id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='站内通知';

-- -------------------------------------------------------------
-- 18. 浏览行为日志（推荐算法的数据来源）
-- -------------------------------------------------------------
DROP TABLE IF EXISTS `t_browse_log`;
CREATE TABLE `t_browse_log` (
    `id`          BIGINT   NOT NULL AUTO_INCREMENT,
    `user_id`     BIGINT   NOT NULL,
    `product_id`  BIGINT   NOT NULL,
    `source`      TINYINT  NOT NULL DEFAULT 1 COMMENT '来源：1首页 2分类页 3搜索 4详情页推荐位',
    `created_at`  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_browse_user` (`user_id`, `created_at`),
    KEY `idx_browse_product` (`product_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='浏览行为日志';

-- -------------------------------------------------------------
-- 19. 商品共现统计（离线任务产出，供基于物品的协同过滤直接查询）
--     同一用户对 a、b 均产生过行为则计一次共现
-- -------------------------------------------------------------
DROP TABLE IF EXISTS `t_product_cooccur`;
CREATE TABLE `t_product_cooccur` (
    `id`          BIGINT   NOT NULL AUTO_INCREMENT,
    `product_id`  BIGINT   NOT NULL COMMENT '物品 A',
    `related_id`  BIGINT   NOT NULL COMMENT '物品 B',
    `co_count`    INT      NOT NULL DEFAULT 0 COMMENT '共现次数',
    `updated_at`  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_cooccur_pair` (`product_id`, `related_id`),
    KEY `idx_cooccur_related` (`related_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='商品共现统计';

-- -------------------------------------------------------------
-- 20. 操作日志（审计用：仅查询与筛选，不提供删除）
-- -------------------------------------------------------------
DROP TABLE IF EXISTS `t_operation_log`;
CREATE TABLE `t_operation_log` (
    `id`            BIGINT       NOT NULL AUTO_INCREMENT,
    `operator_type` TINYINT      NOT NULL COMMENT '1管理员 2商家',
    `operator_id`   BIGINT       NOT NULL,
    `operator_name` VARCHAR(32)  NOT NULL COMMENT '操作人名称（快照）',
    `action`        VARCHAR(64)  NOT NULL COMMENT '操作类型，如 PRODUCT_OFFLINE',
    `target`        VARCHAR(128) DEFAULT NULL COMMENT '操作对象描述',
    `detail`        VARCHAR(500) DEFAULT NULL COMMENT '操作详情',
    `ip`            VARCHAR(64)  DEFAULT NULL,
    `created_at`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_oplog_time` (`created_at`),
    KEY `idx_oplog_operator` (`operator_type`, `operator_id`),
    KEY `idx_oplog_action` (`action`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='操作日志';

-- -------------------------------------------------------------
-- 21. 推荐结果日志（支撑「每条推荐附带推荐依据」的可解释性）
-- -------------------------------------------------------------
DROP TABLE IF EXISTS `t_recommend_log`;
CREATE TABLE `t_recommend_log` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT,
    `user_id`     BIGINT       NOT NULL,
    `product_id`  BIGINT       NOT NULL,
    `scene`       VARCHAR(32)  NOT NULL COMMENT '场景：HOME / DETAIL / CART',
    `strategy`    VARCHAR(32)  NOT NULL COMMENT '策略：COLD_START / ITEM_CF / HYBRID',
    `reason`      VARCHAR(255) NOT NULL COMMENT '推荐理由文案',
    `score`       DECIMAL(10,4) DEFAULT NULL COMMENT '推荐得分',
    `created_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_reclog_user` (`user_id`, `created_at`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='推荐结果日志';

SET FOREIGN_KEY_CHECKS = 1;
