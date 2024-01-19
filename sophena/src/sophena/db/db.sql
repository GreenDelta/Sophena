CREATE TABLE sophena_version (
    version SMALLINT
);
INSERT INTO sophena_version (version) VALUES (2);


CREATE TABLE tbl_weather_stations (

    id          CHAR(36),
    name        VARCHAR(255),
    description CLOB(64 K),

    is_protected BOOLEAN,

    longitude   DOUBLE,
    latitude    DOUBLE,
    altitude    DOUBLE,
    data        BLOB (80 K),

    PRIMARY KEY (id)
);


CREATE TABLE tbl_fuels (

    id          CHAR(36),
    name        VARCHAR(255),
    description CLOB(64 K),

    is_protected BOOLEAN,

    unit                  VARCHAR(255),
    calorific_value       DOUBLE,
    density               DOUBLE,
    fuel_group            VARCHAR(50),
    co2_emissions         DOUBLE,
    primary_energy_factor DOUBLE,
    ash_content           DOUBLE,

    PRIMARY KEY (id)
);


CREATE TABLE tbl_building_states (

    id          CHAR(36),
    name        VARCHAR(255),
    description CLOB(64 K),

    is_protected BOOLEAN,

    idx                      INTEGER,
    is_default               BOOLEAN,
    building_type            VARCHAR(50),
    heating_limit            DOUBLE,
    antifreezing_temperature DOUBLE,
    water_fraction           DOUBLE,
    load_hours               INTEGER,

    PRIMARY KEY (id)
);


CREATE TABLE tbl_product_groups (

    id          CHAR(36),
    name        VARCHAR(255),
    description CLOB(64 K),

    is_protected BOOLEAN,

    idx          INTEGER,
    product_type VARCHAR(50),
    fuel_group   VARCHAR(50),
    duration     INTEGER,
    repair       DOUBLE,
    maintenance  DOUBLE,
    operation    DOUBLE,

    PRIMARY KEY (id)
);


CREATE TABLE tbl_locations (

    id CHAR(36),

    name      VARCHAR(255),
    street    VARCHAR(255),
    zip_code  VARCHAR(255),
    city      VARCHAR(255),
    latitude  DOUBLE,
    longitude DOUBLE,

    PRIMARY KEY (id)
);


CREATE TABLE tbl_time_intervals (

    id      CHAR(36),
    f_owner CHAR(36),

    start_time  VARCHAR(255),
    end_time    VARCHAR(255),
    description VARCHAR(255),

    PRIMARY KEY (id)
);


CREATE TABLE tbl_cost_settings (

    id          CHAR(36),
    f_project   CHAR(36),

    hourly_wage                 DOUBLE,
    electricity_price           DOUBLE,
    electricity_demand_share    DOUBLE,
    f_project_electricity_mix   CHAR(36),
    f_electricity_mix           CHAR(36),
    f_replaced_electricity_mix  CHAR(36),
    electricity_revenues        DOUBLE,
    heat_revenues               DOUBLE,

    funding DOUBLE,
    funding_biomass_boilers DOUBLE,
    funding_heat_net DOUBLE,
    funding_transfer_stations DOUBLE,
    connection_fees DOUBLE,

    interest_rate DOUBLE,
    interest_rate_funding DOUBLE,

    investment_factor DOUBLE,
    operation_factor DOUBLE,
    bio_fuel_factor DOUBLE,
    fossil_fuel_factor DOUBLE,
    electricity_factor DOUBLE,
    maintenance_factor DOUBLE,
    heat_revenues_factor DOUBLE,
    electricity_revenues_factor DOUBLE,

    insurance_share DOUBLE,
    other_share DOUBLE,
    administration_share DOUBLE,

    PRIMARY KEY (id)
);


CREATE TABLE tbl_annual_costs (
    f_project CHAR(36),
    label VARCHAR(255),
    cost_entry DOUBLE
);


CREATE TABLE tbl_projects (

    id                  CHAR(36),
    name                VARCHAR(255),
    description         CLOB(64 K),
    f_project_folder    CHAR(36),

    project_duration    INTEGER,
    f_cost_settings     CHAR(36),
    f_weather_station   CHAR(36),
    f_heat_net          CHAR(36),

    PRIMARY KEY (id)
);


CREATE TABLE tbl_project_folders (
    id              CHAR(36),
    name            VARCHAR(255),
    description     CLOB(64 K),

    PRIMARY KEY (id)
);


CREATE TABLE tbl_product_entries (

    id CHAR(36),

    f_project CHAR(36),
    f_product CHAR(36),

    price_per_piece DOUBLE,
    number_of_items DOUBLE,

    investment DOUBLE,
    duration INTEGER,
    repair DOUBLE,
    maintenance DOUBLE,
    operation DOUBLE,

    PRIMARY KEY (id)
);


