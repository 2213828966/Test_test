create database if not exists chat_room charset utf8;

use chat_room;

drop table if exists user;
create table user(
    userId int primary key auto_increment,
    username varchar(20) unique,
    password varchar(20)
);

insert into user values(null,'zhangsan','123');
insert into user values(null,'lisi','123');


drop table if exists friend;
create table friend(
    userId int,
    friendId int
);

insert into friend values(1,2);
insert into friend values(2,1);


-- 创建会话表
drop table if exists message_session;
create table message_session(
    sessionId int primary key auto_increment,
    -- 上次访问时间
    lastTime datetime
);
insert into message_session values(1,'2000-05-01 00:00:00');


-- 创建会话和用户的关联表
drop table if exists message_session_user;
create table message_session_user (
    sessionId int,
    userId int
);
-- 1 号会话有张三和李四
insert into message_session_user values(1,1),(1,2);
-- 2 号会话有张三和王五
--insert into message_session_user values(2,1),(2,3);

-- 创建消息表
drop table if exists message;
create table message(
    messageId int primary key auto_increment,
    fromId int, -- 消息是哪个用户发来的
    sessionId int, -- 消息属于哪个会话
    content varchar(2048), -- 消息正文
    postTime datetime -- 消息发送时间
);

-- 构造几个测试数据 张三和李四
insert into message values(1,1,1,'今晚吃啥','2000-05-01 00:00:00');
insert into message values(2,2,1,'随便','2000-05-01 17:01:00');
insert into message values(3,1,1,'那吃面？','2000-05-01 17:02:00');
insert into message values(4,2,1,'不想吃？','2000-05-01 17:03:00');
insert into message values(5,1,1,'那你想吃啥？','2000-05-01 17:04:00');
insert into message values(6,2,1,'随便','2000-05-01 17:05:00');

insert into message values (11, 1, 1, '那吃米饭炒菜?', '2000-05-01 17:06:00');
insert into message values (8, 2, 1, '不想吃', '2000-05-01 17:07:00');
insert into message values (9, 1, 1, '那你想吃啥?', '2000-05-01 17:08:00');
insert into message values (10, 2, 1, '随便', '2000-05-01 17:09:00');

-- 张三和王五
--insert into message values(7,1,2,'晚上一起？','2000-05-02 12:00:00');




