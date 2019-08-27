create database weevent_governance default character set utf8 collate utf8_general_ci;
use weevent_governance;
-- ----------------------------
-- Table structure for `t_account`
-- ----------------------------
DROP TABLE IF EXISTS `t_account`;
CREATE TABLE `t_account` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'primary key',
  `email` varchar(256) NOT NULL COMMENT 'email',
  `username` varchar(256) NOT NULL COMMENT 'username',
  `password` varchar(256) NOT NULL COMMENT 'password`',
  `last_update` timestamp NOT NULL COMMENT 'update date',
  `is_delete` int(1) NOT NULL COMMENT '0 means not deleted 1 means deleted',
  PRIMARY KEY (`id`),
  UNIQUE KEY `username` (`username`),
  KEY `index_email` (`email`),
  KEY `index_username` (`username`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 comment 't_account';

-- ----------------------------
-- Records of t_account
-- ----------------------------
INSERT INTO `t_account` VALUES ('1', 'admin@xxx.com', 'admin', '$2a$10$4N8PIKjVrccCQGBhHYvWeuFjcEcElGI7znKQi9iTXTveeZ7r4dqSi', '2019-06-27 17:13:15', '0');
INSERT INTO `t_account` VALUES ('2', 'admin@xxx.com', 'admin1', '$2a$10$wLPTlfdE/PnzRrHRTAj/beR66kYnSmIzFCzksOG1GRKyxeNb5Ymzy', '2019-06-11 10:57:47', '0');
INSERT INTO `t_account` VALUES ('3', 'admin@xxx.com', 'admin2', '$2a$10$P2ydFQ68H9FVzzahVSTrX.MtJXzJ8EF1tbMc3BeAlhn4JOUOI3uUe', '2019-06-27 14:39:01', '0');
INSERT INTO `t_account` VALUES ('4', 'admin@xxx.com', 'admin3', '$2a$10$2EO3HrSAjXbYGFr4GuICke63LCq0TztXZZyecFROxwmT2fbAw7zqK', '2019-06-28 16:25:55', '0');


-- ----------------------------
-- Table structure for `t_broker`
-- ----------------------------
DROP TABLE IF EXISTS `t_broker`;
CREATE TABLE `t_broker` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'primary key',
  `user_id` int(11) NOT NULL  COMMENT 'user id',
  `name` varchar(256) NOT NULL COMMENT 'name',
  `last_update` timestamp NOT NULL COMMENT 'update date',
  `broker_url` varchar(256) DEFAULT NULL COMMENT 'broker url',
  `webase_url` varchar(256) DEFAULT NULL COMMENT 'webase url',
  `is_delete` int(1) NOT NULL COMMENT '0 means not deleted 1 means deleted',
  PRIMARY KEY (`id`),
  KEY `user_id` (`user_id`),
  KEY `index_name` (`name`),
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 comment 't_broker';

-- ----------------------------
-- Records of t_broker
-- ----------------------------
INSERT INTO `t_broker` VALUES ('1', '1', 'broker1', '2019-08-02 14:59:15', 'https://127.0.0.1:443/weevent', 'http://127.0.0.1:8072/WeBASE-Node-Manager', '0');
INSERT INTO `t_broker` VALUES ('10', '6', '228', '2019-08-02 15:03:24', 'http://127.0.0.1:8090/weevent', 'http://127.0.0.1:8072/webase-node-mgr', '0');
INSERT INTO `t_broker` VALUES ('13', '1', 'broker2', '2019-08-02 15:02:56', 'http://127.0.0.1:8090/weevent', 'http://127.0.0.1:8072/WeBASE-Node-Manager', '0');


-- ----------------------------
-- Table structure for `t_topic`
-- ----------------------------
DROP TABLE IF EXISTS `t_topic`;
CREATE TABLE `t_topic` (
  `topic_name` varchar(256) NOT NULL COMMENT 'primary key',
  `creater` varchar(256) DEFAULT NULL COMMENT 'creater',
  `broker_id` int(11) NOT NULL COMMENT 'broker id',
  `last_update` timestamp NOT NULL  COMMENT 'update date',
  `is_delete` int(1) NOT NULL COMMENT '0 means not deleted 1 means deleted',
  PRIMARY KEY (`broker_id`,`topic_name`),
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 comment 't_topic';

-- ----------------------------
-- Records of t_topic
-- ----------------------------
INSERT INTO `t_topic` VALUES ('com.weevent.rest', 'admin', '1', '2019-07-04 14:57:33', '0');
INSERT INTO `t_topic` VALUES ('com.weevent.jsonrpc', 'admin', '10', '2019-08-02 15:29:03', '0');
INSERT INTO `t_topic` VALUES ('com.weevent.mtqq', 'admin', '11', '2019-08-02 15:57:52', '0');
INSERT INTO `t_topic` VALUES ('com.weevent.stomp', 'admin', '12', '2019-08-02 15:33:17', '0');
