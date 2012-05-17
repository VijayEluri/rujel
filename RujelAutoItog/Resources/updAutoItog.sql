-- Schema updates for AutoItog model --

-- v1 (0.9.4) --

ALTER TABLE AI_ITOG_RELATED ADD INDEX (EDU_COURSE);
ALTER TABLE AI_STUDENT_TIMEOUT ADD INDEX (EDU_COURSE);
ALTER TABLE AI_STUDENT_TIMEOUT ADD INDEX (ITOG_CONTAINER);

CREATE TABLE IF NOT EXISTS SCHEMA_VERSION (
  MODEL_NAME varchar(255),
  VERSION_NUMBER smallint unsigned NOT NULL,
  VERSION_TITLE varchar(255),
  INSTALL_DATE timestamp
);

INSERT INTO SCHEMA_VERSION (MODEL_NAME,VERSION_NUMBER,VERSION_TITLE)
  VALUES ('AutoItog',1,'0.9.4');
