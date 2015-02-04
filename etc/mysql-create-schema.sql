DROP TABLE IF EXISTS ITEMS;
DROP TABLE IF EXISTS QUESTIONS;
DROP TABLE IF EXISTS RECOMMENDATIONS;
CREATE TABLE ITEMS (ITEM_ID integer not null auto_increment, VALUE integer, QUESTION_ID integer, RECOMMENDATION_ID integer, PRIMARY KEY (ITEM_ID));
CREATE TABLE QUESTIONS (QUESTION_ID integer not null auto_increment, QUESTION varchar(255), PRIMARY KEY (QUESTION_ID));
CREATE TABLE RECOMMENDATIONS (RECOMMENDATION_ID integer not null auto_increment, REQUEST_DATE date, USER_ID varchar(255), PRIMARY KEY (RECOMMENDATION_ID));
ALTER TABLE ITEMS ADD CONSTRAINT FK_TO_QUESTIONS FOREIGN KEY (QUESTION_ID) REFERENCES QUESTIONS (QUESTION_ID);
ALTER TABLE ITEMS ADD CONSTRAINT FK_TO_RECOMMENDATIONS FOREIGN KEY (RECOMMENDATION_ID) REFERENCES RECOMMENDATIONS (RECOMMENDATION_ID);

INSERT INTO QUESTIONS (QUESTION) VALUES('What is the total number of data sources going to be integrated ?');
INSERT INTO QUESTIONS (QUESTION) VALUES('How many concurrent queries at peak load time do you want to support ?');
INSERT INTO QUESTIONS (QUESTION) VALUES('How many client queries needs to be supported per second ?');
INSERT INTO QUESTIONS (QUESTION) VALUES('Average row count from each physical source ?');
INSERT INTO QUESTIONS (QUESTION) VALUES('What is the average row size in bytes that is returned from each source ?');
INSERT INTO QUESTIONS (QUESTION) VALUES('Average time in milliseconds by each source to return a result ?');
INSERT INTO QUESTIONS (QUESTION) VALUES('What is average row size that expected as result of federated query in bytes ?');
INSERT INTO QUESTIONS (QUESTION) VALUES('Average row count expected as result of federated query ?');
INSERT INTO QUESTIONS (QUESTION) VALUES('Average time taken in milliseconds to execute a client query in sample runs ?');
INSERT INTO QUESTIONS (QUESTION) VALUES('Does client query doing aggregations, sorts or view transformations doing sorts and aggregations ?');
