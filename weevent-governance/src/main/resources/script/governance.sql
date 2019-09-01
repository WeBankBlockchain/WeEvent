CREATE TABLE t_account(
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'primary key',
  `email` varchar(256) NOT NULL COMMENT 'email',
  `username` varchar(256) NOT NULL COMMENT 'username',
  `password` varchar(256) NOT NULL COMMENT 'password`',
  `last_update` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP comment 'update date',
  `is_delete` int(1) NOT NULL DEFAULT  0 COMMENT '0 means not deleted 1 means deleted',
  PRIMARY KEY (`id`),
  UNIQUE KEY `username` (`username`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 comment 't_account';


CREATE TABLE t_broker (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'primary key',
  `user_id` int(11) NOT NULL  COMMENT 'user id',
  `name` varchar(256) NOT NULL COMMENT 'name',
  `last_update` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP comment 'update date',
  `broker_url` varchar(256) DEFAULT NULL COMMENT 'broker url',
  `webase_url` varchar(256) DEFAULT NULL COMMENT 'webase url',
  `is_delete` int(1) NOT NULL DEFAULT  0 COMMENT '0 means not deleted 1 means deleted',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 comment 't_broker';

CREATE TABLE  t_topic (
  `topic_name` varchar(256) NOT NULL COMMENT 'primary key',
  `creater` varchar(256) DEFAULT NULL COMMENT 'creater',
  `broker_id` int(11) NOT NULL COMMENT 'broker id',
  `last_update` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP comment 'update date',
  `is_delete` int(1) NOT NULL DEFAULT  0 COMMENT '0 means not deleted 1 means deleted',
  PRIMARY KEY (`broker_id`,`topic_name`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 comment 't_topic';


CREATE TABLE `t_permission` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'primary key',
   create_date timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'create date',
  `broker_id` int(11) NOT NULL COMMENT 'broker_id',
  `user_id` int(11) NOT NULL COMMENT 'user_id',
  `last_update` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP comment 'update date',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 comment 't_permission';



