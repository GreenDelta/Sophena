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
		for(var y = startYear; y <= endYear; y++) {
			var val = idx[y][h];
			if(val) {
				sum += val;
				entries++;
			}
		}
		if(entries === 0) {
			console.log('no entries for h=', h);
			avgCurve[h] = -999;
		} else {
			avgCurve[h] = sum/entries;
			console.log(h, entries, 'entries', 'avg=', sum/entries);
		}
	}
}


function prepareAvgIndex(startYear, endYear) {
	var idx = {};
	for (var y = startYear; y <= endYear; y++) {
		var data = [];
		for (var h = 0; h < 8760; h++) {
			data[h] = null;
		}
		idx[y] = data;
	}
	return idx;
}

function fillAvgIndex(idx, rows) {
	for (var k = 0; k < rows.length; k++) {
		var row = rows[k];
		var data = idx[row.year];
		if (!data)
			continue;
		var old = data[row.hour - 1];
		if (old != null)
			console.log('duplicate entry: year=', row.year, 'hour=', row.hour);
		else
			data[row.hour - 1] = row.val;
	}
}


module.exports.Import = Import;
module.exports.AvgCalculator = AvgCalculator;