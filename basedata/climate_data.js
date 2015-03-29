var fs = require('fs'), 
    byline = require('byline'),
    split = require('./climate_data_split.js');

var splitter = split.splitter('climate_data/out/splitted');

var basePath = './climate_data/raw_data/sy_ds_abgabe011_15_';
for (var i = 1; i < 11; i++) {
    var path = basePath + i + 'elc.txt';
    streamDataFile(path);
}

function streamDataFile(path) {
    var stream = fs.createReadStream(path, { encoding: 'utf8' }),
        lineCount = 0;
    stream = byline.createStream(stream);
    stream.on('readable', function () {
        var line;
        while (null !== (line = stream.read())) {
            splitter.handleLine(line);
            count++;
        }
    });
    
    stream.on('end', function () {
        splitter.flush();
        console.log(count, 'lines handled');
    });
}

