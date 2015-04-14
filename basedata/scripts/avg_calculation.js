var climatedb = require('../lib/climatedb.js'),
	mysql = require('mysql');

var dbConfig = {
	connectionLimit: 10,
	host: 'localhost',
	user: 'root',
	database: 'climatedb',
	port: 3306
}

var pool = mysql.createPool(config);


calc.calculateStation('10004', 2005, 2014);


var AvgCalculator = function(config) {

	this.calculateStation = function(station, startYear, endYear) {
		var pool = mysql.createPool(config);
		var sql = 'SELECT year, hour, temperature AS val FROM tbl_data WHERE f_station=?';
		pool.query(sql, [station], function(err, rows, fields) {
			if (err)
				throw err;
			var curve = calculateAvgCurve(startYear, endYear, rows);
			pool.end();
		});
	};
};


var calculateAvgCurve = function(startYear, endYear, rows) {
	var idx = prepareAvgIndex(startYear, endYear);
	fillAvgIndex(idx, rows);
	var avgCurve = [];
	for (var h = 0; h < 8760; h++) {
		var sum = 0;
		var entries = 0;
		for (var y = startYear; y <= endYear; y++) {
			var val = idx[y][h];
			if (val) {
				sum += val;
				entries++;
			}
		}
		if (entries === 0) {
			console.log('no entries for h=', h);
			avgCurve[h] = -999;
		} else {
			avgCurve[h] = sum / entries;
			console.log(h, entries, 'entries', 'avg=', sum / entries);
		}
	}
}