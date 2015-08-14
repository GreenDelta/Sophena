var fs = require('fs'),
    archiver = require('archiver');

var file = '../data_package.sophena';

fs.stat(file, function(err, stat) {
    if (err == null) {
        deleteAndPack(file);
    } else if (err.code == 'ENOENT') {
        // file does not exists
        packData(file);
    } else {
        // some other error
        throw err;
    }
});

function deleteAndPack(file) {
    console.log('File exists; delete old version');
    fs.unlink(file, function(err) {
        if (err) {
            console.log('failed to delete ' + file);
            throw err;
        }
        packData(file);
    });
}

function packData(file) {
    var output = fs.createWriteStream(file);
    var archive = archiver('zip');

    output.on('close', function() {
        console.log(archive.pointer() + ' total bytes');
        console.log('package written: ' + file);
    });

    archive.on('error', function(err) {
        throw err;
    });

    archive.pipe(output);

    var dir = '../data/',
        types = ['fuels', 'boilers', 'pipes', 'cost_settings'];
    for(var i = 0; i < types.length; i++) {
        var t = types[i];
        archive.bulk([{
            expand: true,
            cwd: dir + t,
            src: ['**'],
            dest: t
        }]);
    }
    
    archive.bulk([{
        expand: true,
        cwd: '../climate_data/out/json',
        src: ['**'],
        dest: 'weather_stations'
    }]);
    archive.finalize();
}
