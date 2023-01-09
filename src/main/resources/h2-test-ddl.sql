drop table if exists oauth_user;

create table `oauth_user`
(
    `id`                   bigint not null auto_increment,
    `email`                varchar(255),
    `last_login_date_time` datetime(6) not null default current_timestamp,
    `sign_up_date`         date not null default current_date,
    `username`             varchar(255) not null,
    `github_id`            bigint,
    primary key (`id`)
);

create index idx_username on `oauth_user` (username);
