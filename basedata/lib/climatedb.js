var mysql = require('mysql'),
	hours = require('./hours.js'),
	EventEmitter = require('events').EventEmitter,
	util = require('util');

var Import = function(config, reader) {

	var self = this;

	EventEmitter.call(this);
	var pool = mysql.createPool(config);
	var hourIdx = new hours.Index();
	var handledStations = {};
	var openRequests = 0;
	var readerFinished = false;

	var recordBatch = [];

	function handleStation(station) {
		if (handledStations[station.id])
			return;
		handledStations[station.id] = true;
		var sql = 'INSERT INTO tbl_stations(id, name) VALUES (?, ?)';
		var values = [station.id, station.name];
		execSql(sql, values);
	}

	function handleRecord(rec) {
		var sql = "INSERT INTO tbl_data(f_station, year, hour, temperature) " +
			"VALUES (?) ";
		var hour = hourIdx.getHour(rec.hour);
		var values = [rec.station, rec.year, hour, rec.value];
		recordBatch.push(values);
		if (recordBatch.length > 1000) {
			execSql(sql, recordBatch);
			console.log("batch executed");
			recordBatch = [];
		}
	}

	function execSql(sql, values) {
		pool.getConnection(function(err, con) {
			if (err)
				return self.emit('error', err);
			openRequests++;
			con.query(sql, values, function(err, rows) {
				openRequests--;
				if (err)
					return self.emit('error', err);
				con.release();
				self.emit('inserted');
			});
		});
	}

	reader.on('station', handleStation);
	reader.on('record', handleRecord);
	reader.on('end', function() {
		readerFinished = true;
	});

	this.on('inserted', function() {
		if (!readerFinished || openRequests > 0)
			return;
		pool.end();
		self.emit('end');
	});

};
util.inherits(Import, EventEmitter);

function getAverageData(config, cb) {
	getDataTable(config, function(err, table) {
		if (err)
			return cb(err);
		var years = [];
		for (var year in table) {
			years.push(year);
		}
		var avgs = [];
		for (var h = 0; h < 8760; h++) {
			var sum = 0;
			var entries = 0;
			for (var y = 0; y < years.length; y++) {
				var year = years[y];
				var val = table[year][h];
				if (val !== -999) {
					sum += val;
					entries++;
				}
			}
			if (entries === 0) {
				console.log('no entries for h=', h);
				avgs[h] = -999;
			} else {
				avgs[h] = sum / entries;
			}
		}
		cb(null, avgs);
	});
}

/**
 * Returns all data available for the given station between the given
 * years (inclusively). The returned data have the following form:
 * {2005: [-2, -2.1, ..., 3.4], 2006: [4.1, ..., -2.9], ...}. For each year an
 * array with 8760 ordered entries for each annual hour is stored. If there is a
 * missing entry the array contains the error value -999 at this position.
 *
 * @param {Object} config - the configuration containing the following
 * attributes: {
 * 	con: "the database connection or connection pool",
 *  station: "ID of the station",
 *  startYear: "start of the interval",
 *  endYear: "end of the interval" }
 *
 * @param cb - the callback
 */
function getDataTable(config, cb) {
	var start = config.startYear;
	if (!start)
		start = 2005;
	var end = config.endYear;
	if (!end)
		end = 2014;
	var table = prepareDataTable(start, end);
	if (!config.con || !config.station)
		return table;
	var sql = 'SELECT year, hour, temperature AS val FROM tbl_data WHERE f_station=?';
	config.con.query(sql, [config.station], function(err, rows, fields) {
		if (err)
			return cb(err);
		fillDataTable(table, rows);
		cb(null, table);
	});
}


function prepareDataTable(startYear, endYear) {
	var table = {};
	for (var y = startYear; y <= endYear; y++) {
		var data = [];
		for (var h = 0; h < 8760; h++) {
			data[h] = -999;
		}
		table[y] = data;
	}
	return table;
}

function fillDataTable(table, rows) {
	for (var k = 0; k < rows.length; k++) {
		var row = rows[k];
		var data = table[row.year];
		if (!data)
			continue;
		var old = data[row.hour - 1];
		if (old != -999)
			console.log('duplicate entry: year=', row.year, 'hour=', row.hour);
		else
			data[row.hour - 1] = row.val;
	}
}

module.exports.getDataTable = getDataTable;
module.exports.getAverageData = getAverageData;
module.exports.Import = Import;