var fs = require('fs'), 
    hours = require('./hour.js'),
    mkdir = require('mkdirp');


module.exports.splitter = function (baseDir) {
    
    var index = hours.index(),
        currentStation = null,
        currentYear = null,
        data = {};
    
    var handleLine = function (line) {
        if (!line)
            return;
        var feed = line.trim(),
            station = feed.substring(0, 5).trim(),
            year = feed.substring(5, 9).trim(),
            key = feed.substring(9, 15),
            val = parseFloat(feed.substring(15).trim());
        if (station !== currentStation || year !== currentYear) {
            writeData(currentStation, currentYear, data);
            data = {};
            currentStation = station;
            currentYear = year;
        }
        data[key] = val;
    };
    
    function writeData(station, year, data) {
        if (!station || !year)
            return;
        var folder = baseDir + '/' + station;
        mkdir.mkdirp(folder, function (err) {
            if (err)
                return console.error(err);
            var file = folder + '/' + year + '.csv',
                text = '';
            for (var i = 0; i < index.length; i++) {
                var key = index[i];
                var val = data[key];
                if (!val || val === -999)
                    val = -999;
                else
                    val /= 10;
                text += (i + 1).toString() + ';' + key + ';' + val.toString() + '\n';
            }
            console.log("write file", file);
            fs.writeFile(file, text, function (err) {
                if (err)
                    return console.log(err);
            });
        });
    }
    
    return {
        handleLine: handleLine, 
        flush: function () {
            writeData(currentStation, currentYear, data);
        }
    }
}

