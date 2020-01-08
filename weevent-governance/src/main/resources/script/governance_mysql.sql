CREATE TABLE t_account(
  `id` INT(11) NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `email` VARCHAR(256) NOT NULL COMMENT '邮箱',
  `username` VARCHAR(64) NOT NULL COMMENT '用户名',
  `password` VARCHAR(256) NOT NULL COMMENT '密码`',
  `create_date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建日期',
  `last_update` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改日期',
  `delete_at` BIGINT(16) NOT NULL DEFAULT  0 COMMENT '0 表示 未删除, 时间戳 表示 已经被删除',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 comment '用户表';


CREATE TABLE t_broker (
  `id` INT(11) NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `user_id` INT(11) NOT NULL  COMMENT 'user主键id',
  `name` VARCHAR(256) NOT NULL COMMENT '名称',
  `broker_url` VARCHAR(256) DEFAULT NULL COMMENT 'broker url',
  `webase_url` VARCHAR(256) DEFAULT NULL COMMENT 'webase url',
  `create_date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建日期',
  `last_update` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改日期',
  `delete_at` BIGINT(16) NOT NULL DEFAULT  0 COMMENT '0 表示 未删除, 时间戳 表示 已经被删除',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 comment 'broker配置表';

CREATE TABLE  t_topic (
  `id` INT(11) NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `topic_name` VARCHAR(256) NOT NULL COMMENT '主题名称',
  `creater` VARCHAR(256) DEFAULT NULL COMMENT '创建人',
  `broker_id` INT(11) NOT NULL COMMENT 'broker主键id',
  `group_id` VARCHAR(64) DEFAULT NULL COMMENT '群组id',
  `description` VARCHAR(256)  NULL  DEFAULT NULL COMMENT '主题描述',
  `create_date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建日期',
  `last_update` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP comment '修改日期',
  `delete_at` BIGINT(16) NOT NULL DEFAULT  0 COMMENT '0 表示 未删除, 时间戳 表示 已经被删除',
   PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 comment '主题表';


CREATE TABLE t_permission (
  `id` INT(11) NOT NULL AUTO_INCREMENT COMMENT '主键 id',
  `broker_id` INT(11) NOT NULL COMMENT 'broker主键id',
  `user_id` INT(11) NOT NULL COMMENT 'user主键id',
  `create_date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建日期',
  `last_update` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改日期',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 comment 'broker授权表';

CREATE TABLE t_rule_engine (
  `id` INT(11) NOT NULL AUTO_INCREMENT COMMENT '主键 id',
  `rule_name` VARCHAR(128) NOT  NULL   COMMENT '规则名称',
  `payload_type` INT(4)  NULL DEFAULT NULL COMMENT '1表示JASON',
  `payload` VARCHAR(4096)  NULL DEFAULT NULL COMMENT '规则描述',
  `broker_id` INT(11) not NULL COMMENT 'broker主键id',
  `user_id` INT(11) not NULL COMMENT 'user主键id',
  `group_id` VARCHAR(64) not NULL COMMENT '群组id',
  `cep_id` VARCHAR(64) NULL COMMENT '规则id',
  `broker_url` VARCHAR(255) NULL DEFAULT NULL COMMENT 'broker url',
  `from_destination` VARCHAR(64)  NULL DEFAULT NULL COMMENT  '数据来源',
  `to_destination` VARCHAR(64)  NULL DEFAULT NULL COMMENT  '数据目的',
  `function_array` VARCHAR(1024)  NULL DEFAULT NULL COMMENT  '函数数组字符串',
  `condition_field` VARCHAR(4096)  NULL DEFAULT NULL COMMENT  '过滤条件',
  `condition_field_json` VARCHAR(4096)  NULL DEFAULT NULL COMMENT  '过滤条件Json',
  `select_field` VARCHAR(4096) NULL DEFAULT NULL COMMENT  '选择字段',
  `condition_type` INT(2) NULL DEFAULT NULL COMMENT '数据流转类型',
  `status` INT(2)  NULL DEFAULT null COMMENT '0 未启动, 1 运行,2 已经删除',
  `database_url` VARCHAR(255) NULL DEFAULT NULL COMMENT '数据库 url',
  `rule_database_id` INT(11) NULL DEFAULT NULL COMMENT '数据源 主键id',
  `error_destination` VARCHAR(255) NULL DEFAULT NULL COMMENT '失败流转目的地',
  `system_tag` VARCHAR(1) NOT NULL DEFAULT '1' COMMENT '1 系统内置 ,2 用户新增',
  `create_date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建日期',
  `last_update` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改日期',
  `delete_at` BIGINT(16) NOT NULL DEFAULT  0 COMMENT '0 表示 未删除, 时间戳 表示 已经被删除',
   PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COMMENT='规则引擎表';

CREATE TABLE t_rule_database (
  `id` INT(11) NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `datasource_name` VARCHAR(256) NOT NULL COMMENT '数据源名称',
  `database_url` VARCHAR(128) NOT NULL COMMENT '数据库url',
  `username` VARCHAR(16) NOT NULL COMMENT '数据库用户名',
  `password` VARCHAR(128) NOT NULL COMMENT '数据库密码',
  `table_name` VARCHAR(32) NOT NULL COMMENT '表格名称',
  `optional_parameter` VARCHAR(256) DEFAULT NULL COMMENT '数据库可选参数',
  `broker_id` VARCHAR(256) DEFAULT NULL COMMENT 'broker主键id',
  `user_id` VARCHAR(256) DEFAULT NULL COMMENT 'user主键id',
  `system_tag` VARCHAR(1) NOT NULL DEFAULT '1' COMMENT '1 系统内置 ,2 用户新增',
  `create_date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建日期',
  `last_update` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改日期',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='数据源配置表';

CREATE TABLE t_topic_historical (
   `id` INT(11) NOT NULL AUTO_INCREMENT COMMENT '主键id',
   `topicName` VARCHAR(128) NOT NULL COMMENT '主题名称',
   `groupId` VARCHAR(64) NOT NULL COMMENT '群组id',
   `eventId` VARCHAR(64)  NOT NULL COMMENT '事件id',
   `brokerId` VARCHAR(64) NOT  NULL COMMENT 'broker主键id',
   `create_date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建日期',
   `last_update` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改日期',
   PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='主题历史数据表';


ALTER TABLE t_broker ADD CONSTRAINT brokerUrlDeleteAt UNIQUE (broker_url, delete_at) ;
ALTER TABLE t_rule_engine ADD CONSTRAINT ruleNameDeleteAt UNIQUE (rule_name, delete_at);
ALTER TABLE t_topic ADD CONSTRAINT topicNameBrokerGroupDelete UNIQUE (topic_name,broker_id,group_id,delete_at);
ALTER TABLE t_topic_historical ADD CONSTRAINT brokerIdGroupIdEventId UNIQUE (brokerId, groupId, eventId);