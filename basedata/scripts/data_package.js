var fs = require('fs'),
	archiver = require('archiver');

var file = '../data_package.sophena';

fs.unlink(file, function(err) {
	if(err) {
		console.log('failed to delete ' + file);
		throw err;
	}

	var output = fs.createWriteStream(file);
	var archive = archiver('zip');

	output.on('close', function() {
		console.log(archive.pointer() + ' total bytes');
		console.log('package written: ' + file);
	});

	archive.on('error', function(err) {
		throw err;
	});

	archive.pipe(output);
	archive.bulk([{
		expand: true,
		cwd: '../data/fuels',
		src: ['**'],
		dest: 'fuels'
	}]);
	archive.bulk([{
		expand: true,
		cwd: '../data/boilers',
		src: ['**'],
		dest: 'boilers'
	}]);
	archive.bulk([{
		expand: true,
		cwd: '../climate_data/out/json',
		src: ['**'],
		dest: 'weather_stations'
	}]);
	archive.finalize();


});