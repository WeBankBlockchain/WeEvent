/*
 Navicat Premium Data Transfer

 Source Server         : 127.0.0.1
 Source Server Type    : MySQL
 Source Server Version : 50727
 Source Host           : 127.0.0.1:3306
 Source Schema         : cep

 Target Server Type    : MySQL
 Target Server Version : 50727
 File Encoding         : 65001

 Date: 15/10/2019 20:28:29
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for cep_rule
-- ----------------------------
DROP TABLE IF EXISTS `cep_rule`;
CREATE TABLE `cep_rule`  (
  `id` varchar(20) CHARACTER SET latin1 COLLATE latin1_swedish_ci NOT NULL COMMENT '唯一标识',
  `rule_name` varchar(128) CHARACTER SET latin1 COLLATE latin1_swedish_ci NOT NULL COMMENT '规则名称',
  `from_destination` varchar(64) CHARACTER SET latin1 COLLATE latin1_swedish_ci NULL DEFAULT NULL COMMENT '数据的来源',
  `broker_url` varchar(255) CHARACTER SET latin1 COLLATE latin1_swedish_ci NULL DEFAULT NULL COMMENT '触发Topic，流向的broker',
  `payload` varchar(255) CHARACTER SET latin1 COLLATE latin1_swedish_ci NULL DEFAULT NULL COMMENT 'json 设备数据',
  `payload_type` int(4) NULL DEFAULT NULL COMMENT '类型，当前默认为json',
  `select_field` varchar(255) CHARACTER SET latin1 COLLATE latin1_swedish_ci NULL DEFAULT NULL COMMENT 'Select 后面选择的字段，json格式',
  `condition_field` varchar(255) CHARACTER SET latin1 COLLATE latin1_swedish_ci NULL DEFAULT NULL COMMENT 'Where后面的条件语句',
  `condition_type` int(1) NULL DEFAULT NULL COMMENT '触发条件类型，1标识topic，2标识流向关系型数据局',
  `to_destination` varchar(64) CHARACTER SET latin1 COLLATE latin1_swedish_ci NULL DEFAULT NULL COMMENT '需要将触发事件后，信息发布流向的Topic',
  `database_url` varchar(255) CHARACTER SET latin1 COLLATE latin1_swedish_ci NULL DEFAULT NULL COMMENT '流向关系数据库',
  `created_time` datetime(0) NULL DEFAULT NULL COMMENT '默认为当前时间',
  `status` int(2) UNSIGNED NULL DEFAULT NULL COMMENT '0：规则未启动标识\r\n1：规则启动标识\r\n2：规则删除标识\r\n',
  `error_destination` varchar(64) CHARACTER SET latin1 COLLATE latin1_swedish_ci NULL DEFAULT NULL COMMENT '错误时，需要发布信息流向的Topic',
  `error_code` varchar(8) CHARACTER SET latin1 COLLATE latin1_swedish_ci NULL DEFAULT NULL COMMENT '错误',
  `error_message` varchar(255) CHARACTER SET latin1 COLLATE latin1_swedish_ci NULL DEFAULT NULL COMMENT '错误流向信息',
  `updated_time` datetime(0) NULL DEFAULT NULL COMMENT '编辑更新的时间',
  `broker_id` varchar(128) CHARACTER SET latin1 COLLATE latin1_swedish_ci NULL DEFAULT NULL COMMENT 'broker id',
  `user_id` varchar(64) CHARACTER SET latin1 COLLATE latin1_swedish_ci NULL DEFAULT NULL COMMENT '用户名称',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = latin1 COLLATE = latin1_swedish_ci ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;
