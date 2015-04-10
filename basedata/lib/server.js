var express = require('express'),
	mysql = require('mysql');

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
	var sql = 'SELECT id, name FROM tbl_stations';
	pool.query(sql, function(err, rows, fields) {
		if (err)
			throw err;
		res.json(rows);
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
	var sql = 'SELECT year, hour, temperature as val from tbl_data where f_station=?';
	pool.query(sql, [station], function(err, rows, fields) {
		if(err)
			throw err;
		res.json(rows);
	});
});

var server = app.listen(8080, function() {

	var host = server.address().address;
	var port = server.address().port;

	console.log('dwdat listening at %s', port);

});