-- 1. 创建 lease_upms 数据库
CREATE DATABASE IF NOT EXISTS lease_upms DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE lease_upms;

-- 2. 创建 system_user 表 (管理员)
CREATE TABLE IF NOT EXISTS `system_user` (
                                             `id` bigint NOT NULL AUTO_INCREMENT COMMENT '员工id',
                                             `username` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '用户名',
                                             `password` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '密码',
                                             `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '姓名',
                                             `type` tinyint NULL DEFAULT NULL COMMENT '用户类型',
                                             `phone` varchar(11) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '手机号码',
                                             `avatar_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '头像地址',
                                             `additional_info` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注信息',
                                             `post_id` bigint NULL DEFAULT NULL COMMENT '岗位id',
                                             `status` tinyint NULL DEFAULT 1 COMMENT '账号状态(1正常,0禁用)',
                                             `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                             `update_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                             `is_deleted` tinyint NULL DEFAULT 0 COMMENT '是否删除',
                                             PRIMARY KEY (`id`)
) ENGINE=InnoDB COMMENT='员工信息表';

-- 3. 创建 user_info 表 (C端用户)
CREATE TABLE IF NOT EXISTS `user_info` (
                                           `id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户id',
                                           `phone` varchar(11) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '手机号码',
                                           `password` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '密码',
                                           `avatar_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '头像url',
                                           `nickname` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '昵称',
                                           `status` tinyint NULL DEFAULT 1 COMMENT '账号状态(1正常,0禁用)',
                                           `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                           `update_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                           `is_deleted` tinyint NULL DEFAULT 0 COMMENT '是否删除',
                                           PRIMARY KEY (`id`)
) ENGINE=InnoDB COMMENT='用户信息表';

-- 4. 插入默认管理员账号 (密码: 123456, MD5加密)
INSERT INTO `system_user` (`id`, `username`, `password`, `name`, `type`, `phone`, `status`)
VALUES (1, 'admin', 'e10adc3949ba59abbe56e057f20f883e', '管理员', 0, '18888888888', 1);

-- 5. 插入测试用户
INSERT INTO `user_info` (`id`, `phone`, `nickname`, `status`)
VALUES (1, '13888888888', '测试用户', 1);
