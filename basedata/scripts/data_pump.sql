LOAD DATA INFILE 'C:/Users/Besitzer/Projects/sophena/repo/basedata/climate_data/out/dat_out.csv' 
	INTO TABLE tbl_data FIELDS TERMINATED BY ',';

LOAD DATA INFILE 'C:/Users/Besitzer/Projects/sophena/repo/basedata/climate_data/out/meta_out.csv' 
	INTO TABLE tbl_stations FIELDS TERMINATED BY ',';