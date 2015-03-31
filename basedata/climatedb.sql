
DROP DATABASE IF EXISTS climatedb;
CREATE DATABASE climatedb;
USE climatedb;

CREATE TABLE tbl_data (
	
	f_station CHAR(5),
	year SMALLINT, 
	hour SMALLINT, -- 1..8760
	temperature FLOAT,

	INDEX(f_station),
	INDEX(year)

);
