var climatedb = require('./climatedb.js'),
    async = require('async'),
    mysql = require('mysql'),
    fs = require('fs');

/**
 * Calculates the statistics for the different weather stations in the database.
 *
 * @param {Object} config - a configuration object with the following attributes:
 *        {
             con: "the database connection or connection pool",
             startYear: "start year for the statistics",
             endYear: "end year for the statistics"
 *        }
 * @param cb - callback(err, results) the callback that is called when the
 *             calculation of the is finished or an error occured.
 *
 */
function calculate(config, cb) {
    console.log('calculate the station statistics');
    climatedb.getStations(config.con, function(err, rows) {
        if (err) {
            return cb(err);
        }
        console.log('  fetched stations');
        var tasks = [];
        for (var i = 0; i < rows.length; i++) {
            tasks.push(createTask(config, rows[i]));
        }
        async.series(tasks, function(err, resuls) {
            cb(null, results);
        });
        /**
        var results = [];
        for(var i = 0; i < tasks.length; i++) {
            tasks[i](function(err, result) {
                results.push(result);
                if(results.length == tasks.length)
                    cb(null, results);
            });
        }
        /**
        async.parallelLimit(tasks, 100, function(err, results) {
            if(err)
                throw err;
            console.log("ready", results);
            cb(null, results);
        });
        **/
    });
}

function createTask(config) {
    return function(cb) {
        console.log("handle", config.station);
        climatedb.getDataTable(config, function(err, data) {
            if (err) {
                cb(err);
            }
            console.log("data loaded for", config.station);
            var result = calcStats(config, data);
            console.log("result calculated for", config.station);
            cb(null, result);
        });
    }
}

function calcStats(config, dataTable) {
    var result = {
        station: config.station,
        label: config.label,
        stats: []
    };
    for (var year in dataTable) {
        if (dataTable.hasOwnProperty(year)) {
            var s = checkDataRow(dataTable[year]);
            s.year = year;
            result.stats.push(s);
        }
    }
    return result;
}

function checkDataRow(row) {
    var LARGE_INTERVAL_LIMIT = 6;
    var valueCount = 0,
        errorCount = 0,
        errorBlockSize = 0,
        largeErrorIntervalCount = 0;
    for (var i = 0; i < row.length; i++) {
        var v = row[i];
        if (v === -999) {
            errorCount++;
            errorBlockSize++;
        } else {
            valueCount++;
            if (errorBlockSize > LARGE_INTERVAL_LIMIT) {
                largeErrorIntervalCount++;
            }
            errorBlockSize = 0;
        }
    }
    if (errorBlockSize > LARGE_INTERVAL_LIMIT) {
        largeErrorIntervalCount++;
    }
    return {
        valueCount: valueCount,
        errorCount: errorCount,
        largeErrorIntervalCount: largeErrorIntervalCount
    };
}


var pool = mysql.createPool({
    connectionLimit: 10,
    host: 'localhost',
    user: 'root',
    database: 'climatedb',
    port: 3306
})
var config = {
    con: pool,
    startYear: 2005,
    endYear: 2014
};

climatedb.getStations(config.con, function(err, rows) {
    if (err) {
        throw err;
    }
    var tasks = [];
    for (var i = 0; i < rows.length; i++) {
        var statConf = {
            con: config.con,
            station: rows[i].id,
            label: rows[i].name,
            startYear: config.startYear,
            endYear: config.endYear
        };
        tasks.push(createTask(statConf));
    }
    async.parallelLimit(tasks, 250, function(err, results) {
        if (err) {
            throw err;
        }
        var report = 'Station\tJahr\tWerte\tFehler\tFehlerintervalle >6h\n';
        for (var i = 0; i < results.length; i++) {
            var result = results[i];
            for (var j = 0; j < result.stats.length; j++) {
                var stat = result.stats[j];
                report += result.label + '\t' + stat.year + '\t' + stat.valueCount + '\t' + stat.errorCount + '\t' + stat.largeErrorIntervalCount + '\n';
            }
        }
        fs.writeFile("stats.txt", report, function(err) {
            if (err) {
                throw err;
            }
            console.log("report was written");
            config.con.close();
        });

    });
});

module.exports.calculate = calculate;