CREATE TABLE tbl_heat_nets (

    id CHAR(36),

    net_length DOUBLE,
    supply_temperature DOUBLE,
    return_temperature DOUBLE,
    simultaneity_factor DOUBLE,
    smoothing_factor DOUBLE,
    max_load DOUBLE,
    power_loss DOUBLE,
    f_interruption CHAR(36),

    max_buffer_load_temperature DOUBLE,
    lower_buffer_load_temperature DOUBLE,
    buffer_lambda DOUBLE,

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

    f_heat_net CHAR(36),
    f_pipe CHAR(36),
    name VARCHAR(255),
    length DOUBLE,
    price_per_meter DOUBLE,

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
    floor_space DOUBLE,

    f_project CHAR(36),
    f_profile CHAR(36),
    f_building_state CHAR(36),
    f_location CHAR(36),

    f_transfer_station CHAR(36),
    transfer_station_investment DOUBLE,
    transfer_station_duration INTEGER,
    transfer_station_repair DOUBLE,
    transfer_station_maintenance DOUBLE,
    transfer_station_operation DOUBLE,

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
    dynamic_data BLOB (80 K),
    static_data BLOB (80 K),

    PRIMARY KEY (id)
);


CREATE TABLE tbl_producer_profiles (

    id CHAR(36),

    min_power BLOB (80 K),
    max_power BLOB (80 K),

    PRIMARY KEY (id)
);


CREATE TABLE tbl_products (

    id CHAR(36),
    name VARCHAR(255),
    description CLOB(64 K),

    is_protected BOOLEAN,

    purchase_price DOUBLE,
    url VARCHAR(255),
    product_type VARCHAR(50),
    f_product_group CHAR(36),
    f_manufacturer CHAR(36),

    f_project CHAR(36),

    PRIMARY KEY (id)
);


CREATE TABLE tbl_boilers (

    id CHAR(36),
    name VARCHAR(255),
    description CLOB(64 K),

    is_protected BOOLEAN,

    purchase_price DOUBLE,
    url VARCHAR(255),
    product_type VARCHAR(50),
    f_product_group CHAR(36),
    f_manufacturer CHAR(36),

    max_power DOUBLE,
    min_power DOUBLE,
    efficiency_rate DOUBLE,

    is_co_gen_plant BOOLEAN,
    max_power_electric DOUBLE,
    min_power_electric DOUBLE,
    efficiency_rate_electric DOUBLE,

    PRIMARY KEY (id)
);


CREATE TABLE tbl_pipes (

    id CHAR(36),
    name VARCHAR(255),
    description CLOB(64 K),

    is_protected BOOLEAN,

    purchase_price DOUBLE,
    url VARCHAR(255),
    product_type VARCHAR(50),
    f_product_group CHAR(36),
    f_manufacturer CHAR(36),

    u_value DOUBLE,
    diameter DOUBLE,
    material VARCHAR(255),
    pipe_type VARCHAR(255),
    inner_diameter DOUBLE,
    outer_diameter DOUBLE,
    total_diameter DOUBLE,
    delivery_type VARCHAR(255),
    max_temperature DOUBLE,
    max_pressure DOUBLE,
    PRIMARY KEY (id)
);


CREATE TABLE tbl_buffer_tanks (

    id CHAR(36),
    name VARCHAR(255),
    description CLOB(64 K),

    is_protected BOOLEAN,

    purchase_price DOUBLE,
    url VARCHAR(255),
    product_type VARCHAR(50),
    f_product_group CHAR(36),
    f_manufacturer CHAR(36),

    volume DOUBLE,
    diameter DOUBLE,
    height DOUBLE,
    insulation_thickness DOUBLE,

    PRIMARY KEY (id)
);


CREATE TABLE tbl_transfer_stations (

    id CHAR(36),
    name VARCHAR(255),
    description CLOB(64 K),

    is_protected BOOLEAN,

    purchase_price DOUBLE,
    url VARCHAR(255),
    product_type VARCHAR(50),
    f_product_group CHAR(36),
    f_manufacturer CHAR(36),

    building_type VARCHAR(255),
    output_capacity VARCHAR(255),
    station_type VARCHAR(255),
    material VARCHAR(255),
    water_heating VARCHAR(255),
    control VARCHAR(255),

    PRIMARY KEY (id)
);


CREATE TABLE tbl_flue_gas_cleaning (

    id CHAR(36),
    name VARCHAR(255),
    description CLOB(64 K),

    is_protected BOOLEAN,

    purchase_price DOUBLE,
    url VARCHAR(255),
    product_type VARCHAR(50),
    f_product_group CHAR(36),
    f_manufacturer CHAR(36),

    flue_gas_cleaning_type VARCHAR(255),
    max_volume_flow DOUBLE,
    fuel VARCHAR(255),
    max_producer_power DOUBLE,
    max_electricity_consumption DOUBLE,
    cleaning_method VARCHAR(255),
    cleaning_type VARCHAR(255),
    separation_efficiency DOUBLE,

    PRIMARY KEY (id)
);


