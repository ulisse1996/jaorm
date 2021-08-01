CREATE TABLE USER_ENTITY ( USER_ID NUMERIC, USER_NAME VARCHAR(30), DEPARTMENT_ID NUMERIC);
ALTER TABLE USER_ENTITY ADD CONSTRAINT USER_PK PRIMARY KEY (USER_ID);
CREATE TABLE ROLE (ROLE_ID NUMERIC, ROLE_NAME VARCHAR(30));
ALTER TABLE ROLE ADD CONSTRAINT ROLE_PK PRIMARY KEY (ROLE_ID);
CREATE TABLE USER_ROLE (ROLE_ID NUMERIC, USER_ID NUMERIC);
ALTER TABLE USER_ROLE ADD CONSTRAINT USER_ROLE_PK PRIMARY KEY (ROLE_ID, USER_ID);
CREATE TABLE USER_SPECIFIC (USER_ID NUMERIC, SPECIFIC_ID NUMERIC);
ALTER TABLE USER_SPECIFIC ADD CONSTRAINT USER_SPECIFIC_PK PRIMARY KEY (SPECIFIC_ID, USER_ID);
CREATE TABLE CITY (CITY_ID NUMERIC, CITY_NAME VARCHAR(30));
ALTER TABLE CITY ADD CONSTRAINT CITY_PK PRIMARY KEY (CITY_ID);
CREATE TABLE STORE(STORE_ID NUMERIC, STORE_NAME VARCHAR(30), CITY_ID NUMERIC);
ALTER TABLE STORE ADD CONSTRAINT STORE_PK PRIMARY KEY (STORE_ID);
CREATE TABLE WELD (COL1 VARCHAR(30), COL2 VARCHAR(30));
ALTER TABLE WELD ADD CONSTRAINT WELD_PK PRIMARY KEY (COL1);
CREATE TABLE SPRING (COL1 VARCHAR(30), COL2 VARCHAR(30));
ALTER TABLE SPRING ADD CONSTRAINT SPRING_PK PRIMARY KEY (COL1);
CREATE TABLE AUTO_GEN (GEN_ID NUMERIC GENERATED BY DEFAULT AS IDENTITY(START WITH 1, INCREMENT BY 1) PRIMARY KEY, NAME VARCHAR(30));
CREATE TABLE CUSTOM_ACCESSOR(CUSTOM VARCHAR(30));
CREATE TABLE CASCADE_ENTITY (CASCADE_ID NUMERIC, CASCADE_NAME VARCHAR(30));
ALTER TABLE CASCADE_ENTITY ADD CONSTRAINT CASCADE_ENTITY_PK PRIMARY KEY (CASCADE_ID);
CREATE TABLE CASCADE_ENTITY_INNER (CASCADE_ID NUMERIC, CASCADE_INNER_NAME VARCHAR(30));
ALTER TABLE CASCADE_ENTITY_INNER ADD CONSTRAINT CASCADE_ENTITY_INNER_PK PRIMARY KEY (CASCADE_ID);
CREATE TABLE COMPOUND_ENTITY (COMPOUND_ID NUMERIC, CREATE_DATE DATE, CREATE_USER VARCHAR(30));
ALTER TABLE COMPOUND_ENTITY ADD CONSTRAINT COMPOUND_ENTITY_PK PRIMARY KEY (COMPOUND_ID);
INSERT INTO COMPOUND_ENTITY (COMPOUND_ID, CREATE_DATE, CREATE_USER) values (1, CURRENT_DATE, '1');
CREATE SEQUENCE MY_PROG_SEQUENCE START WITH 23;
CREATE TABLE MY_PROG (ID_COL INTEGER GENERATED BY DEFAULT AS IDENTITY(START WITH 1) PRIMARY KEY, PROGR INTEGER, MY_VALUE VARCHAR(30));
ALTER TABLE MY_PROG ALTER PROGR SET DEFAULT MY_PROG_SEQUENCE.nextVal
