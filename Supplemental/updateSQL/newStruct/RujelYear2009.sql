CREATE DATABASE 'RujelYear2009' DEFAULT CHARACTER SET utf8;

CREATE TABLE AI_AUTOITOG (
  AI_ID smallint NOT NULL,
  LIST_NAME varchar(28) NOT NULL,
  ITOG_CONTANER smallint NOT NULL,
  FIRE_DATE date NOT NULL,
  FIRE_TIME time,
  CALCULATOR_NAME varchar(255),
  BORDER_SET smallint,
  AI_FLAGS tinyint NOT NULL,
  PRIMARY KEY (AI_ID)
);

CREATE TABLE AI_BONUS (
  B_ID mediumint NOT NULL,
  ADD_VALUE decimal(4,4) NOT NULL,
  TARGET_MARK varchar(5),
  BONUS_REASON varchar(255),
  PRIMARY KEY (B_ID)
);

CREATE TABLE AI_COURSE_TIMEOUT (
  CT_ID mediumint NOT NULL,
  ITOG_CONTAINER smallint NOT NULL,
  EDU_COURSE int,
  EDU_GROUP int,
  EDU_CYCLE mediumint,
  TEACHER_ID int,
  FIRE_DATE date NOT NULL,
  REASON_TEXT varchar(255),
  CTO_FLAGS tinyint NOT NULL,
  PRIMARY KEY (CT_ID)
);

CREATE TABLE AI_ITOG_RELATED (
  R_ID int NOT NULL,
  EDU_COURSE int NOT NULL,
  ITOG_CONTAINER smallint NOT NULL,
  REL_KEY int NOT NULL,
  PRIMARY KEY (R_ID)
);

CREATE TABLE AI_PROGNOSIS (
  ITOG_CONTAINER smallint NOT NULL,
  EDU_COURSE int NOT NULL,
  STUDENT_ID int NOT NULL,
  PR_MARK varchar(5),
  SUCCESS_VALUE decimal(5,4) NOT NULL,
  COMPLETE_RATE decimal(5,4) NOT NULL,
  FIRE_DATE date,
  BONUS_ID mediumint,
  PR_FLAGS tinyint NOT NULL,
  PRIMARY KEY (ITOG_CONTAINER,EDU_COURSE,STUDENT_ID)
);

CREATE TABLE AI_STUDENT_TIMEOUT (
  T_ID mediumint NOT NULL,
  ITOG_CONTAINER smallint NOT NULL,
  STUDENT_ID int NOT NULL,
  EDU_COURSE int,
  FIRE_DATE date NOT NULL,
  REASON_TEXT varchar(255),
  STO_FLAGS tinyint NOT NULL,
  PRIMARY KEY (T_ID)
);

CREATE TABLE BASE_AUDIENCE (
  EDU_COURSE smallint NOT NULL,
  STUDENT_ID int NOT NULL,
  PRIMARY KEY (EDU_COURSE,STUDENT_ID)
);

CREATE TABLE BASE_LESSON (
  L_ID int NOT NULL,
  EDU_COURSE smallint NOT NULL,
  NUMBER_ORDER smallint,
  DATE_PERFORMED date NOT NULL,
  LESSON_TITLE varchar(5),
  LESSON_THEME varchar(255),
  HOME_TASK int,
  PRIMARY KEY (L_ID)
);

CREATE TABLE BASE_NOTE (
  LESSON_ID int NOT NULL,
  STUDENT_ID int NOT NULL,
  NOTE_TEXT varchar(255),
  PRIMARY KEY (LESSON_ID,STUDENT_ID)
);

CREATE TABLE BASE_TAB (
  TAB_ID mediumint NOT NULL,
  EDU_COURSE smallint NOT NULL,
  LESSON_ENTITY smallint NOT NULL,
  FIRST_LESSON smallint NOT NULL,
  PRIMARY KEY (TAB_ID)
);

CREATE TABLE BASE_TEACHER_CHANGE (
  TC_ID SMALLINT NOT NULL,
  EDU_COURSE SMALLINT NOT NULL,
  UPTO_DATE DATE NOT NULL,
  TEACHER_ID MEDIUMINT,
  COMMENT_TEXT VARCHAR(255),
  PRIMARY KEY (TC_ID)
);

CREATE TABLE BASE_TEXT_STORE (
  T_ID int NOT NULL,
  FROM_ENTITY smallint NOT NULL,
  STORED_TEXT text NOT NULL,
  PRIMARY KEY (T_ID)
);

CREATE TABLE CR_MARK (
  WORK_ID mediumint NOT NULL,
  CRITER_NUM smallint NOT NULL,
  STUDENT_ID int NOT NULL,
  MARK_VALUE smallint,
  DATE_SET date NOT NULL,
  PRIMARY KEY (WORK_ID,CRITER_NUM,STUDENT_ID)
);

CREATE TABLE CR_MASK (
  WORK_ID mediumint NOT NULL,
  CRITER_NUM smallint NOT NULL,
  MARK_MAX smallint NOT NULL,
  CRITER_WEIGHT smallint,
  PRIMARY KEY (WORK_ID,CRITER_NUM)
);

CREATE TABLE CR_NOTE (
  WORK_ID int NOT NULL,
  STUDENT_ID int NOT NULL,
  NOTE_TEXT varchar(255),
  PRIMARY KEY (WORK_ID,STUDENT_ID)
);

