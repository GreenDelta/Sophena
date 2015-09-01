## Sophena basedata
This project contains the scripts for managing the base data of the Sophena
application.


## Climate data management
The climate data are prepared with the following steps:

1. The files that are provided by the DWD are converted into CSV files with a
   simple structure (see lib/dwd.js for reading the DWD data and
   scripts/dwd2csv.js for converting DWD data to CSV)
2. These files are then imported into an MySQL database (see the script
   scripts/data_pump.sql)
3. The lib/climatedb.js module provides a simple API for recieving the data
   from the database. It is also used by the data server (lib/server.js) which
   provides a user interface to browse the data.
4. The script scripts/station2json.js creates the average climate curves and
   stores them as json files.
5. Finally, these json files are then packaged into the base data package via
   the data packaging script (see below).  

## The climate data server
The server can be started via

    node server.js

 It requires that the MySQL database server is running (see the configuration
 in the server.js file). The client side code (HTML, JavaScript, CSS) of the
 server is stored in the lib/assets folder.

## Data packaging
