create table brennon.user_logins (
	username varchar not null primary key,
	password varchar not null,
	enabled boolean not null
);

create table brennon.user_authorities (
	username varchar not null,
	authority varchar not null,
	constraint fk_authorities_users foreign key(username) references brennon.user_logins(username)
);

create unique index ix_auth_username on brennon.user_authorities (username, authority);

create table brennon.user_profiles (
	id varchar not null primary key,
    username varchar unique not null,
	first_name varchar not null,
    last_name varchar not null,
    constraint fk_profile_username foreign key(username) references brennon.user_logins(username)
);

-- Default users (for test purpose only)

insert into brennon.user_logins(username, password, enabled)
values
	('john@mail.com', '$2a$10$N7jzrXagIVXru/1KB02g7OkAVLJC2GqULUxN6nCYAOZntCsRIEk2e', true),
	('anna@mail.com', '$2a$10$N7jzrXagIVXru/1KB02g7OkAVLJC2GqULUxN6nCYAOZntCsRIEk2e', true);


insert into brennon.user_authorities(username, authority)
values
	('john@mail.com', 'admin'),
	('anna@mail.com', 'user');


insert into brennon.user_profiles(id, username, first_name, last_name)
values
	('1', 'john@mail.com', 'John', 'Smith'),
	('2', 'anna@mail.com', 'Anna', 'Kavinsky');