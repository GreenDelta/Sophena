// Provides methods for reading prepared DWD files.

var fs = require('fs'),
	byline = require('byline'),
	S = require('string'),
	EventEmitter = require('events').EventEmitter,
	util = require('util');

var Reader = function() {
	EventEmitter.call(this);
};
util.inherits(Reader, EventEmitter);

Reader.prototype.readFolder = function(folderPath) {
	var self = this;
	this.emit('start-folder', folderPath);
	fs.readdir(folderPath, function(err, fileNames) {
		if (err)
			return self.emit('error', err);

		var metaFiles = filterFiles(fileNames, 'bestand.txt');
		var dataFiles = filterFiles(fileNames, 'elc.txt');
		var count = metaFiles.length + dataFiles.length;
		if (count === 0)
			return self.emit('end-folder', folderPath);

		var handledCount = 0;

		self.on('end-file', function(filePath) {
			handledCount++;
			if (handledCount === count) {
				self.emit('end-folder', folderPath);
			}
		});

		metaFiles.forEach(function(metaFile) {
			self.readMetaFile(folderPath + '/' + metaFile);
		});
		dataFiles.forEach(function(dataFile) {
			self.readDataFile(folderPath + '/' + dataFile);
		});
	});
};

Reader.prototype.readMetaFile = function(filePath) {
	var self = this;
	this.emit('start-file', filePath);
	console.log('read meta data file:', filePath);

	var fsStream = fs.createReadStream(filePath),
		stream = byline.createStream(fsStream);

	stream.on('readable', function() {
		var buffer;
		while ((buffer = stream.read()) !== null) {
			var line = S(buffer.toString()).trim();
			if (line.startsWith('Station= ')) {
				var station = parseStationInfo(line);
				self.emit('station', station);
			}
		}
	});

	stream.on('end', function() {
		console.log('finished meta file', filePath);
		self.emit('end-file', filePath);
	});

	stream.on('error', function(err) {
		self.emit('error', e);
	});
};

Reader.prototype.readDataFile = function(filePath) {
	var self = this;
	this.emit('start-file', filePath);
	console.log('read data file:', filePath);

	var fsStream = fs.createReadStream(filePath),
		stream = byline.createStream(fsStream);

	stream.on('readable', function() {
		var buffer;
		while ((buffer = stream.read()) !== null) {
			var line = buffer.toString();
			var record = parseDataLine(line);
			if(record)
				self.emit('record', record);			
		}
	});

	stream.on('end', function() {
		console.log('finished data file', filePath);
		self.emit('end-file', filePath);
	});

	stream.on('error', function(err) {
		self.emit('error', e);
	});	
};

function filterFiles(fileNames, suffix) {
	var files = [];
	for (var i = 0; i < fileNames.length; i++) {
		var name = S(fileNames[i]);
		if (name.endsWith(suffix))
			files.push(name.s);
	}
	return files;
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

function parseDataLine(line) {
	var feed = line.trim(),
        station = feed.substring(0, 5).trim(),
        year = parseInt(feed.substring(5, 9).trim(), 10),
        hour = feed.substring(9, 15),
        val = parseFloat(feed.substring(15).trim());
    if (val != -999)
        val /= 10;
    return {
    	station: station,
    	year: year,
    	hour: hour,
    	value: val
    };
}

module.exports.Reader = Reader; 

var r = new Reader();
r.on('file', function(file) {
	console.log(file);
});
r.on('error', function(err) {
	console.log(err);
});
r.on('end-folder', function(path) {
	console.log('finished folder', path);
});


r.on('station', function(station) {
	// console.log('station: ', station);
});

var recordCount = 0;

r.on('record', function(record) {
	recordCount++;
	if(recordCount % 10000 === 0) {
		console.log(recordCount, "records handled");
		console.log('last record: ', record);
	}
});

r.readFolder('C:/Users/Besitzer/Projects/sophena/repo/basedata/climate_data/raw_data');


//var folder = 'C:/Users/Besitzer/Projects/sophena/repo/basedata/climate_data/raw_data';
//readFolder(folder);



/*
var metaFile = 'C:/Users/Besitzer/Projects/sophena/repo/basedata/climate_data/raw_data/sy_ds_abgabe011_15_1bestand.txt';

readMetaDataFile(metaFile, function(station) {
	console.log(station);
}, function() {
	console.log('all done');
});
*/