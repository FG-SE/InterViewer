# --- Created by Ebean DDL
# To stop Ebean DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table audio_file (
  path                      varchar(255) not null,
  object_name               varchar(255),
  constraint pk_audio_file primary key (path))
;

create table code (
  id                        varchar(40) not null,
  name                      varchar(255),
  constraint pk_code primary key (id))
;

create table dynamic_property (
  id                        varchar(40) not null,
  interview_id              varchar(40) not null,
  property_type_id          varchar(40),
  value                     varchar(255),
  constraint pk_dynamic_property primary key (id))
;

create table interview (
  id                        varchar(40) not null,
  project_id                varchar(40),
  name                      varchar(255),
  audio_path                varchar(255),
  constraint pk_interview primary key (id))
;

create table project (
  id                        varchar(40) not null,
  name                      varchar(255),
  constraint pk_project primary key (id))
;

create table property_type (
  id                        varchar(40) not null,
  name                      varchar(255),
  project_id                varchar(40),
  constraint pk_property_type primary key (id))
;

create table statement (
  id                        varchar(40) not null,
  description               varchar(255),
  time_id                   varchar(40),
  interview_id              varchar(40),
  constraint pk_statement primary key (id))
;

create table time (
  id                        varchar(40) not null,
  minutes                   integer,
  seconds                   integer,
  millis                    integer,
  constraint pk_time primary key (id))
;

create table user (
  id                        bigint not null,
  email                     varchar(255),
  alias                     varchar(255),
  password                  varchar(255),
  admin                     boolean,
  session_id                varchar(255),
  constraint pk_user primary key (id))
;


create table code_statement (
  code_id                        varchar(40) not null,
  statement_id                   varchar(40) not null,
  constraint pk_code_statement primary key (code_id, statement_id))
;

create table project_user (
  project_id                     varchar(40) not null,
  user_id                        bigint not null,
  constraint pk_project_user primary key (project_id, user_id))
;
create sequence audio_file_seq;

create sequence user_seq;

alter table dynamic_property add constraint fk_dynamic_property_interview_1 foreign key (interview_id) references interview (id) on delete restrict on update restrict;
create index ix_dynamic_property_interview_1 on dynamic_property (interview_id);
alter table dynamic_property add constraint fk_dynamic_property_propertyTy_2 foreign key (property_type_id) references property_type (id) on delete restrict on update restrict;
create index ix_dynamic_property_propertyTy_2 on dynamic_property (property_type_id);
alter table interview add constraint fk_interview_project_3 foreign key (project_id) references project (id) on delete restrict on update restrict;
create index ix_interview_project_3 on interview (project_id);
alter table interview add constraint fk_interview_audio_4 foreign key (audio_path) references audio_file (path) on delete restrict on update restrict;
create index ix_interview_audio_4 on interview (audio_path);
alter table property_type add constraint fk_property_type_project_5 foreign key (project_id) references project (id) on delete restrict on update restrict;
create index ix_property_type_project_5 on property_type (project_id);
alter table statement add constraint fk_statement_time_6 foreign key (time_id) references time (id) on delete restrict on update restrict;
create index ix_statement_time_6 on statement (time_id);
alter table statement add constraint fk_statement_interview_7 foreign key (interview_id) references interview (id) on delete restrict on update restrict;
create index ix_statement_interview_7 on statement (interview_id);



alter table code_statement add constraint fk_code_statement_code_01 foreign key (code_id) references code (id) on delete restrict on update restrict;

alter table code_statement add constraint fk_code_statement_statement_02 foreign key (statement_id) references statement (id) on delete restrict on update restrict;

alter table project_user add constraint fk_project_user_project_01 foreign key (project_id) references project (id) on delete restrict on update restrict;

alter table project_user add constraint fk_project_user_user_02 foreign key (user_id) references user (id) on delete restrict on update restrict;

# --- !Downs

SET REFERENTIAL_INTEGRITY FALSE;

drop table if exists audio_file;

drop table if exists code;

drop table if exists code_statement;

drop table if exists dynamic_property;

drop table if exists interview;

drop table if exists project;

drop table if exists project_user;

drop table if exists property_type;

drop table if exists statement;

drop table if exists time;

drop table if exists user;

SET REFERENTIAL_INTEGRITY TRUE;

drop sequence if exists audio_file_seq;

drop sequence if exists user_seq;

