var fs = require('fs'),
	archiver = require('archiver');

var output = fs.createWriteStream('../data_package.sophena');
var archive = archiver('zip');

output.on('close', function () {
    console.log(archive.pointer() + ' total bytes');
    console.log('archiver has been finalized and the output file descriptor has closed.');
});

archive.on('error', function(err){
    throw err;
});

archive.pipe(output);
archive.bulk([
    { expand: true, cwd: '../data/fuels', src: ['**'], dest: 'fuels'}
]);
archive.finalize();