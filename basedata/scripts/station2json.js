// This script exports the climate data to JSON files that can be
// exported into the Sophena application.

var async = require('async'),
    db = require('../lib/climatedb.js'),
    fs = require('fs'),
    mysql = require('mysql'),
    uuid = require('node-uuid');

var pool = mysql.createPool({
    connectionLimit: 10,
    host: 'localhost',
    user: 'root',
    database: 'climatedb',
    port: 3306
});

var outFolder = 'C:/Users/Besitzer/Projects/sophena/repo/basedata/climate_data/out/json/';

db.getStations(pool, function(err, stations) {
    if (err) {
        throw err;
    }
    var handlers = [];
    for (var i = 0; i < stations.length; i++) {
        var station = stations[i];
        handlers.push(stationHandler(station));
    }
    async.series(handlers, function(err) {
        if (err) {
            throw err;
        }
        console.log('all done');
        pool.end();
    });
});

function stationHandler(station) {
    return function(cb) {
        var config = {
            con: pool,
            station: station.id,
            startYear: 2005,
            endYear: 2014
        };
        db.getDataTable(config, function(err, table) {
            if (err) {
                cb(err);
            }
            handleStation(station, table);
            cb(null);
        });
    }
}

function handleStation(station, table) {
    console.log('handle station', station)
    var data = null,
        grid = {},
        years = 0;
    for (var year = 2005; year < 2015; year++) {
        console.log('  check year', year);
        data = table[year];
        var column = createGridColumn(data);
        if (!column) {
            console.log('    > excluded year ' + year);
        } else {
            grid[year] = column;
            years++;
        }
    }
    if(years < 7) {
        console.log('>> excluded station; less than 7 years')
        return;
    }
    var refYear = grid[2009] ? 2009 : 2010;
    if(!grid[refYear]) {
        console.log('>> excluded; reference year 2009 or 2010 missing');
        return;
    }
    createCurve(station, grid, refYear);
}

function createGridColumn(data) {
    var totalMissing = 0,
        currentMissing = 0,
        record = null,
        col = [];
    for (var i = 0; i < data.length; i++) {
        record = {
            index: i,
            value: data[i],
            missing: false
        };
        col.push(record);
        if (record.value !== -999) {
            currentMissing = 0;
        } else {
            record.missing = true;
            currentMissing++;
            totalMissing++;
            if (currentMissing > 11) {
                console.log('    more than 11 values missing in series');
                return null;
            }
            if (totalMissing > 670) {
                console.log('    more than 670 values missing in total');
                return null;
            }
        }
    }
    console.log('    ok (' + totalMissing + ' values missing)');
    calcuateMissingValues(col);
    col.sort(function(a, b) {
        return a.value - b.value;
    });
    return col;
}

function calcuateMissingValues(col) {
    var i = 0, j = 0, k = 0;
    while (i < 8760) {
        if (!col[i].missing) {
            i++;
            continue;
        }
        // found an error interval
        // 'start' and 'end' are the indices before and after the error interval
        var start = i === 0 ? -1 : i - 1;
        var end = i + 1;
        while (end < 8760 && col[end].missing) {
            end++;
        }
        if (end >= 8760) {
            // error interval is at the end of the array
            for (k = i; k < 8760; k++) {
                col[k].value = col[start].value;
            }
        } else if (start < 0) {
            // error interval is at the beginning of the array
            for (k = 0; k < end; k++) {
                col[k].value = col[end].value;
            }
        } else {
            // error interval is between two indices with values
            var step = (col[end].value - col[start].value) / (end - start);
            j = 1;
            for (k = i; k < end; k++) {
                col[k].value = col[start].value + j * step;
                j++;
            }
        }
        i = end + 1;
    }
}

function createCurve(station, grid, refYear) {
    var curve = [];
    for(var i = 0; i < 8760; i++) {
        var sum = 0;
        var years = 0;
        for(var year = 2005; year < 2015; year++) {
            var col = grid[year];
            if(!col || col[i].missing) {
                continue;
            }
            sum += col[i].value;
            years++;
        }
        if(years === 0) {
            throw 'missing row ' + i + ' in station ' + station;
        }
        curve.push({
            value: sum/years,
            index: grid[refYear][i].index
        });
    }
    curve.sort(function(a, b) {
        return a.index - b.index;
    });
    var obj = {
        id: uuid.v4().toString(),
        name: station.name,
        data: []
    };
    for(var i = 0; i < 8760; i++) {
        obj.data[i] = curve[i].value.toFixed(1);
    }
    dump(obj, obj.id);
}

function dump(obj, id) {
    var fname = outFolder + id + '.json';
    var json = JSON.stringify(obj, null, 4);
    fs.writeFile(fname, json, function(err) {
		if (err)
			throw err;
		console.log('wrote file ' + id + '.json');
	});
}
