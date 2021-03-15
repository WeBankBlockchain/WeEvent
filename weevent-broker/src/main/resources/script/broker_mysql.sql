drop table IF EXISTS t_account;
drop table IF EXISTS t_account_topic_auth;

create TABLE t_account (
	id INT(11) NOT NULL AUTO_INCREMENT COMMENT '主键id',
	user_name VARCHAR (255) COMMENT '用户名',
	password VARCHAR (255)  COMMENT '密码',
	create_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
	last_update TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
	delete_at BIGINT NOT NULL,
	PRIMARY KEY (id)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COMMENT='账户表';

create TABLE t_account_topic_auth (
	id INT(11) NOT NULL AUTO_INCREMENT COMMENT '主键id',
	user_name VARCHAR (200) COMMENT '用户名',
	topic_name VARCHAR (128) COMMENT '主题名称',
	permission INTEGER COMMENT '权限(0:订阅，发布,1:发布,2:订阅)',
	delete_at BIGINT NOT NULL DEFAULT 0 COMMENT '0 表示 未删除, 时间戳 表示 已经被删除',
	create_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
	last_update TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
	PRIMARY KEY (id)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COMMENT='账户主题权限表';

alter table t_account add CONSTRAINT userName UNIQUE (user_name);
alter table t_account_topic_auth add CONSTRAINT userNameTopicName UNIQUE (user_name, topic_name);