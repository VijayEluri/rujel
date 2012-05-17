-- Schema updates for Criterial model --

-- v1 (0.9.4) --

ALTER TABLE CR_WORK ADD INDEX (EDU_COURSE);
ALTER TABLE CR_MARK ADD INDEX (STUDENT_ID);

CREATE TABLE IF NOT EXISTS SCHEMA_VERSION (
  MODEL_NAME varchar(255),
  VERSION_NUMBER smallint unsigned NOT NULL,
  VERSION_TITLE varchar(255),
  INSTALL_DATE timestamp
);

INSERT INTO SCHEMA_VERSION (MODEL_NAME,VERSION_NUMBER,VERSION_TITLE)
  VALUES ('Criterial',1,'0.9.4');