CREATE TABLE CR_WORK (
  W_ID mediumint NOT NULL,
  EDU_COURSE smallint NOT NULL,
  NUMBER_ORDER smallint NOT NULL,
  ANNOUNCE_DATE date NOT NULL,
  DEADLINE_DATE date NOT NULL,
  WORK_WEIGHT decimal(4,2) NOT NULL,
  WORK_TYPE tinyint NOT NULL,
  WORK_TITLE varchar(5),
  WORK_THEME varchar(255),
  LOAD_TIME smallint NOT NULL,
  TASK_LINK int,
  PRIMARY KEY (W_ID)
);

CREATE TABLE CU_REASON (
  R_ID mediumint NOT NULL,
  TEACHER_ID mediumint,
  EDU_GROUP mediumint,
  DATE_BEGIN date NOT NULL,
  DATE_END date,
  REASON_TEXT varchar(255) NOT NULL,
  VERIFY_TEXT varchar(255),
  SCHOOL_NUM smallint NOT NULL,
  REASON_FLAGS tinyint NOT NULL,
  PRIMARY KEY (R_ID)
);

CREATE TABLE CU_REPRIMAND (
  R_ID mediumint NOT NULL,
  EDU_COURSE smallint NOT NULL,
  DATE_RAISED datetime NOT NULL,
  DATE_RELIEF datetime,
  CONTENT_TEXT varchar(255) NOT NULL,
  AUTHOR_NAME varchar(255) NOT NULL,
  STATUS_FLAG tinyint NOT NULL,
  PRIMARY KEY (R_ID)
);

CREATE TABLE CU_SUBSTITUTE (
  LESSON_ID int NOT NULL,
  TEACHER_ID mediumint NOT NULL,
  LESSON_DATE date NOT NULL,
  SUBS_FACTOR decimal(4,2) NOT NULL,
  REASON_ID mediumint NOT NULL,
  FROM_LESSON int,
  PRIMARY KEY (LESSON_ID,TEACHER_ID)
);

CREATE TABLE CU_VARIATION (
  V_ID int NOT NULL,
  EDU_COURSE smallint,
  VAR_DATE date NOT NULL,
  VAR_VALUE tinyint NOT NULL,
  REASON_ID mediumint NOT NULL,
  PRIMARY KEY (V_ID)
);

CREATE TABLE MA_MARK_ARCHIVE (
  A_ID int NOT NULL,
  OBJ_ENTITY smallint NOT NULL,
  KEY1_VALUE int NOT NULL,
  KEY2_VALUE int NOT NULL,
  KEY3_VALUE int NOT NULL,
  CHANGE_TIME timestamp NOT NULL,
  ARCH_DATA text NOT NULL,
  BY_USER varchar(255) NOT NULL,
  WOSID_CODE varchar(28) NOT NULL,
  PRIMARY KEY (A_ID)
);

CREATE TABLE MA_USED_ENTITY (
  ENT_ID smallint NOT NULL,
  KEY1_NAME varchar(28),
  KEY2_NAME varchar(28),
  KEY3_NAME varchar(28),
  TO_ARCHIVE varchar(28),
  ENTITY_NAME varchar(28) NOT NULL,
  PRIMARY KEY (ENT_ID)
);

CREATE TABLE PL_EDU_PERIOD (
  P_ID smallint NOT NULL,
  EDU_YEAR smallint NOT NULL,
  DATE_BEGIN date NOT NULL,
  DATE_END date NOT NULL,
  SHORT_TITLE varchar(9) NOT NULL,
  FULL_NAME varchar(28),
  PRIMARY KEY (P_ID)
);

CREATE TABLE PL_HOLIDAY (
  H_ID smallint NOT NULL,
  HD_TYPE smallint NOT NULL,
  DATE_BEGIN date NOT NULL,
  DATE_END date NOT NULL,
  LIST_NAME varchar(28),
  PRIMARY KEY (H_ID)
);

CREATE TABLE PL_PERIOD_LIST (
  PL_ID smallint NOT NULL,
  EDU_PERIOD smallint NOT NULL,
  LIST_NAME varchar(28) NOT NULL,
  PRIMARY KEY (PL_ID)
);

CREATE TABLE PL_PLAN_DETAIL (
  EDU_COURSE smallint NOT NULL,
  EDU_PERIOD smallint NOT NULL,
  TOTAL_HOURS smallint NOT NULL,
  WEEKLY_HOURS smallint NOT NULL,
  PRIMARY KEY (EDU_COURSE,EDU_PERIOD)
);

CREATE TABLE ST_DESCRIPTION (
  D_ID smallint NOT NULL,
  ENT_NAME varchar(28) NOT NULL,
  ST_DESCRIPTION varchar(28),
  STAT_FIELD varchar(28) NOT NULL,
  GR_FIELD1 varchar(28),
  GR_FIELD2 varchar(28),
  BORDER_SET smallint,
  PRIMARY KEY (D_ID)
);

CREATE TABLE ST_ENTRY (
  E_ID int NOT NULL,
  GROUPING_ID int NOT NULL,
  STAT_KEY varchar(28) NOT NULL,
  KEY_COUNT mediumint NOT NULL,
  PRIMARY KEY (E_ID)
);

CREATE TABLE ST_GROUPING (
  G_ID int NOT NULL,
  DESCRIPTION_ID smallint NOT NULL,
  GR_ID1 int,
  GR_ID2 int,
  GR_TOTAL mediumint NOT NULL,
  PRIMARY KEY (G_ID)
);

