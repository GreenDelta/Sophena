var Reader = require('../lib/dwd.js').Reader,
	Import = require('../lib/climatedb.js').Import;


var dataFolder = 'C:/Users/Besitzer/Projects/sophena/repo/basedata/climate_data/raw_data';

var config = {
	connectionLimit: 10,
	host: 'localhost',
	user: 'root',
	database: 'climatedb',
	port: 3306
};

var reader = new Reader();
var writer = new Import(config, reader);

reader.on('end-folder', function() {
	console.log('reader finished');
	reader.end();
});

var rows = 0;
writer.on('inserted', function() {
	rows++;
	if(rows % 100 === 0)
		console.log(rows, ' rows written');
});

writer.on('end', function() {
	console.log('all done');
});

reader.readFolder(dataFolder);
