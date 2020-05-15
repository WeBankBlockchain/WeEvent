drop table IF EXISTS t_account;
drop table IF EXISTS t_broker;
drop table IF EXISTS t_permission;
drop table IF EXISTS t_rule_database;
drop table IF EXISTS t_rule_engine;
drop table IF EXISTS t_rule_engine_condition;
drop table IF EXISTS t_topic;
drop table IF EXISTS t_topic_historical;
drop table IF EXISTS t_timer_scheduler;

create TABLE t_account (
	id INTEGER generated BY DEFAULT AS identity,
	create_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
	last_update TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
	delete_at BIGINT NOT NULL,
	email VARCHAR (255),
	PASSWORD VARCHAR (255),
	username VARCHAR (255),
	PRIMARY KEY (id)
);
 create TABLE t_broker (
	id INTEGER generated BY DEFAULT AS identity,
	create_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
	last_update TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
	broker_url VARCHAR (64),
	delete_at BIGINT NOT NULL,
	NAME VARCHAR (255),
	user_id INTEGER CHECK (user_id >= 1),
	PRIMARY KEY (id)
);
 create TABLE t_permission (
	id INTEGER generated BY DEFAULT AS identity,
	create_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
	last_update TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
	broker_id INTEGER,
	user_id INTEGER,
	PRIMARY KEY (id)
);
 create TABLE t_rule_database (
	id INTEGER generated BY DEFAULT AS identity,
	create_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
	last_update TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
	broker_id INTEGER,
	database_type VARCHAR (1),
	database_url VARCHAR (255),
	database_ip VARCHAR (32),
	database_port VARCHAR (32),
	database_name VARCHAR (128),
	datasource_name VARCHAR (255),
	optional_parameter VARCHAR (256),
	PASSWORD VARCHAR (255),
	system_tag BOOLEAN,
	table_name VARCHAR (255),
	user_id INTEGER,
	username VARCHAR (255),
	PRIMARY KEY (id)
);
 create TABLE t_rule_engine (
	id INTEGER generated BY DEFAULT AS identity,
	create_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
	last_update TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
	broker_id INTEGER,
	cep_id VARCHAR (255),
	condition_field VARCHAR (4096),
	condition_field_json VARCHAR (4096),
	condition_type INTEGER,
	delete_at BIGINT NOT NULL,
	error_destination VARCHAR (255),
	from_destination VARCHAR (255),
	function_array VARCHAR (1024),
	group_id VARCHAR (255),
	payload VARCHAR (4096),
	rule_description VARCHAR (255),
	payload_type INTEGER,
	rule_database_id INTEGER,
	rule_name VARCHAR (128),
	select_field VARCHAR (4096),
	STATUS INTEGER,
	system_tag BOOLEAN,
	table_name VARCHAR (128),
	to_destination VARCHAR (255),
	user_id INTEGER,
	PRIMARY KEY (id)
);
 create TABLE t_topic (
	id INTEGER generated BY DEFAULT AS identity,
	create_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
	last_update TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
	broker_id INTEGER,
	creater VARCHAR (255),
	delete_at BIGINT NOT NULL,
	description VARCHAR (255),
	group_id VARCHAR (64),
	topic_name VARCHAR (128),
	PRIMARY KEY (id)
);
 create TABLE t_topic_historical (
	id INTEGER generated BY DEFAULT AS identity,
	create_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
	last_update TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
	brokerId INTEGER,
	eventId VARCHAR (64),
	groupId VARCHAR (64),
	topicName VARCHAR (128),
	PRIMARY KEY (id)
);

CREATE TABLE t_timer_scheduler (
	id INTEGER generated BY DEFAULT AS identity,
	create_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
	last_update TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
	broker_id INTEGER,
    user_id INTEGER,
    scheduler_name VARCHAR(128),
    rule_database_id INTEGER,
    period_params VARCHAR (64),
    parsing_sql VARCHAR (1024),
    PRIMARY KEY (id)
) ;


alter table t_broker add CONSTRAINT brokerUrlDeleteAt UNIQUE (broker_url, delete_at) ;
alter table t_rule_engine add CONSTRAINT ruleNameDeleteAt UNIQUE (rule_name, delete_at);
alter table t_topic add CONSTRAINT topicNameBrokerGroupDelete UNIQUE (topic_name,broker_id,group_id,delete_at);
alter table t_topic_historical add CONSTRAINT brokerIdGroupIdEventId UNIQUE (brokerId, groupId, eventId);