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
			"VALUES(?, ?, ?, ?) ";
		var hour = hourIdx.getHour(rec.hour);
		var values = [rec.station, rec.year, hour, rec.value];		
		execSql(sql, values);
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
		if(!readerFinished || openRequests > 0)
			return;
		pool.end();
		self.emit('end');
	});

};
util.inherits(Import, EventEmitter);

module.exports.Import = Import;