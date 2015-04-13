var climatedb = require('../lib/climatedb.js');

var calc = new climatedb.AvgCalculator({
        connectionLimit : 10,
        host            : 'localhost',
        user            : 'root',
        database        : 'climatedb',
        port : 3306
    });

calc.calculateStation('10004', 2005, 2014); 