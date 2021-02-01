drop table IF EXISTS t_account;

create TABLE t_account (
	id INTEGER generated BY DEFAULT AS identity,
	user_name VARCHAR (255),
	password VARCHAR (255),
	permission VARCHAR (255),
	create_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
	last_update TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
	delete_at BIGINT NOT NULL,
	PRIMARY KEY (id)
);