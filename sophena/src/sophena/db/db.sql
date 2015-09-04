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


CREATE TABLE tbl_building_states (
	
	id CHAR(36),
	name VARCHAR(255),
	description CLOB(64 K),
	
	idx INTEGER,
	is_default BOOLEAN,
	building_type VARCHAR(50),
	heating_limit DOUBLE,
	water_fraction DOUBLE,
	load_hours INTEGER,

	PRIMARY KEY (id)
);


CREATE TABLE tbl_product_groups (

	id CHAR(36),
	name VARCHAR(255),
	description CLOB(64 K),

	product_type VARCHAR(50),
	duration INTEGER,
    repair DOUBLE,
    maintenance DOUBLE,
    operation DOUBLE, 

	PRIMARY KEY (id)
);


CREATE TABLE tbl_locations (

	id CHAR(36),

    name VARCHAR(255),
    street VARCHAR(255),
    zip_code VARCHAR(255),
    city VARCHAR(255),
    latitude DOUBLE,
    longitude DOUBLE,

    PRIMARY KEY (id)
);

CREATE TABLE tbl_cost_settings (

	id CHAR(36),
	f_project CHAR(36),

    funding DOUBLE,
    electricity_revenues DOUBLE,
    interest_rate DOUBLE,
    interest_rate_funding DOUBLE,
    investment_factor DOUBLE,
    operation_factor DOUBLE,
    hourly_wage DOUBLE,
    bio_fuel_factor DOUBLE,
    fossil_fuel_factor DOUBLE,
	electricity_price DOUBLE,
    electricity_factor DOUBLE,
    maintenance_factor DOUBLE,
    vat_rate DOUBLE,
    insurance_share DOUBLE,
    other_share DOUBLE,
    administration_share DOUBLE,

	PRIMARY KEY (id)
);


CREATE TABLE tbl_projects (

	id CHAR(36),
	name VARCHAR(255),
	description CLOB(64 K),

	is_variant BOOLEAN,
	f_project CHAR(36),
	project_duration INTEGER,
	f_cost_settings CHAR(36),
	f_weather_station CHAR(36),
	f_heat_net CHAR(36),

	PRIMARY KEY (id)
);


CREATE TABLE tbl_heat_nets (

	id CHAR(36),
	
	net_length DOUBLE,
    supply_temperature DOUBLE,
    return_temperature DOUBLE,
    simultaneity_factor DOUBLE,
    max_load DOUBLE,
    power_loss DOUBLE,
    with_interruption BOOLEAN,
    interruption_start VARCHAR(10),
    interruption_end VARCHAR(10),
    
    buffer_tank_volume DOUBLE,
    f_buffer_tank CHAR(36),
    investment DOUBLE,
    duration INTEGER,
    repair DOUBLE,
    maintenance DOUBLE,
    operation DOUBLE, 
    
    PRIMARY KEY (id)
);


CREATE TABLE tbl_heat_net_pipes (
	
	id CHAR(36),
	
	f_project CHAR(36),
	f_pipe CHAR(36),
	length DOUBLE,
	
	investment DOUBLE,
    duration INTEGER,
    repair DOUBLE,
    maintenance DOUBLE,
    operation DOUBLE, 
    
    PRIMARY KEY (id)
);


CREATE TABLE tbl_consumers (

	id CHAR(36),
	name VARCHAR(255),
	description CLOB(64 K),

	is_disabled BOOLEAN,
	demand_based BOOLEAN,
	heating_load DOUBLE,
	water_fraction DOUBLE,
	load_hours INTEGER,
	heating_limit DOUBLE,

	f_project CHAR(36),
	f_building_type CHAR(36),
	f_building_state CHAR(36),
	f_location CHAR(36),

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

	purchase_price DOUBLE,
	url VARCHAR(255),
	
	max_power DOUBLE,
	min_power DOUBLE,
	efficiency_rate DOUBLE,

	f_fuel CHAR(36),
	wood_amount_type VARCHAR(255),

    PRIMARY KEY (id)
);


CREATE TABLE tbl_pipes (

	id CHAR(36),
	name VARCHAR(255),
	description CLOB(64 K),

	purchase_price DOUBLE,
	url VARCHAR(255),

	u_value DOUBLE,
	diameter DOUBLE,

	PRIMARY KEY (id)
);


CREATE TABLE tbl_buffer_tanks (

	id CHAR(36),
	name VARCHAR(255),
	description CLOB(64 K),

	purchase_price DOUBLE,
	url VARCHAR(255),
	
	volume DOUBLE,	

	PRIMARY KEY (id)
);


CREATE TABLE tbl_producers (

	id CHAR(36),
    name VARCHAR(255),
    description CLOB(64 K),
    
    is_disabled BOOLEAN,
    rank INTEGER,
    producer_function VARCHAR(255),

	f_project CHAR(36),
    f_boiler VARCHAR(36),

    investment DOUBLE,
    duration INTEGER,
    repair DOUBLE,
    maintenance DOUBLE,
    operation DOUBLE,

    f_wood_fuel CHAR(36),
    water_content DOUBLE,
    price_per_unit DOUBLE,
    tax_rate DOUBLE,

    PRIMARY KEY (id)
);