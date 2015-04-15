var async = require('async'),
	climatedb = require('../lib/climatedb.js'),
	fs = require('fs'),
	mysql = require('mysql'),
	uuid = require('node-uuid');

var pool = mysql.createPool({
	connectionLimit: 10,
	host: 'localhost',
	user: 'root',
	database: 'climatedb',
	port: 3306
});

var outFolder = 'C:/Users/Besitzer/Projects/sophena/repo/basedata/climate_data/out/json/';

var count = -1,
	handled = 0;

climatedb.getStations(pool, function(err, stations) {
	if (err)
		throw err;
	count = stations.length;
	var fns = [];
	for (var i = 0; i < count; i++) {
		var station = stations[i];
		fns.push(stationHandler(station));
	}
	createStations(fns);
});

function createStations(handlers) {
	async.series(handlers, function(err, results) {
		if(err)
			throw err;
		console.log(results.length + ' stations handled');
		console.log('all done');
		pool.end();
	});
}

function stationHandler(station) {
	return function(cb) {
		var conf = {
			con: pool,
			station: station.id,
			startYear: 2005,
			endYear: 2014
		};
		climatedb.getAverageData(conf, function(err, data) {
			if (err)
				return cb(err);
			checkWriteStation(station, data)
			cb(null, true);
		});
	}
}

function checkWriteStation(station, data) {
	for(var i = 0; i < data.length; i++) {
		if(data[i] === -999) {
			console.log('station ' + station.id + ' is not complete');
			return;
		}
	}
	var uid = uuid.v4().toString();
	var obj = {
		id: uid,
		name: station.name,
		data: data
	};
	var fname = outFolder + uid + '.json'
	var json = JSON.stringify(obj, null, 4);
	fs.writeFile(fname, json, function(err) {
		if (err)
			throw err;
		console.log('wrote file ' + uid + '.json');
	});
}

