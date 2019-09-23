CREATE TABLE t_account(
   `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'primary key',
  `create_date` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'create date',
  `last_update` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP comment 'update date',
  `is_delete` int(1) NOT NULL DEFAULT  0 COMMENT '0 means not deleted 1 means deleted',
  `email` varchar(256) NOT NULL COMMENT 'email',
  `username` varchar(256) NOT NULL COMMENT 'username',
  `password` varchar(256) NOT NULL COMMENT 'password`',
  PRIMARY KEY (`id`),
  UNIQUE KEY `username` (`username`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 comment 't_account';


CREATE TABLE t_broker (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'primary key',
  `create_date` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'create date',
  `last_update` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP comment 'update date',
  `is_delete` int(1) NOT NULL DEFAULT  0 COMMENT '0 means not deleted 1 means deleted',
  `user_id` int(11) NOT NULL  COMMENT 'user id',
  `name` varchar(256) NOT NULL COMMENT 'name',
  `broker_url` varchar(256) DEFAULT NULL COMMENT 'brokerEntity url',
  `webase_url` varchar(256) DEFAULT NULL COMMENT 'webase url',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 comment 't_broker';

CREATE TABLE  t_topic (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'primary key',
  `create_date` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'create date',
  `last_update` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP comment 'update date',
  `is_delete` int(1) NOT NULL DEFAULT  0 COMMENT '0 means not deleted 1 means deleted',
  `topic_name` varchar(256) NOT NULL COMMENT 'topic name',
  `creater` varchar(256) DEFAULT NULL COMMENT 'creater',
  `broker_id` int(11) NOT NULL COMMENT 'brokerEntity id',
  `description` varchar(256)  NULL  DEFAULT NULL COMMENT 'description',
   PRIMARY KEY (`id`),
   UNIQUE KEY `brokerIdTopicName` (`broker_id`,`topic_name`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 comment 't_topic';


CREATE TABLE t_permission (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'primary key',
  `create_date` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'create date',
  `last_update` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP comment 'update date',
  `broker_id` int(11) NOT NULL COMMENT 'broker_id',
  `user_id` int(11) NOT NULL COMMENT 'user_id',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 comment 't_permission';

CREATE TABLE `t_rule_engine` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'primary key',
  `create_date` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'create date',
  `last_update` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'update date',
  `rule_name` varchar(128) NOT  NULL   COMMENT 'rule name',
  `payload_type` int(4)  NULL DEFAULT NULL COMMENT '1 means JASON, 2 means binary',
  `payload` varchar(255)  NULL DEFAULT NULL COMMENT 'message payload',
  `broker_id` int(11) not NULL COMMENT 'broker id',
  `user_id` int(11) not NULL COMMENT 'user id',
  `broker_url` varchar(255) NULL DEFAULT NULL COMMENT 'broker url',
  `from_destination` varchar(64)  NULL DEFAULT NULL COMMENT  'message origin',
  `to_destination` varchar(64)  NULL DEFAULT NULL COMMENT  'message reached',
  `select_field` varchar(255) NULL DEFAULT NULL COMMENT 'selected field',
  `condition_field` varchar(255) NULL DEFAULT NULL COMMENT 'condition field',
  `condition_type` int(2) NULL DEFAULT NULL COMMENT 'condition type',
  `status` int(2)  NULL DEFAULT null COMMENT '0 means not started, 1 means running,2 means is deleted',
   PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COMMENT='t_rule_engine';

