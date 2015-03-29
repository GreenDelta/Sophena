var fs = require('fs'), 
    byline = require('byline'),
    split = require('./climate_data_split.js');

var path = './climate_data/raw_data/sy_ds_abgabe011_15_10elc.txt';
var stream = fs.createReadStream(path, { encoding: 'utf8' });
stream = byline.createStream(stream);

var splitter = split.splitter('climate_data/out');

var count = 0;
stream.on('readable', function () {
    var line;
    while (null !== (line = stream.read())) {
        splitter.handle(line);
        count++;
    }    
});

stream.on('end', function () {
    console.log(count, 'lines');
});

