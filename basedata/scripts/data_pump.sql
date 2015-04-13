DROP DATABASE IF EXISTS climatedb;
CREATE DATABASE climatedb;
USE climatedb;

CREATE TABLE tbl_stations (

	id CHAR(5),
	name VARCHAR(255),

	PRIMARY KEY (id)

) Engine=MyISAM;


CREATE TABLE tbl_data (
	
	f_station CHAR(5),
	year SMALLINT, 
	hour SMALLINT, -- 1..8760
	temperature FLOAT

) Engine=MyISAM;


CREATE TABLE tbl_average_curves (

	f_station CHAR(5),	 
	hour SMALLINT, -- 1..8760
	temperature FLOAT,

	INDEX(f_station),
	INDEX(year)

) Engine=MyISAM;


LOAD DATA INFILE 'C:/Users/Besitzer/Projects/sophena/repo/basedata/climate_data/out/meta_out.csv' 
	INTO TABLE tbl_stations FIELDS TERMINATED BY ',';

LOAD DATA INFILE 'C:/Users/Besitzer/Projects/sophena/repo/basedata/climate_data/out/data_out.csv' 
	INTO TABLE tbl_data FIELDS TERMINATED BY ',';

ALTER TABLE tbl_data ADD INDEX (f_station);
ALTER TABLE tbl_data ADD INDEX (year);

