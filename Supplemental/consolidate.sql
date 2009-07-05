SET NAMES utf8;

create database RujelStatic default character set utf8;

create table RujelStatic.BASE_COURSE select * from BaseJournal.BASE_COURSE;
alter table RujelStatic.BASE_COURSE ADD PRIMARY KEY (`CR_ID`);

create table RujelStatic.PRIMITIVE_EDU_CYCLE select * from BaseJournal.PRIMITIVE_EDU_CYCLE;
alter table RujelStatic.PRIMITIVE_EDU_CYCLE ADD PRIMARY KEY (`C_ID`);

create table RujelStatic.ENT_INDEX select * from BaseJournal.ENT_INDEX;
alter table RujelStatic.ENT_INDEX ADD PRIMARY KEY (`E_ID`);

CREATE TABLE  RujelStatic.SETTINGS_BASE (
  `S_ID` mediumint NOT NULL,
  `KEY` varchar(28) NOT NULL,
  `TEXT_VALUE` varchar(255),
  `NUM_VALUE` int,
  PRIMARY KEY (`S_ID`)
);

CREATE TABLE  RujelStatic.SETTING_BY_COURSE (
  `SC_ID` mediumint NOT NULL,
  `SETTINGS` mediumint NOT NULL,
  `EDU_YEAR` smallint,
  `COURSE` mediumint,
  `CYCLE` mediumint,
  `GRADE` smallint,
  `EDU_GROUP` mediumint,
  `TEACHER` mediumint,
  `TEXT_VALUE` varchar(255),
  `NUM_VALUE` int,
  PRIMARY KEY (`SC_ID`)
);

create database RujelYear2007 default character set utf8;
create database RujelYear2008 default character set utf8;

create table RujelYear2007.COURSE_AUDIENCE select * from BaseJournal.COURSE_AUDIENCE
where COURSE in (select CR_ID from BaseJournal.BASE_COURSE where EDU_YEAR = 2007);
alter table RujelYear2007.COURSE_AUDIENCE ADD PRIMARY KEY (`COURSE`,`STUDENT`);

create table RujelYear2008.COURSE_AUDIENCE select * from BaseJournal.COURSE_AUDIENCE
where COURSE in (select CR_ID from BaseJournal.BASE_COURSE where EDU_YEAR = 2008);
alter table RujelYear2008.COURSE_AUDIENCE ADD PRIMARY KEY (`COURSE`,`STUDENT`);

create table RujelYear2007.BASE_TAB select * from BaseJournal.BASE_TAB
where COURSE in (select CR_ID from BaseJournal.BASE_COURSE where EDU_YEAR = 2007);
alter table RujelYear2007.BASE_TAB ADD PRIMARY KEY (`TAB_ID`);

create table RujelYear2008.BASE_TAB select * from BaseJournal.BASE_TAB
where COURSE in (select CR_ID from BaseJournal.BASE_COURSE where EDU_YEAR = 2008);
alter table RujelYear2008.BASE_TAB ADD PRIMARY KEY (`TAB_ID`);

create table RujelYear2007.BASE_LESSON select * from BaseJournal.BASE_LESSON
where COURSE in (select CR_ID from BaseJournal.BASE_COURSE where EDU_YEAR = 2007);
alter table RujelYear2007.BASE_LESSON ADD PRIMARY KEY (`L_ID`);

create table RujelYear2008.BASE_LESSON select * from BaseJournal.BASE_LESSON
where COURSE in (select CR_ID from BaseJournal.BASE_COURSE where EDU_YEAR = 2008);
alter table RujelYear2008.BASE_LESSON ADD PRIMARY KEY (`L_ID`);

create table RujelYear2007.BASE_NOTE select * from BaseJournal.BASE_NOTE
where LESSON in (select L_ID from RujelYear2007.BASE_LESSON);
alter table RujelYear2007.BASE_NOTE ADD PRIMARY KEY (`LESSON`,`STUDENT`);

create table RujelYear2008.BASE_NOTE select * from BaseJournal.BASE_NOTE
where LESSON in (select L_ID from RujelYear2008.BASE_LESSON);
alter table RujelYear2008.BASE_NOTE ADD PRIMARY KEY (`LESSON`,`STUDENT`);

create table RujelYear2007.WORK select * from Criterial.WORK
where COURSE in (select CR_ID from BaseJournal.BASE_COURSE where EDU_YEAR = 2007);
alter table RujelYear2007.WORK ADD PRIMARY KEY (`W_ID`);

create table RujelYear2008.WORK select * from Criterial.WORK
where COURSE in (select CR_ID from BaseJournal.BASE_COURSE where EDU_YEAR = 2008);
alter table RujelYear2008.WORK ADD PRIMARY KEY (`W_ID`);

create table RujelYear2007.TEXT_STORE select T_ID,ENTITY,STORED_TEXT from BaseJournal.TEXT_STORE
  where T_ID in (select TASK FROM RujelYear2007.`WORK` where TASK is not null);
alter table RujelYear2007.TEXT_STORE ADD PRIMARY KEY (`T_ID`);

create table RujelYear2008.TEXT_STORE select T_ID,ENTITY,STORED_TEXT from BaseJournal.TEXT_STORE
  where T_ID in (select TASK FROM RujelYear2008.`WORK` where TASK is not null);
alter table RujelYear2008.TEXT_STORE ADD PRIMARY KEY (`T_ID`);

