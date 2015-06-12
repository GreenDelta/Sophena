var express = require('express'),
    mysql = require('mysql'),
    climatedb = require('./climatedb.js'),
    stats = require('./climatestats.js');

var app = express(),
    pool = mysql.createPool({
        connectionLimit: 10,
        host: 'localhost',
        user: 'root',
        database: 'climatedb',
        port: 3306
    });

app.use(express.static('assets'));

app.get('/stations', function(req, res) {
    climatedb.getStations(pool, function(err, stations) {
        if (err)
            throw err;
        res.send(stations);
    });
});

app.get('/station/:id', function(req, res) {
    var id = req.params['id'];
    var sql = 'SELECT id, name FROM tbl_stations WHERE id = ?';
    pool.query(sql, [id], function(err, rows, fields) {
        if (err)
            throw err;
        res.json(rows[0]);
    });
});

app.get('/data/:station', function(req, res) {
    var station = req.params['station'];
    var conf = {
        con: pool,
        station: station,
        startYear: 2005,
        endYear: 2014
    };
    climatedb.getDataTable(conf, function(err, table) {
        if (err)
            throw err;
        res.send(table);
    });
});

app.get('/avgdata/:station', function(req, res) {
    var station = req.params['station'];
    var conf = {
        con: pool,
        station: station,
        startYear: 2005,
        endYear: 2014
    };
    climatedb.getAverageData(conf, function(err, data) {
        if (err)
            throw err;
        res.send(data);
    });
});

app.get('/stats', function(req, res) {
	var conf = {
        con: pool,
        startYear: 2005,
        endYear: 2014
    };
	stats.calculate(conf, function(err, data) {
		if(err){
			throw err;
		}
		res.send(data);
	});
});

var server = app.listen(8080, function() {

    var host = server.address().address;
    var port = server.address().port;

    console.log('dwdat listening at %s', port);

});
