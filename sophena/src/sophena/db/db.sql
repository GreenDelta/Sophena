CREATE TABLE sophena_version (
	version SMALLINT
);
INSERT INTO sophena_version (version) VALUES (1);

CREATE TABLE tbl_projects (
    id CHAR(36),
    name VARCHAR(255),
    description CLOB(64 K),
	project_duration SMALLINT,

    PRIMARY KEY (id)
);

CREATE TABLE tbl_fuels (
	id CHAR(36),
	name VARCHAR(255),
    description CLOB(64 K),

    PRIMARY KEY (id)
);

CREATE TABLE tbl_building_types (
   id CHAR(36),
   name VARCHAR(255),
   description CLOB(64 K),

   PRIMARY KEY (id)
);
INSERT INTO tbl_building_types (id, name) VALUES ('c39f721d-f804-4a73-a4bf-b218d832fb47', 'Einfamilienhaus');
INSERT INTO tbl_building_types (id, name) VALUES ('2b5f3318-f892-4993-a4dd-5f1898449059', 'Mehrfamilienhaus');
INSERT INTO tbl_building_types (id, name) VALUES ('a205c846-167c-4bca-9c5f-a45a7f1f3236', 'Wohnblock');
INSERT INTO tbl_building_types (id, name) VALUES ('ae3cdd55-f6ec-45cb-9d43-9cdd6f08072f', 'Schule');
INSERT INTO tbl_building_types (id, name) VALUES ('fd3cfff6-65e7-44e7-ad96-23178feed53e', 'Kindergarten');
INSERT INTO tbl_building_types (id, name) VALUES ('c440f34c-54a3-42c3-b0e2-5d88c9a2393a', 'Hallenbad');
INSERT INTO tbl_building_types (id, name) VALUES ('0496a809-3662-4621-8607-441adbc93e48', 'Freibad');
INSERT INTO tbl_building_types (id, name) VALUES ('4beba594-f4dc-431f-8960-1eb05558a0cf', 'Fertigungshalle');
INSERT INTO tbl_building_types (id, name) VALUES ('039a0027-e83d-47f8-b7bd-62b4842eb714', 'Krankenhaus');
INSERT INTO tbl_building_types (id, name) VALUES ('e8ff2144-504f-4248-b608-8168f4c97363', 'Bürogebäude');
INSERT INTO tbl_building_types (id, name) VALUES ('0850c918-0564-4a6e-9fc5-d69d4f5f0b81', 'Gaststätte');
INSERT INTO tbl_building_types (id, name) VALUES ('df4e6cbe-5a10-4d25-9110-13d41cb82d6d', 'Hotel');
INSERT INTO tbl_building_types (id, name) VALUES ('5de90f24-f00e-4361-9a7b-9c520b2bf8e3', 'Wellnesshotel');
INSERT INTO tbl_building_types (id, name) VALUES ('92d3ae90-4a80-4511-9536-5206014c94bc', 'Kirche');




CREATE TABLE tbl_consumers (

   id CHAR(36),
   name VARCHAR(255),
   description CLOB(64 K),



   PRIMARY KEY (id)
);