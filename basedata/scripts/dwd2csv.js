var dwd = require('../lib/dwd.js'),
	hours = require('../lib/hours.js'),
	fs = require('fs');

var dataOutPath = 'C:/Users/Besitzer/Projects/sophena/repo/basedata/climate_data/out/data_out.csv';
var metaOutPath = 'C:/Users/Besitzer/Projects/sophena/repo/basedata/climate_data/out/meta_out.csv';

var reader = new dwd.Reader(),
	dataOut = fs.createWriteStream(dataOutPath),
	metaOut = fs.createWriteStream(metaOutPath),
	hourIndex = new hours.Index(),
	dataBlock = '',
	metaBlock = '',
	dataCount = 0,
	metaCount = 0,
	errorCount = 0,
	handledStations = {};

reader.on('record', function(rec) {
	var val = rec.value;
	if(val === -999) {
		errorCount++;
		return;
	}
	hour = hourIndex.getHour(rec.hour);
	var line = rec.station + ',' + rec.year + ',' + hour + ',' + rec.value + '\n';
	dataBlock += line;
	dataCount++;
	if (dataCount % 10000 === 0) {
		writeDataBlock();	
	}
});

reader.on('station', function(stat) {
	if(!stat || handledStations[stat.id])
		return;
	handledStations[stat.id] = true;
	var line = stat.id + ',' + stat.name + '\n';
	metaBlock += line;
	metaCount++;
	if(metaCount % 1000 === 0) {
		writeMetaBlock();
	}
});

reader.on('end-folder', function() {
	writeDataBlock();
	writeMetaBlock();
	dataOut.end();
	metaOut.end();
	console.log('all finished')
});

function writeDataBlock() {
	if(!dataBlock)
		return;
	dataOut.write(dataBlock);
	dataBlock = '';
	console.log(dataCount, 'data rows written');	
}

function writeMetaBlock() {
	if(!metaBlock)
		return;
	metaOut.write(metaBlock);
	metaBlock = '';
	console.log(metaCount, 'meta rows written');
}

//reader.readDataFile('C:/Users/Besitzer/Projects/sophena/repo/basedata/climate_data/raw_data/sy_ds_abgabe011_15_10elc.txt')
//reader.readMetaFile('C:/Users/Besitzer/Projects/sophena/repo/basedata/climate_data/raw_data/sy_ds_abgabe011_15_10bestand.txt')
reader.readFolder('C:/Users/Besitzer/Projects/sophena/repo/basedata/climate_data/raw_data');