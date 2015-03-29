var fs = require('fs'), 
    mkdir = require('mkdirp');

function createIndex() {
    var months = ["01", "02", "03", "04", "05", "06",
				"07", "08", "09", "10", "11", "12"];
    var days = [31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31];
    var index = [];
    var idx = 0;
    for (var month = 0; month < months.length; month++) {
        for (var day = 1; day <= days[month]; day++) {
            for (var hour = 0; hour < 24; hour++) {
                var key = months[month] + format(day) + format(hour);
                index.push(key);
                idx++;
            }
        }
    }
    return index;
}

function format(val) {
    if (val < 10)
        return "0" + val;
    else
        return val.toString();
}


module.exports.splitter = function (baseDir) {
    
    var index = createIndex(),
        currentStation = null,
        currentYear = null,
        data = {};
    
    var handle = function (line) {
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
    
    return { handle: handle };
}