create table RujelYear2007.CRITER_MASK select * from Criterial.CRITER_MASK
  where WORK in (select W_ID FROM RujelYear2007.`WORK`);
alter table RujelYear2007.CRITER_MASK ADD PRIMARY KEY (`WORK`,`CRITERION`);

create table RujelYear2008.CRITER_MASK select * from Criterial.CRITER_MASK
  where WORK in (select W_ID FROM RujelYear2008.`WORK`);
alter table RujelYear2008.CRITER_MASK ADD PRIMARY KEY (`WORK`,`CRITERION`);

create table RujelYear2007.MARK select * from Criterial.MARK
  where WORK in (select W_ID FROM RujelYear2007.`WORK`);
alter table RujelYear2007.MARK ADD PRIMARY KEY (`WORK`,`CRITER`,`STUDENT`);

create table RujelYear2008.MARK select * from Criterial.MARK
  where WORK in (select W_ID FROM RujelYear2008.`WORK`);
alter table RujelYear2008.MARK ADD PRIMARY KEY (`WORK`,`CRITER`,`STUDENT`);

create table RujelYear2007.WORK_NOTE select * from Criterial.WORK_NOTE
  where WORK in (select W_ID FROM RujelYear2007.`WORK`);
alter table RujelYear2007.WORK_NOTE ADD PRIMARY KEY (`WORK`,`STUDENT`);

create table RujelYear2008.WORK_NOTE select * from Criterial.WORK_NOTE
  where WORK in (select W_ID FROM RujelYear2008.`WORK`);
alter table RujelYear2008.WORK_NOTE ADD PRIMARY KEY (`WORK`,`STUDENT`);

insert into RujelStatic.SETTINGS_BASE (`S_ID`,`KEY`,`NUM_VALUE`) values (1,'CriteriaSet',1);

create table RujelStatic.BORDER select * from Criterial.BORDER;
alter table RujelStatic.BORDER ADD PRIMARY KEY (`B_ID`);

create table RujelStatic.BORDER_SET select * from Criterial.BORDER_SET;
alter table RujelStatic.BORDER_SET ADD PRIMARY KEY (`BS_ID`);

create table RujelStatic.CRIT_SET select * from Criterial.CRIT_SET;
alter table RujelStatic.CRIT_SET ADD PRIMARY KEY (`CS_ID`);

create table RujelStatic.CRITERION select * from Criterial.CRITERION;
alter table RujelStatic.CRITERION ADD PRIMARY KEY (`CR_ID`);

alter table Criterial.INDEXER ADD COLUMN `DEFAULT_VALUE` VARCHAR(255);
create table RujelStatic.INDEXER select * from Criterial.INDEXER;
alter table RujelStatic.INDEXER ADD PRIMARY KEY (`IND_ID`);

create table RujelStatic.INDEX_ROW select * from Criterial.INDEX_ROW;
alter table RujelStatic.INDEX_ROW ADD PRIMARY KEY (`IND`,`IDX`);

create table RujelStatic.ITOG_TYPE select ID_TYPE as T_ID, 
  TITLE, NAME, IN_YEAR_COUNT, ARCHIVE_SINCE as SORT from EduResults.PER_TYPE;
alter table RujelStatic.ITOG_TYPE ADD PRIMARY KEY (`T_ID`);
update RujelStatic.ITOG_TYPE set SORT = (10 - IN_YEAR_COUNT);

create table RujelStatic.ITOG select ID_PER as I_ID, PER_TYPE as TYPE, NUM, EDU_YEAR
  from EduResults.EDU_PERIOD where EDU_YEAR in (2007,2008);
alter table RujelStatic.ITOG ADD PRIMARY KEY (`I_ID`);

create table RujelStatic.ITOG_MARK select PERIOD as CONTAINER, EDU_CYCLE as CYCLE, STUDENT,
  MARK, VALUE, FLAGS from EduResults.ITOG_MARK;
alter table RujelStatic.ITOG_MARK ADD COLUMN `COMMENT` mediumint;
alter table RujelStatic.ITOG_MARK ADD PRIMARY KEY (`CONTAINER`,`CYCLE`,`STUDENT`);

create table RujelStatic.ITOG_COMMENT (
  C_ID mediumint NOT NULL,
  COMMENT text NOT NULL,
  PRIMARY KEY (`C_ID`)
);

insert into RujelStatic.SETTINGS_BASE (`S_ID`,`KEY`,`TEXT_VALUE`) 
	values (2,'ItogType','Общий');

insert into RujelStatic.SETTING_BY_COURSE (SC_ID, SETTINGS, COURSE, EDU_GROUP, EDU_YEAR, TEXT_VALUE)
select PKEY - 2 , 2, EDU_COURSE, EDU_GROUP, EDU_YEAR, 'Семестры' from EduResults.PERTYPE_USAGE where PERTYPE_ID = 2;

update RujelStatic.SETTING_BY_COURSE S,  RujelStatic.BASE_COURSE C
set S.EDU_YEAR = C.EDU_YEAR where S.COURSE = C.CR_ID;

create table RujelStatic.ITOG_TYPE_LIST (
  TL_ID mediumint NOT NULL,
  LIST_NAME varchar(28) NOT NULL,
  ITOG_TYPE smallint NOT NULL,
  PRIMARY KEY (`TL_ID`)
);

insert into RujelStatic.ITOG_TYPE_LIST values (1,'Общий',3), (2,'Общий',1), (3,'Семестры',2);
