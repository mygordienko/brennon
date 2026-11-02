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

create sequence if not exists tickets_id_seq
    start with 100
    increment by 1;

create table tickets (
	id bigint not null primary key default nextval('tickets_id_seq'),
	subject varchar not null,
	text varchar not null,
	status varchar not null,
	author_id varchar not null,
	assignee_id varchar,
	version int not null default 0,
	created timestamptz not null,
	updated timestamptz not null,
	last_attachment_id int not null default 0,
	last_reply_id int not null default 0,
	constraint fk_ticket_author foreign key(author_id) references user_profiles(id)
);

alter sequence tickets_id_seq owned by tickets.id;

create index idx_tickets_assignee_id      on tickets (assignee_id, id);
create index idx_tickets_assignee_updated on tickets (assignee_id, updated);
create index idx_tickets_assignee_status_updated on tickets (assignee_id, status, updated);
create index idx_tickets_author_id      on tickets (author_id, id);
create index idx_tickets_author_updated on tickets (author_id, updated);
create index idx_tickets_author_status_updated on tickets (author_id, status, updated);

-- TODO: replies are owned by ticket (no independent IDs)
create table ticket_replies (
	id int not null default 0,
	ticket_id bigint not null,
	text varchar not null,
	author_id varchar not null,
	created timestamptz not null,
	updated timestamptz not null,
	constraint fk_ticket_reply_ticket foreign key(ticket_id) references tickets(id),
	constraint fk_ticket_reply_author foreign key(author_id) references user_profiles(id),
	constraint uq_ticket_reply unique(id, ticket_id)
);

create index idx_ticket_replies_ticket_id on ticket_replies (ticket_id);

-- attachments are owned by ticket
create table ticket_attachments (
	id int not null,
	ticket_id bigint not null,
	reply_id int,
	file_id varchar not null,
	name varchar not null,
	author_id varchar not null,
	created timestamptz not null,
	constraint fk_ticket_attachment_ticket foreign key(ticket_id) references tickets(id),
	constraint fk_ticket_attachment_reply foreign key(reply_id, ticket_id) references ticket_replies(id, ticket_id),
	constraint fk_ticket_attachment_author foreign key(author_id) references user_profiles(id),
	constraint uq_ticket_attachment unique(id, ticket_id)
);

create index idx_ticket_attachments_ticket_id on ticket_attachments (ticket_id);

-- TODO: Default users (for test purpose only)

insert into user_logins(username, password, enabled)
values
	('john@mail.com', '$2a$10$N7jzrXagIVXru/1KB02g7OkAVLJC2GqULUxN6nCYAOZntCsRIEk2e', true),
	('anna@mail.com', '$2a$10$N7jzrXagIVXru/1KB02g7OkAVLJC2GqULUxN6nCYAOZntCsRIEk2e', true),
	('paul@mail.com', '$2a$10$N7jzrXagIVXru/1KB02g7OkAVLJC2GqULUxN6nCYAOZntCsRIEk2e', true);


insert into user_authorities(username, authority)
values
	('john@mail.com', 'admin:read'),
	('john@mail.com', 'admin:write'),
	('john@mail.com', 'support:read'),
	('john@mail.com', 'support:write'),
	('john@mail.com', 'user:read'),
	('john@mail.com', 'user:write'),
	('anna@mail.com', 'user:read'),
	('anna@mail.com', 'user:write'),
	('paul@mail.com', 'user:read'),
	('paul@mail.com', 'user:write');


insert into user_profiles(id, username, first_name, last_name)
values
	('1', 'john@mail.com', 'John', 'Smith'),
	('2', 'anna@mail.com', 'Anna', 'Kavinsky'),
	('3', 'paul@mail.com', 'Paul', 'Allen');