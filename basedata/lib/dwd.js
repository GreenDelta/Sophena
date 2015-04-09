// Provides methods for reading prepared DWD files.

var fs = require('fs'),
	byline = require('byline'),
	S = require('string');

function readMetaDataFile(filePath, stationCb, endCb) {

	var fsStream = fs.createReadStream(filePath),
		stream = byline.createStream(fsStream);

	stream.on('readable', function() {
		if (!stationCb)
			return;
		var buffer;
		while ((buffer = stream.read()) !== null) {
			var line = S(buffer.toString()).trim();
			if (line.startsWith('Station= ')) {
				var station = parseStationInfo(line);
				stationCb(station)
			}
		};
	});

	stream.on('end', function() {
		if (!endCb)
			return;
		endCb();
	});
}

function parseStationInfo(line) {
	var info = line.between('Station= ', ',').trim().collapseWhitespace().s;
	var i = info.indexOf(' ');
	var id = info.substring(0, i);
	var name = info.substring(i + 1);
	return {
		id: id,
		name: name
	};
}

var metaFile = 'C:/Users/Besitzer/Projects/sophena/repo/basedata/climate_data/raw_data/sy_ds_abgabe011_15_1bestand.txt';

readMetaDataFile(metaFile, function(station) {
	console.log(station);
}, function() {
	console.log('all done');
});