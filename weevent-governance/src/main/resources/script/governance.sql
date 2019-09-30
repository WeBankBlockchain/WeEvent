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

CREATE TABLE `t_historical_data` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'primary key',
  `create_date` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'create date',
  `last_update` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'update date',
  `topic_name` varchar(256) NOT NULL COMMENT 'topic name',
  `group_id` varchar(256) DEFAULT NULL COMMENT 'group id',
  `broker_number` int(11) NOT NULL COMMENT 'broker id',
  `event_id` varchar(256) DEFAULT NULL COMMENT 'event id',
  `broker_id` varchar(256) DEFAULT NULL COMMENT 'broker id',
  `user_id` varchar(256) DEFAULT NULL COMMENT 'user id',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='t_historical_data';
