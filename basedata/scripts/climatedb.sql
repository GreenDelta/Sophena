
DROP DATABASE IF EXISTS climatedb;
CREATE DATABASE climatedb;
USE climatedb;

CREATE TABLE tbl_stations (

	id CHAR(5),
	name VARCHAR(255),

	PRIMARY KEY (id)
);


CREATE TABLE tbl_data (
	
	f_station CHAR(5),
	year SMALLINT, 
	hour SMALLINT, -- 1..8760
	temperature FLOAT,

	INDEX(f_station),
	INDEX(year)

);

CREATE TABLE tbl_average_curves (

	f_station CHAR(5),	 
	hour SMALLINT, -- 1..8760
	temperature FLOAT,

	INDEX(f_station),
	INDEX(year)

);
