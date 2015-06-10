CREATE TABLE sophena_version (
	version SMALLINT
);
INSERT INTO sophena_version (version) VALUES (1);


CREATE TABLE tbl_weather_stations (
	id CHAR(36),
	name VARCHAR(255),
	description CLOB(64 K),

	data BLOB (80 K),

	PRIMARY KEY (id)
);


CREATE TABLE tbl_units (
	id CHAR(36),
	name VARCHAR(255),
	description CLOB(64 K),

	quantity VARCHAR(255),
	is_reference_unit BOOLEAN,
	conversion_factor DOUBLE,

	PRIMARY KEY (id)
);


CREATE TABLE tbl_fuels (
	id CHAR(36),
	name VARCHAR(255),
	description CLOB(64 K),

	unit VARCHAR(255),
	calorific_value DOUBLE,
	density DOUBLE,
	is_wood BOOLEAN,

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


CREATE TABLE tbl_building_states (
	id CHAR(36),
	name VARCHAR(255),
	description CLOB(64 K),

	PRIMARY KEY (id)
);
INSERT INTO tbl_building_states (id, name) VALUES ('71828b10-1851-424d-833c-d2d1f74cefa5', 'Altbau');
INSERT INTO tbl_building_states (id, name) VALUES ('c71f03c2-fe3a-4a12-8fa4-6d837c735da7', 'Stand 60er Jahre');
INSERT INTO tbl_building_states (id, name) VALUES ('b5972d9b-8e0a-4374-b004-efb84f8ec0b9', 'Stand 70er Jahre');
INSERT INTO tbl_building_states (id, name) VALUES ('28a0b292-0066-4bf2-8691-a28527ea8ebf', 'Stand 80er Jahre');
INSERT INTO tbl_building_states (id, name) VALUES ('8e64c6cf-1ab3-4f0c-a041-4450fecc105c', 'Stand 90er Jahre');
INSERT INTO tbl_building_states (id, name) VALUES ('bc2ab8a9-6596-484b-aa11-978741c4c0e1', 'Stand 00er Jahre');
INSERT INTO tbl_building_states (id, name) VALUES ('bc22815b-1ffe-4f83-8e1b-3b11751a9323', 'ENEV');
INSERT INTO tbl_building_states (id, name) VALUES ('b3352b90-a9e3-4c97-91e0-626d73e7d84a', 'KfW 70');
INSERT INTO tbl_building_states (id, name) VALUES ('b28f6c18-3c9b-4ab8-b665-724d1cf1e7c6', 'KfW 40');
INSERT INTO tbl_building_states (id, name) VALUES ('bd8c640a-ced1-4a78-8078-b9c27b549e7e', 'Passivhaus');


CREATE TABLE tbl_cost_settings (

	id CHAR(36),

    is_global BOOLEAN,
    investment_factor DOUBLE,
    other_factor DOUBLE,
    hourly_wage DOUBLE,
    bio_fuel_factor DOUBLE,
    fossil_fuel_factor DOUBLE,
    electricity_factor DOUBLE,
    maintenance_factor DOUBLE,
    vat_rate DOUBLE,
    insurance_share DOUBLE,
    tax_share DOUBLE,


	PRIMARY KEY (id)
);

CREATE TABLE tbl_projects (
	id CHAR(36),
	name VARCHAR(255),
	description CLOB(64 K),

    net_length DOUBLE,
    supply_temperature DOUBLE,
    return_temperature DOUBLE,
    simultaneity_factor DOUBLE,
    buffer_tank_volume DOUBLE,
    power_loss DOUBLE,
    with_interruption BOOLEAN,
    interruption_start VARCHAR(10),
    interruption_end VARCHAR(10),

	is_variant BOOLEAN,
	project_duration INTEGER,
	f_project CHAR(36),
	f_weather_station CHAR(36),

	PRIMARY KEY (id)
);


CREATE TABLE tbl_consumers (

	id CHAR(36),
	name VARCHAR(255),
	description CLOB(64 K),

	demand_based BOOLEAN,
	heating_load DOUBLE,
	water_fraction DOUBLE,
	load_hours INTEGER,
	heating_limit DOUBLE,

	f_project CHAR(36),
	f_building_type CHAR(36),
	f_building_state CHAR(36),

	PRIMARY KEY (id)
);


CREATE TABLE tbl_fuel_consumptions (

	id CHAR(36),

	f_consumer CHAR(36),
	f_fuel CHAR(36),
	amount DOUBLE,
	utilisation_rate DOUBLE,
	wood_amount_type VARCHAR(255),
	water_content DOUBLE,

	PRIMARY KEY (id)
);


CREATE TABLE tbl_load_profiles (

	id CHAR(36),
	name VARCHAR(255),
	description CLOB(64 K),

    data BLOB (80 K),
    f_consumer CHAR(36),

    PRIMARY KEY (id)
);


CREATE TABLE tbl_boilers (

	id CHAR(36),
	name VARCHAR(255),
	description CLOB(64 K),

	tbl_purchase_price DOUBLE,
	tbl_url VARCHAR(255),
	tbl_max_power DOUBLE,
	tbl_min_power DOUBLE,
	efficiency_rate DOUBLE,

	f_fuel CHAR(36),
	wood_amount_type VARCHAR(255),

    PRIMARY KEY (id)
);


CREATE TABLE tbl_producers (

	id CHAR(36),
    name VARCHAR(255),
    description CLOB(64 K),
    rank INTEGER,
    producer_function VARCHAR(255),

	f_project CHAR(36),
    f_boiler VARCHAR(36),

    PRIMARY KEY (id)
);