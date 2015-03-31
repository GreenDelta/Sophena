var events = require('events'),
    fs = require('fs'), 
    byline = require('byline'),
    mysql = require('mysql'),
    hours = require('./hour.js');


var eventEmitter = new events.EventEmitter();
/*
var basePath = './climate_data/raw_data/sy_ds_abgabe011_15_';
for (var i = 1; i < 11; i++) {
    var path = basePath + i + 'elc.txt';
    streamDataFile(path);
}
*/
streamDataFile('./climate_data/raw_data/xxx_test_file.txt')


function streamDataFile(path) {
    
    var pool = mysql.createPool({
        connectionLimit : 10,
        host            : 'localhost',
        user            : 'root',
        database        : 'climatedb',
        port : 3306
    });
    
    var hourIndex = hours.keyIndexMap();
    
    var stream = fs.createReadStream(path, { encoding: 'utf8' }),
        lineCount = 0,
        insertCount = 0,
        fileFinished = false;
    
    stream = byline.createStream(stream);
    stream.on('readable', function () {
        var line;
        while (null !== (line = stream.read())) {
            lineCount++;
            handleLine(line, pool, hourIndex);
        }
    });
    
    stream.on('end', function () {
        console.log('file finished with');
        fileFinished = true;
    });
    
    eventEmitter.on('insertDone', function () {
        insertCount++;
        //console.log('insert', insertCount, 'lines', lineCount);
        if (fileFinished && insertCount == lineCount) {
            pool.end();
            console.log('all done');
        }
    });
}

function handleLine(line, pool, hourIndex) {
    var feed = line.trim(),
        station = feed.substring(0, 5).trim(),
        year = parseInt(feed.substring(5, 9).trim(), 10),
        key = feed.substring(9, 15),
        hour = hourIndex[key] + 1;
    
    if (!hour) {
        console.log('unkown hour: ' + key);
        return;
    }
    
    var val = parseFloat(feed.substring(15).trim());
    if (val != -999)
        val /= 10;
    
    var sql = "INSERT INTO tbl_data(f_station, year, hour, temperature) " + 
            "VALUES(?, ?, ?, ?) ";
    var values = [station, year, hour, val];
    sql = mysql.format(sql, values);
    
    //console.log(sql);
    
    pool.getConnection(function (err, connection) {
        if (err)
            return console.log(err);
        connection.query(sql, function (err, rows) {
            if (err)
                return console.log('failed to execute ' + sql);
            connection.release();
            //console.log('done with', sql);
            eventEmitter.emit('insertDone');
        });
    });
}