CREATE TABLE tbl_flue_gas_cleaning_entries (

    id CHAR(36),
    f_project CHAR(36),
    f_flue_gas_cleaning CHAR(36),

    investment DOUBLE,
    duration INTEGER,
    repair DOUBLE,
    maintenance DOUBLE,
    operation DOUBLE
);


CREATE TABLE tbl_heat_recovery (

    id CHAR(36),
    name VARCHAR(255),
    description CLOB(64 K),

    is_protected BOOLEAN,

    purchase_price DOUBLE,
    url VARCHAR(255),
    product_type VARCHAR(50),
    f_product_group CHAR(36),
    f_manufacturer CHAR(36),

    power DOUBLE,
    heat_recovery_type VARCHAR(255),
    fuel VARCHAR(255),
    producer_power DOUBLE,

    PRIMARY KEY (id)
);


CREATE TABLE tbl_producers (

    id          CHAR(36),
    name        VARCHAR(255),
    description CLOB(64 K),

    is_disabled       BOOLEAN,
    rank              INTEGER,
    producer_function VARCHAR(255),
    utilisation_rate  DOUBLE,

    f_project                     CHAR(36),
    f_product_group               CHAR(36),
    f_boiler                      CHAR(36),
    f_profile                     CHAR(36),
    profile_max_power             DOUBLE,
    profile_max_power_electric    DOUBLE,

    investment  DOUBLE,
    duration    INTEGER,
    repair      DOUBLE,
    maintenance DOUBLE,
    operation   DOUBLE,

    f_fuel           CHAR(36),
    wood_amount_type VARCHAR(255),
    water_content    DOUBLE,
    price_per_unit   DOUBLE,
    ash_costs        DOUBLE,

    f_produced_electricity    CHAR(36),
    f_heat_recovery           CHAR(36),
    heat_recovery_investment  DOUBLE,
    heat_recovery_duration    INTEGER,
    heat_recovery_repair      DOUBLE,
    heat_recovery_maintenance DOUBLE,
    heat_recovery_operation   DOUBLE,

	f_solar_collector						CHAR(36), 						
	solar_collector_area 					DOUBLE,
	solar_collector_alignment 				DOUBLE,
	solar_collector_tilt 					DOUBLE,
	solar_collector_operating_mode 			VARCHAR(255),
	solar_collector_temperature_difference  DOUBLE,
	solar_collector_temperature_increase 	DOUBLE,

    PRIMARY KEY (id)
);

CREATE TABLE tbl_manufacturer (

    id            CHAR(36),
    name          VARCHAR(255),
    description   CLOB(64 K),
    logo          CLOB(1024 K),
    sponsor_order INTEGER,

    is_protected BOOLEAN,

    address VARCHAR(255),
    url VARCHAR(255),

    PRIMARY KEY (id)
);


CREATE TABLE tbl_solar_collectors (

    id CHAR(36),
    name VARCHAR(255),
    description CLOB(64 K),

    is_protected BOOLEAN,

    purchase_price DOUBLE,
    url VARCHAR(255),
    product_type VARCHAR(50),
    f_product_group CHAR(36),
    f_manufacturer CHAR(36),

    collector_area DOUBLE,
    efficiency_rate_radiation DOUBLE,
	correction_factor DOUBLE,
	heat_transfer_coefficient1 DOUBLE,
	heat_transfer_coefficient2 DOUBLE,
	heat_capacity DOUBLE,
	angle_incidence_EW_10 DOUBLE,
	angle_incidence_EW_20 DOUBLE,
	angle_incidence_EW_30 DOUBLE,
	angle_incidence_EW_40 DOUBLE,
	angle_incidence_EW_50 DOUBLE,
	angle_incidence_EW_60 DOUBLE,
	angle_incidence_EW_70 DOUBLE,
	angle_incidence_EW_80 DOUBLE,
	angle_incidence_EW_90 DOUBLE,
	angle_incidence_NS_10 DOUBLE,
	angle_incidence_NS_20 DOUBLE,
	angle_incidence_NS_30 DOUBLE,
	angle_incidence_NS_40 DOUBLE,
	angle_incidence_NS_50 DOUBLE,
	angle_incidence_NS_60 DOUBLE,
	angle_incidence_NS_70 DOUBLE,
	angle_incidence_NS_80 DOUBLE,
	angle_incidence_NS_90 DOUBLE,

    PRIMARY KEY (id)
);