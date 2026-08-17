create table user_logins (
	username varchar primary key,
	password varchar not null,
	enabled boolean not null
);

create table user_authorities (
	username varchar not null,
	authority varchar not null,
	constraint fk_authorities_users foreign key(username) references user_logins(username)
);

create unique index ix_auth_username on user_authorities (username, authority);

create table user_profiles (
	id varchar primary key,
    username varchar unique not null,
	first_name varchar not null,
    last_name varchar not null,
    constraint fk_profile_username foreign key(username) references user_logins(username)
);

create table tickets (
	id varchar not null primary key,
	subject varchar not null,
	text varchar not null,
	status integer not null,
	author_id varchar not null,
	created timestamptz not null,
	updated timestamptz not null,
	constraint fk_ticket_author foreign key(author_id) references user_profiles(id)
);

create table ticket_replies (
	id varchar not null primary key,
	ticket_id varchar not null,
	text varchar not null,
	author_id varchar not null,
	created timestamptz not null,
	updated timestamptz not null,
	constraint fk_ticket_reply_ticket foreign key(ticket_id) references tickets(id),
	constraint fk_ticket_reply_author foreign key(author_id) references user_profiles(id)
);

-- Default users (for test purpose only)

insert into user_logins(username, password, enabled)
values
	('john@mail.com', '$2a$10$N7jzrXagIVXru/1KB02g7OkAVLJC2GqULUxN6nCYAOZntCsRIEk2e', true),
	('anna@mail.com', '$2a$10$N7jzrXagIVXru/1KB02g7OkAVLJC2GqULUxN6nCYAOZntCsRIEk2e', true);


insert into user_authorities(username, authority)
values
	('john@mail.com', 'admin:read'),
	('john@mail.com', 'admin:write'),
	('john@mail.com', 'user:read'),
	('john@mail.com', 'user:write'),
	('anna@mail.com', 'user:read'),
	('anna@mail.com', 'user:write');


insert into user_profiles(id, username, first_name, last_name)
values
	('1', 'john@mail.com', 'John', 'Smith'),
	('2', 'anna@mail.com', 'Anna', 'Kavinsky');