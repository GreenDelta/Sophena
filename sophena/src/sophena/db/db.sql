CREATE TABLE SEQUENCE (
	SEQ_NAME VARCHAR(255) NOT NULL,
	SEQ_COUNT BIGINT
);
INSERT INTO SEQUENCE(SEQ_NAME, SEQ_COUNT) VALUES('entity_seq', 0);

CREATE TABLE sophena_version (
	version SMALLINT
);
INSERT INTO openlca_version (version) VALUES (1);

CREATE TABLE tbl_projects (
    id VARCHAR(36),
    name VARCHAR(255),
    description CLOB(64 K),

    PRIMARY KEY (id)
)