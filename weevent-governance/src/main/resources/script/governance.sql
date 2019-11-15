CREATE TABLE t_account(
  `id` INT(11) NOT NULL AUTO_INCREMENT COMMENT 'primary key',
  `create_date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'create date',
  `last_update` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP comment 'update date',
  `is_delete` INT(1) NOT NULL DEFAULT  0 COMMENT '0 means not deleted 1 means deleted',
  `email` VARCHAR(256) NOT NULL COMMENT 'email',
  `username` VARCHAR(64) NOT NULL COMMENT 'username',
  `password` VARCHAR(256) NOT NULL COMMENT 'password`',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 comment 't_account';


CREATE TABLE t_broker (
  `id` INT(11) NOT NULL AUTO_INCREMENT COMMENT 'primary key',
  `create_date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'create date',
  `last_update` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP comment 'update date',
  `is_delete` INT(1) NOT NULL DEFAULT  0 COMMENT '0 means not deleted 1 means deleted',
  `is_config_rule` INT(1)  NULL DEFAULT  NULL COMMENT '1 means configured ,2 means not configured',
  `user_id` INT(11) NOT NULL  COMMENT 'user id',
  `name` VARCHAR(256) NOT NULL COMMENT 'name',
  `broker_url` VARCHAR(256) DEFAULT NULL COMMENT 'broker url',
  `webase_url` VARCHAR(256) DEFAULT NULL COMMENT 'webase url',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 comment 't_broker';

CREATE TABLE  t_topic (
  `id` INT(11) NOT NULL AUTO_INCREMENT COMMENT 'primary key',
  `create_date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'create date',
  `last_update` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP comment 'update date',
  `is_delete` INT(1) NOT NULL DEFAULT  0 COMMENT '0 means not deleted 1 means deleted',
  `topic_name` VARCHAR(256) NOT NULL COMMENT 'topic name',
  `creater` VARCHAR(256) DEFAULT NULL COMMENT 'creator',
  `broker_id` INT(11) NOT NULL COMMENT 'broker id',
  `group_id` VARCHAR(64) DEFAULT NULL COMMENT 'group id',
  `description` VARCHAR(256)  NULL  DEFAULT NULL COMMENT 'description',
   PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 comment 't_topic';


CREATE TABLE t_permission (
  `id` INT(11) NOT NULL AUTO_INCREMENT COMMENT 'primary key',
  `create_date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'create date',
  `last_update` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP comment 'update date',
  `broker_id` INT(11) NOT NULL COMMENT 'broker_id',
  `user_id` INT(11) NOT NULL COMMENT 'user_id',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 comment 't_permission';

CREATE TABLE t_rule_engine (
  `id` INT(11) NOT NULL AUTO_INCREMENT COMMENT 'primary key',
  `create_date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'create date',
  `last_update` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'update date',
  `rule_name` VARCHAR(128) NOT  NULL   COMMENT 'rule name',
  `payload_type` INT(4)  NULL DEFAULT NULL COMMENT '1 means JASON, 2 means binary',
  `payload` VARCHAR(4096)  NULL DEFAULT NULL COMMENT 'message payload',
  `broker_id` INT(11) not NULL COMMENT 'broker id',
  `user_id` INT(11) not NULL COMMENT 'user id',
  `group_id` VARCHAR(64) not NULL COMMENT 'group id',
  `cep_id` VARCHAR(64) NULL COMMENT 'complex event processing id',
  `broker_url` VARCHAR(255) NULL DEFAULT NULL COMMENT 'broker url',
  `from_destination` VARCHAR(64)  NULL DEFAULT NULL COMMENT  'message origin',
  `to_destination` VARCHAR(64)  NULL DEFAULT NULL COMMENT  'message reached',
  `select_field` VARCHAR(4096) NULL DEFAULT NULL COMMENT 'selected field',
  `condition_field` VARCHAR(4096) NULL DEFAULT NULL COMMENT 'condition field',
  `condition_type` INT(2) NULL DEFAULT NULL COMMENT 'condition type',
  `status` INT(2)  NULL DEFAULT null COMMENT '0 means not started, 1 means running,2 means is deleted',
  `database_url` VARCHAR(255) NULL DEFAULT NULL COMMENT 'database url',
  `rule_database_id` INT(11) NULL DEFAULT NULL COMMENT 'rule database id',
  `error_destination` VARCHAR(255) NULL DEFAULT NULL COMMENT 'error destination',
  `error_message` VARCHAR(255) NULL DEFAULT NULL COMMENT 'error message',
  `system_tag` VARCHAR(1) NOT NULL DEFAULT '2' COMMENT '1  means system,2 means user add',
   PRIMARY KEY (`id`),
   UNIQUE KEY ruleName(rule_name)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COMMENT='t_rule_engine';

CREATE TABLE t_rule_database (
  `id` INT(11) NOT NULL AUTO_INCREMENT COMMENT 'primary key',
  `create_date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'create date',
  `last_update` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'update date',
  `database_url` VARCHAR(256) NOT NULL COMMENT 'database url',
  `database_name` VARCHAR(128) NOT NULL COMMENT 'database name',
  `table_name` VARCHAR(128) NOT NULL COMMENT 'table name',
  `broker_id` VARCHAR(256) DEFAULT NULL COMMENT 'broker id',
  `user_id` VARCHAR(256) DEFAULT NULL COMMENT 'user id',
  `is_visible` VARCHAR(1) NOT NULL DEFAULT '1' COMMENT '1 visible ,2 invisible',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='t_rule_database';

CREATE TABLE t_rule_engine_condition (
  `id` INT(11) NOT NULL AUTO_INCREMENT COMMENT 'primary key',
  `create_date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'create date',
  `last_update` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'update date',
  `rule_id` INT(64) DEFAULT NULL COMMENT 'rule id',
  `sql_condition_json` VARCHAR(512) DEFAULT NULL COMMENT 'sql condition json',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='t_rule_engine_condition';


CREATE TABLE t_topic_historical (
   `id` INT(11) NOT NULL AUTO_INCREMENT COMMENT 'primary key',
   `create_date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'create date',
   `last_update` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'update date',
   `topicName` VARCHAR(128) NOT NULL COMMENT 'topic name',
   `groupId` VARCHAR(64) NOT NULL COMMENT 'group id',
   `eventId` VARCHAR(64)  NOT NULL COMMENT 'event id',
   `brokerId` VARCHAR(64) NOT  NULL COMMENT 'broker id',
   PRIMARY KEY (`id`),
   UNIQUE KEY brokerIdGroupIdEventId(brokerId,groupId,eventId)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='t_topic_historical';
